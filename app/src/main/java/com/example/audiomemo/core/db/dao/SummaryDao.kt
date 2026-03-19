package com.example.audiomemo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.audiomemo.data.db.entities.SummaryEntity
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    @Insert
    suspend fun insert(summary: SummaryEntity): Long

    @Update
    suspend fun update(summary: SummaryEntity)

    @Query("SELECT * FROM summaries WHERE sessionId = :sessionId LIMIT 1")
    fun getSummaryForSession(sessionId: Long): Flow<SummaryEntity?>

    @Query("UPDATE summaries SET status = :status WHERE sessionId = :sessionId")
    suspend fun updateStatus(sessionId: Long, status: SummaryStatus)

    @Query("DELETE FROM summaries WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Long)
}