package com.example.audiomemo.features.transcript.manager

import com.example.audiomemo.data.db.dao.ChunkDao
import com.example.audiomemo.data.db.dao.SessionDao
import com.example.audiomemo.data.db.entities.ChunkEntity
import com.example.audiomemo.data.db.entities.SessionEntity
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Persists recording session lifecycle (RECORDING → PAUSED → STOPPED) and completed chunk
 * metadata to Room so the app can recover after a crash or process death.
 */
class SessionStateManager(
    private val sessionDao: SessionDao,
    private val chunkDao: ChunkDao
) {
    var currentSessionId: Long = -1L
        private set

    private var chunkIndex = 0

    suspend fun startSession(): Long = withContext(Dispatchers.IO) {
        // Promote any RECORDING sessions left by a prior crash/process-kill to PAUSED so
        // getLastActiveSession() doesn't surface them as still-live ghost sessions.
        sessionDao.abandonOrphanedSessions()
        val id = sessionDao.insert(
            SessionEntity(
                state = SessionState.RECORDING,
                startTime = System.currentTimeMillis()
            )
        )
        currentSessionId = id
        chunkIndex = 0
        id
    }

    suspend fun pauseSession() = withContext(Dispatchers.IO) {
        if (currentSessionId > 0) sessionDao.updateState(currentSessionId, SessionState.PAUSED)
    }

    suspend fun resumeSession() = withContext(Dispatchers.IO) {
        if (currentSessionId > 0) sessionDao.updateState(currentSessionId, SessionState.RECORDING)
    }

    suspend fun stopSession() = withContext(Dispatchers.IO) {
        if (currentSessionId > 0) sessionDao.updateState(currentSessionId, SessionState.STOPPED)
    }

    suspend fun saveChunk(filePath: String) = withContext(Dispatchers.IO) {
        if (currentSessionId > 0) {
            chunkDao.insert(
                ChunkEntity(
                    sessionId = currentSessionId,
                    chunkIndex = chunkIndex++,
                    filePath = filePath,
                    status = ChunkStatus.PENDING
                )
            )
        }
    }
}
