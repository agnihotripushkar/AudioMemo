package com.example.audiomemo.features.transcript.data.mapper

import com.example.audiomemo.data.db.entities.SessionEntity
import com.example.audiomemo.features.transcript.domain.model.Session

fun SessionEntity.toDomain() = Session(
    id = id,
    state = state,
    startTime = startTime,
    totalDuration = totalDuration
)

fun Session.toEntity() = SessionEntity(
    id = id,
    state = state,
    startTime = startTime,
    totalDuration = totalDuration
)
