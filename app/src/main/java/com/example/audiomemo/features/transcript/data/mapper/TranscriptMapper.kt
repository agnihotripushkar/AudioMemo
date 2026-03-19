package com.example.audiomemo.features.transcript.data.mapper

import com.example.audiomemo.data.db.entities.TranscriptEntity
import com.example.audiomemo.features.transcript.domain.model.Transcript

fun TranscriptEntity.toDomain() = Transcript(
    id = id,
    sessionId = sessionId,
    chunkIndex = chunkIndex,
    text = text,
    createdAt = createdAt
)

fun Transcript.toEntity() = TranscriptEntity(
    id = id,
    sessionId = sessionId,
    chunkIndex = chunkIndex,
    text = text,
    createdAt = createdAt
)
