package com.example.audiomemo.features.transcript.domain.repository

import com.example.audiomemo.features.transcript.domain.model.Session
import com.example.audiomemo.features.transcript.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun insert(session: Session): Long
    suspend fun update(session: Session)
    suspend fun getById(id: Long): Session?
    fun getAllSessions(): Flow<List<Session>>
    suspend fun updateState(id: Long, state: SessionState)
    suspend fun updateDuration(id: Long, duration: Long)
    suspend fun deleteById(id: Long)
    suspend fun getLastActiveSession(): Session?
}
