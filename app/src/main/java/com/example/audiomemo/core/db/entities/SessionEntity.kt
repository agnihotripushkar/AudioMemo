package com.example.audiomemo.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.audiomemo.features.transcript.domain.model.SessionState

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val state: SessionState,
    val startTime: Long,
    val totalDuration: Long = 0L
)
