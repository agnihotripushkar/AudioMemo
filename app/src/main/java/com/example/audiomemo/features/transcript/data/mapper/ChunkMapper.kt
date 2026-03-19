package com.example.audiomemo.features.transcript.data.mapper

import com.example.audiomemo.data.db.entities.ChunkEntity
import com.example.audiomemo.features.transcript.domain.model.Chunk

fun ChunkEntity.toDomain() = Chunk(
    id = id,
    sessionId = sessionId,
    chunkIndex = chunkIndex,
    filePath = filePath,
    status = status,
    overlapMs = overlapMs
)

fun Chunk.toEntity() = ChunkEntity(
    id = id,
    sessionId = sessionId,
    chunkIndex = chunkIndex,
    filePath = filePath,
    status = status,
    overlapMs = overlapMs
)
