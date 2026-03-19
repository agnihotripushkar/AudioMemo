package com.example.audiomemo.features.transcript.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.audiomemo.data.db.dao.ChunkDao
import com.example.audiomemo.data.db.dao.SessionDao
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChunkFinalizationEntryPoint {
    fun sessionDao(): SessionDao
    fun chunkDao(): ChunkDao
}

/**
 * WorkManager worker enqueued every time recording starts. It runs after a short delay
 * so that a clean service stop can cancel it first. If the process is killed before the
 * service can cancel it (crash / OOM), the worker runs and:
 *  - marks the session STOPPED in Room, and
 *  - marks any PENDING chunks FAILED so the upload/transcription pipeline can retry them.
 */
class ChunkFinalizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val WORK_NAME_PREFIX = "chunk_finalization_"
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getLong(KEY_SESSION_ID, -1L)
        if (sessionId < 0L) return Result.failure()

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ChunkFinalizationEntryPoint::class.java
        )
        val sessionDao: SessionDao = entryPoint.sessionDao()
        val chunkDao: ChunkDao = entryPoint.chunkDao()

        val session = sessionDao.getById(sessionId)
        if (session == null || session.state == SessionState.STOPPED) return Result.success()

        sessionDao.updateState(sessionId, SessionState.STOPPED)

        chunkDao.getChunksByStatus(ChunkStatus.PENDING)
            .filter { it.sessionId == sessionId }
            .forEach { chunkDao.updateStatus(it.id, ChunkStatus.FAILED) }

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "${TranscriptRetryWorker.WORK_NAME_PREFIX}$sessionId",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TranscriptRetryWorker>()
                .setInputData(
                    Data.Builder()
                        .putLong(TranscriptRetryWorker.KEY_SESSION_ID, sessionId)
                        .build()
                )
                .build()
        )

        return Result.success()
    }
}
