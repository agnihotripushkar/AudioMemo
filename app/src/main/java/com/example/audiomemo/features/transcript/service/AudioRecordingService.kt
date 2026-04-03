package com.example.audiomemo.features.transcript.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.audiomemo.features.transcript.data.worker.UploadPreferences
import com.example.audiomemo.data.db.dao.ChunkDao
import com.example.audiomemo.data.db.dao.SessionDao
import com.example.audiomemo.features.summary.data.worker.SummaryGenerationWorker
import com.example.audiomemo.features.transcript.data.worker.ChunkFinalizationWorker
import com.example.audiomemo.features.transcript.data.worker.TranscriptRetryWorker
import com.example.audiomemo.features.transcript.data.worker.WhisperUploadWorker
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import com.example.audiomemo.features.transcript.manager.AudioInterruptionManager
import com.example.audiomemo.features.transcript.manager.AudioRecorderManager
import com.example.audiomemo.features.transcript.manager.BatteryGuard
import com.example.audiomemo.features.transcript.manager.MediaButtonHandler
import com.example.audiomemo.features.transcript.manager.SessionStateManager
import com.example.audiomemo.features.transcript.manager.SilenceDetector
import com.example.audiomemo.features.transcript.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    companion object {
        const val ACTION_STOP = "com.example.audiomemo.action.STOP_RECORDING"
        const val ACTION_RESUME = "com.example.audiomemo.action.RESUME_RECORDING"
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordingService = this@AudioRecordingService
    }

    @Inject lateinit var sessionDao: SessionDao
    @Inject lateinit var chunkDao: ChunkDao

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Tracks the most recent chunk-save job so we can await it before enqueuing transcription. */
    private var lastChunkSaveJob: kotlinx.coroutines.Job? = null

    private lateinit var recorder: AudioRecorderManager
    private lateinit var interruptionManager: AudioInterruptionManager
    private lateinit var silenceDetector: SilenceDetector
    private lateinit var sessionStateManager: SessionStateManager
    private lateinit var batteryGuard: BatteryGuard
    private lateinit var mediaButtonHandler: MediaButtonHandler

    /** True while the service is actively recording (not stopped). Used to reject duplicate starts. */
    private var isRecordingActive = false
    /** True when the user has manually paused via a headset/media button. */
    private var isMediaButtonPaused = false

    private val _isStopped = MutableStateFlow(false)
    val isStopped: StateFlow<Boolean> = _isStopped.asStateFlow()

    val amplitude: StateFlow<Int> get() = recorder.amplitude
    val lastChunkFile: StateFlow<File?> get() = recorder.lastChunkFile
    val currentSessionId: Long
        get() = if (::sessionStateManager.isInitialized) sessionStateManager.currentSessionId else -1L

    // ── Pending intents ────────────────────────────────────────────────────────

    private fun stopPendingIntent(): PendingIntent = PendingIntent.getService(
        this, 1,
        Intent(this, AudioRecordingService::class.java).apply { action = ACTION_STOP },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun resumePendingIntent(): PendingIntent = PendingIntent.getService(
        this, 2,
        Intent(this, AudioRecordingService::class.java).apply { action = ACTION_RESUME },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()

        recorder = AudioRecorderManager(applicationContext)
        sessionStateManager = SessionStateManager(sessionDao, chunkDao)

        recorder.onChunkCompleted = { file ->
            lastChunkSaveJob = serviceScope.launch { sessionStateManager.saveChunk(file.absolutePath) }
        }
        recorder.onStorageLow = { handleLowStorage() }
        recorder.onHardwareError = { handleHardwareError() }

        interruptionManager = AudioInterruptionManager(
            context = this,
            onPauseRequested = { reason -> handleInterruptionPause(reason) },
            onResumeRequested = { handleInterruptionResume() },
            onSourceChanged = { name -> handleSourceChanged(name) }
        )

        silenceDetector = SilenceDetector(
            context = this,
            amplitude = recorder.amplitude,
            onSilenceDetected = { handleSilenceDetected() },
            onPermissionRevoked = { handlePermissionRevoked() }
        )

        batteryGuard = BatteryGuard(
            context = this,
            onBatteryLow = { handleBatteryLow() }
        )

        mediaButtonHandler = MediaButtonHandler(
            context = this,
            onToggle    = { handleMediaButtonToggle() },
            onPauseOnly = { if (recorder.isRecording) handleMediaButtonPause() },
            onPlayOnly  = { if (isMediaButtonPaused) handleMediaButtonPlay() }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopRecordingCleanly()
                return START_NOT_STICKY
            }
            ACTION_RESUME -> {
                // Clear any user-initiated media-button pause so handleInterruptionResume resumes normally.
                isMediaButtonPaused = false
                handleInterruptionResume()
                return START_NOT_STICKY
            }
        }

        // ── Guard against duplicate starts ─────────────────────────────────────
        // TranscriptScreen may call startForegroundService more than once (e.g. back-stack
        // manipulation). Skip re-initialization if recording is already in progress.
        if (isRecordingActive) return START_STICKY

        // ── Start recording ────────────────────────────────────────────────────
        isRecordingActive = true
        NotificationHelper.createNotificationChannel(this)
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.buildForegroundNotification(this, stopPendingIntent())
        )

        serviceScope.launch {
            val sessionId = sessionStateManager.startSession()
            enqueueFinalizationWorker(sessionId)
        }

        recorder.startRecording()
        interruptionManager.start()
        silenceDetector.start()
        batteryGuard.start()
        mediaButtonHandler.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        if (!_isStopped.value) {
            recorder.stopRecording()
            silenceDetector.stop()
            interruptionManager.stop()
            batteryGuard.stop()
            serviceScope.launch { sessionStateManager.pauseSession() }
        }
        mediaButtonHandler.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Interruption handlers ──────────────────────────────────────────────────

    private fun handleInterruptionPause(reason: AudioInterruptionManager.PauseReason) {
        recorder.pauseRecording()
        silenceDetector.stop()
        serviceScope.launch { sessionStateManager.pauseSession() }

        val notification = when (reason) {
            AudioInterruptionManager.PauseReason.PHONE_CALL ->
                NotificationHelper.buildPausedPhoneCallNotification(
                    this, resumePendingIntent(), stopPendingIntent()
                )
            AudioInterruptionManager.PauseReason.AUDIO_FOCUS ->
                NotificationHelper.buildPausedAudioFocusNotification(
                    this, resumePendingIntent(), stopPendingIntent()
                )
            AudioInterruptionManager.PauseReason.MIC_MUTED ->
                NotificationHelper.buildPausedMicMutedNotification(
                    this, resumePendingIntent(), stopPendingIntent()
                )
        }
        NotificationHelper.updateNotification(this, notification)
    }

    private fun handleInterruptionResume() {
        // If the user has manually paused via headset button, keep the recorder paused
        // and show the media-button pause notification instead of resuming.
        if (isMediaButtonPaused) {
            NotificationHelper.updateNotification(
                this,
                NotificationHelper.buildPausedMediaButtonNotification(
                    this, resumePendingIntent(), stopPendingIntent()
                )
            )
            return
        }
        recorder.resumeRecording()
        silenceDetector.reset()
        silenceDetector.start()
        serviceScope.launch { sessionStateManager.resumeSession() }
        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildForegroundNotification(this, stopPendingIntent())
        )
    }

    // ── Media button handlers ──────────────────────────────────────────────────

    private fun handleMediaButtonToggle() {
        if (_isStopped.value) return
        if (recorder.isRecording) handleMediaButtonPause() else handleMediaButtonPlay()
    }

    private fun handleMediaButtonPause() {
        if (_isStopped.value || !recorder.isRecording) return
        isMediaButtonPaused = true
        recorder.pauseRecording()
        silenceDetector.stop()
        serviceScope.launch { sessionStateManager.pauseSession() }
        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildPausedMediaButtonNotification(
                this, resumePendingIntent(), stopPendingIntent()
            )
        )
    }

    private fun handleMediaButtonPlay() {
        if (_isStopped.value || !isMediaButtonPaused) return
        isMediaButtonPaused = false
        // Only physically resume the recorder if no other interruption (call/focus/mic) is
        // still active. If one is, handleInterruptionResume will resume when it clears.
        if (!recorder.isRecording) {
            recorder.resumeRecording()
            silenceDetector.reset()
            silenceDetector.start()
            serviceScope.launch { sessionStateManager.resumeSession() }
            NotificationHelper.updateNotification(
                this,
                NotificationHelper.buildForegroundNotification(this, stopPendingIntent())
            )
        }
    }

    private fun handleSourceChanged(sourceName: String) {
        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildMicSourceChangedNotification(this, sourceName, stopPendingIntent())
        )
        serviceScope.launch {
            kotlinx.coroutines.delay(3_000)
            if (!_isStopped.value) {
                NotificationHelper.updateNotification(
                    this@AudioRecordingService,
                    NotificationHelper.buildForegroundNotification(
                        this@AudioRecordingService, stopPendingIntent()
                    )
                )
            }
        }
    }

    private fun handleSilenceDetected() {
        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildSilenceWarningNotification(this, stopPendingIntent())
        )
    }

    private fun handleBatteryLow() {
        recorder.stopRecording()
        silenceDetector.stop()
        interruptionManager.stop()
        batteryGuard.stop()
        val sessionId = sessionStateManager.currentSessionId
        val savedChunkJob = lastChunkSaveJob
        serviceScope.launch {
            savedChunkJob?.join()
            sessionStateManager.stopSession()
            cancelFinalizationWorker(sessionId)
            enqueueTranscriptionChain(sessionId)
        }
        _isStopped.value = true

        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildBatteryLowNotification(this)
        )
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun handleLowStorage() {
        recorder.stopRecording()
        silenceDetector.stop()
        interruptionManager.stop()
        batteryGuard.stop()
        val sessionId = sessionStateManager.currentSessionId
        val savedChunkJob = lastChunkSaveJob
        serviceScope.launch {
            savedChunkJob?.join()
            sessionStateManager.stopSession()
            cancelFinalizationWorker(sessionId)
            enqueueTranscriptionChain(sessionId)
        }
        _isStopped.value = true

        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildLowStorageNotification(this)
        )
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun handlePermissionRevoked() {
        recorder.stopRecording()
        silenceDetector.stop()
        interruptionManager.stop()
        batteryGuard.stop()
        val sessionId = sessionStateManager.currentSessionId
        val savedChunkJob = lastChunkSaveJob
        serviceScope.launch {
            savedChunkJob?.join()
            sessionStateManager.stopSession()
            cancelFinalizationWorker(sessionId)
            enqueueTranscriptionChain(sessionId)
        }
        _isStopped.value = true

        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildPermissionRevokedNotification(this)
        )
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun handleHardwareError() {
        silenceDetector.stop()
        interruptionManager.stop()
        batteryGuard.stop()
        val sessionId = sessionStateManager.currentSessionId
        val savedChunkJob = lastChunkSaveJob
        serviceScope.launch {
            savedChunkJob?.join()
            sessionStateManager.stopSession()
            cancelFinalizationWorker(sessionId)
            enqueueTranscriptionChain(sessionId)
        }
        _isStopped.value = true

        NotificationHelper.updateNotification(
            this,
            NotificationHelper.buildHardwareErrorNotification(this)
        )
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    /** Normal stop via user action (Stop button or ACTION_STOP intent). */
    private fun stopRecordingCleanly() {
        recorder.stopRecording()
        silenceDetector.stop()
        interruptionManager.stop()
        batteryGuard.stop()
        mediaButtonHandler.stop()
        val sessionId = sessionStateManager.currentSessionId
        val savedChunkJob = lastChunkSaveJob
        serviceScope.launch {
            savedChunkJob?.join()
            sessionStateManager.stopSession()
            cancelFinalizationWorker(sessionId)
            enqueueTranscriptionChain(sessionId)
        }
        _isStopped.value = true
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── WorkManager helpers ────────────────────────────────────────────────────

    private fun enqueueFinalizationWorker(sessionId: Long) {
        if (sessionId <= 0L) return
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "${ChunkFinalizationWorker.WORK_NAME_PREFIX}$sessionId",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ChunkFinalizationWorker>()
                .setInputData(
                    Data.Builder()
                        .putLong(ChunkFinalizationWorker.KEY_SESSION_ID, sessionId)
                        .build()
                )
                .setInitialDelay(15, TimeUnit.SECONDS)
                .build()
        )
    }

    private fun cancelFinalizationWorker(sessionId: Long) {
        if (sessionId <= 0L) return
        WorkManager.getInstance(applicationContext)
            .cancelUniqueWork("${ChunkFinalizationWorker.WORK_NAME_PREFIX}$sessionId")
    }

    private suspend fun enqueueTranscriptionChain(sessionId: Long) {
        if (sessionId <= 0L) return

        val pendingChunks = chunkDao.getChunksForSessionOnce(sessionId)
            .filter { it.status == ChunkStatus.PENDING }

        val summaryRequest = OneTimeWorkRequestBuilder<SummaryGenerationWorker>()
            .setInputData(workDataOf(SummaryGenerationWorker.KEY_SESSION_ID to sessionId))
            .addTag("${SummaryGenerationWorker.WORK_NAME_PREFIX}$sessionId")
            .build()

        if (pendingChunks.isEmpty()) {
            WorkManager.getInstance(applicationContext).enqueue(summaryRequest)
            return
        }

        val uploadConstraints: Constraints = UploadPreferences.networkConstraints(applicationContext)

        val uploadRequests = pendingChunks.map { chunk ->
            OneTimeWorkRequestBuilder<WhisperUploadWorker>()
                .setConstraints(uploadConstraints)
                .setInputData(
                    workDataOf(
                        WhisperUploadWorker.KEY_CHUNK_ID to chunk.id,
                        WhisperUploadWorker.KEY_SESSION_ID to chunk.sessionId
                    )
                )
                .addTag("${WhisperUploadWorker.WORK_NAME_PREFIX}${chunk.id}")
                .build()
        }

        WorkManager.getInstance(applicationContext)
            .beginWith(uploadRequests)
            .then(summaryRequest)
            .enqueue()
    }
}
