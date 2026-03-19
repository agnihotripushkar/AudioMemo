package com.example.audiomemo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.audiomemo.data.db.entities.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {

    @Insert
    suspend fun insert(transcript: TranscriptEntity): Long

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    fun getTranscriptsForSession(sessionId: Long): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId AND chunkIndex = :chunkIndex")
    suspend fun getByChunk(sessionId: Long, chunkIndex: Int): TranscriptEntity?

    @Query("DELETE FROM transcripts WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Long)
}