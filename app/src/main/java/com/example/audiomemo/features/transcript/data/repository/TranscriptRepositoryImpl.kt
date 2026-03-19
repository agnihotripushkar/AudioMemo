package com.example.audiomemo.features.transcript.data.repository

import com.example.audiomemo.data.db.dao.TranscriptDao
import com.example.audiomemo.features.transcript.data.mapper.toDomain
import com.example.audiomemo.features.transcript.data.mapper.toEntity
import com.example.audiomemo.features.transcript.domain.model.Transcript
import com.example.audiomemo.features.transcript.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TranscriptRepositoryImpl @Inject constructor(
    private val transcriptDao: TranscriptDao
) : TranscriptRepository {

    override fun getTranscriptsForSession(sessionId: Long): Flow<List<Transcript>> =
        transcriptDao.getTranscriptsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(transcript: Transcript): Long =
        transcriptDao.insert(transcript.toEntity())

    override suspend fun getByChunk(sessionId: Long, chunkIndex: Int): Transcript? =
        transcriptDao.getByChunk(sessionId, chunkIndex)?.toDomain()

    override suspend fun deleteForSession(sessionId: Long) =
        transcriptDao.deleteForSession(sessionId)
}
