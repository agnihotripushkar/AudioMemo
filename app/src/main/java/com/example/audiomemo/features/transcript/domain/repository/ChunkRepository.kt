package com.example.audiomemo.features.transcript.domain.repository

import com.example.audiomemo.features.transcript.domain.model.Chunk
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import kotlinx.coroutines.flow.Flow

interface ChunkRepository {
    fun getChunksForSession(sessionId: Long): Flow<List<Chunk>>
    suspend fun getChunksByStatus(status: ChunkStatus): List<Chunk>
    suspend fun getChunksForSessionOnce(sessionId: Long): List<Chunk>
    suspend fun insert(chunk: Chunk): Long
    suspend fun update(chunk: Chunk)
    suspend fun updateStatus(id: Long, status: ChunkStatus)
    suspend fun deleteForSession(sessionId: Long)
}
