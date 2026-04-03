package com.example.audiomemo.features.transcript.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.audiomemo.data.db.dao.ChunkDao
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TranscriptRetryEntryPoint {
    fun chunkDao(): ChunkDao
}

/**
 * Finds all FAILED chunks for a session and re-enqueues [WhisperUploadWorker] for each.
 * Enqueued by [ChunkFinalizationWorker] after crash-recovery, or can be triggered manually.
 */
class TranscriptRetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val WORK_NAME_PREFIX = "transcript_retry_"
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getLong(KEY_SESSION_ID, -1L)
        if (sessionId < 0L) return Result.failure()

        val ep = EntryPointAccessors.fromApplication(
            applicationContext,
            TranscriptRetryEntryPoint::class.java
        )
        val chunkDao = ep.chunkDao()

        val failedChunks = chunkDao.getChunksByStatus(ChunkStatus.FAILED)
            .filter { it.sessionId == sessionId }

        val uploadConstraints = UploadPreferences.networkConstraints(applicationContext)

        failedChunks.forEach { chunk ->
            chunkDao.updateStatus(chunk.id, ChunkStatus.PENDING)
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "${WhisperUploadWorker.WORK_NAME_PREFIX}${chunk.id}",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<WhisperUploadWorker>()
                    .setConstraints(uploadConstraints)
                    .setInputData(
                        Data.Builder()
                            .putLong(WhisperUploadWorker.KEY_CHUNK_ID, chunk.id)
                            .putLong(WhisperUploadWorker.KEY_SESSION_ID, chunk.sessionId)
                            .build()
                    )
                    .build()
            )
        }

        return Result.success()
    }
}
