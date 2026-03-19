package com.example.audiomemo.features.transcript.domain.model

enum class ChunkStatus { PENDING, UPLOADING, DONE, FAILED }

data class Chunk(
    val id: Long = 0,
    val sessionId: Long,
    val chunkIndex: Int,
    val filePath: String,
    val status: ChunkStatus,
    val overlapMs: Int = 0
)
