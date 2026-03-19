package com.example.audiomemo.features.transcript.domain.model

data class Transcript(
    val id: Long = 0,
    val sessionId: Long,
    val chunkIndex: Int,
    val text: String,
    val createdAt: Long
)
