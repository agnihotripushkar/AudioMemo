package com.example.audiomemo.features.transcript.domain.repository

import com.example.audiomemo.features.transcript.domain.model.Transcript
import kotlinx.coroutines.flow.Flow

interface TranscriptRepository {
    fun getTranscriptsForSession(sessionId: Long): Flow<List<Transcript>>
    suspend fun insert(transcript: Transcript): Long
    suspend fun getByChunk(sessionId: Long, chunkIndex: Int): Transcript?
    suspend fun deleteForSession(sessionId: Long)
}
