package com.example.audiomemo.features.transcript.domain.model

enum class SessionState { RECORDING, PAUSED, STOPPED }

data class Session(
    val id: Long = 0,
    val state: SessionState,
    val startTime: Long,
    val totalDuration: Long = 0L
)
