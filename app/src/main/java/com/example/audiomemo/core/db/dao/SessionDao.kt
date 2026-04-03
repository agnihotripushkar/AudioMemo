package com.example.audiomemo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.audiomemo.data.db.entities.SessionEntity
import com.example.audiomemo.features.transcript.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("UPDATE sessions SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: SessionState)

    @Query("UPDATE sessions SET totalDuration = :duration WHERE id = :id")
    suspend fun updateDuration(id: Long, duration: Long)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Returns the most recent session that was not stopped cleanly — used for crash recovery.
     *
     * WARNING: On process restart, sessions left in RECORDING state are ghost sessions (the
     * recorder is no longer running). Call [abandonOrphanedSessions] before this to promote
     * those to PAUSED so they are recovered correctly rather than treated as live sessions.
     */
    @Query("SELECT * FROM sessions WHERE state != 'STOPPED' ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastActiveSession(): SessionEntity?

    /**
     * Marks any session still in RECORDING state as PAUSED.
     * Called at the start of each new session to clean up ghost sessions left by a process crash
     * or kill — the recorder was no longer running for those sessions, so RECORDING is incorrect.
     */
    @Query("UPDATE sessions SET state = 'PAUSED' WHERE state = 'RECORDING'")
    suspend fun abandonOrphanedSessions()
}