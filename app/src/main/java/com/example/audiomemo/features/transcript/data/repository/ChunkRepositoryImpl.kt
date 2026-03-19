package com.example.audiomemo.features.transcript.data.repository

import com.example.audiomemo.data.db.dao.ChunkDao
import com.example.audiomemo.features.transcript.data.mapper.toDomain
import com.example.audiomemo.features.transcript.data.mapper.toEntity
import com.example.audiomemo.features.transcript.domain.model.Chunk
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import com.example.audiomemo.features.transcript.domain.repository.ChunkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChunkRepositoryImpl @Inject constructor(
    private val chunkDao: ChunkDao
) : ChunkRepository {

    override fun getChunksForSession(sessionId: Long): Flow<List<Chunk>> =
        chunkDao.getChunksForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override suspend fun getChunksByStatus(status: ChunkStatus): List<Chunk> =
        chunkDao.getChunksByStatus(status).map { it.toDomain() }

    override suspend fun getChunksForSessionOnce(sessionId: Long): List<Chunk> =
        chunkDao.getChunksForSessionOnce(sessionId).map { it.toDomain() }

    override suspend fun insert(chunk: Chunk): Long =
        chunkDao.insert(chunk.toEntity())

    override suspend fun update(chunk: Chunk) =
        chunkDao.update(chunk.toEntity())

    override suspend fun updateStatus(id: Long, status: ChunkStatus) =
        chunkDao.updateStatus(id, status)

    override suspend fun deleteForSession(sessionId: Long) =
        chunkDao.deleteForSession(sessionId)
}
