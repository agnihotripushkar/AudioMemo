package com.example.audiomemo.features.transcript.data.repository

import com.example.audiomemo.data.db.dao.SessionDao
import com.example.audiomemo.features.transcript.data.mapper.toDomain
import com.example.audiomemo.features.transcript.data.mapper.toEntity
import com.example.audiomemo.features.transcript.domain.model.Session
import com.example.audiomemo.features.transcript.domain.model.SessionState
import com.example.audiomemo.features.transcript.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun insert(session: Session): Long =
        sessionDao.insert(session.toEntity())

    override suspend fun update(session: Session) =
        sessionDao.update(session.toEntity())

    override suspend fun getById(id: Long): Session? =
        sessionDao.getById(id)?.toDomain()

    override fun getAllSessions(): Flow<List<Session>> =
        sessionDao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override suspend fun updateState(id: Long, state: SessionState) =
        sessionDao.updateState(id, state)

    override suspend fun updateDuration(id: Long, duration: Long) =
        sessionDao.updateDuration(id, duration)

    override suspend fun deleteById(id: Long) =
        sessionDao.deleteById(id)

    override suspend fun getLastActiveSession(): Session? =
        sessionDao.getLastActiveSession()?.toDomain()
}
