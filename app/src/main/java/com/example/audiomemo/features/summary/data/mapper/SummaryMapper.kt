package com.example.audiomemo.features.summary.data.mapper

import com.example.audiomemo.data.db.entities.SummaryEntity
import com.example.audiomemo.features.summary.domain.model.Summary

fun SummaryEntity.toDomain() = Summary(
    id = id,
    sessionId = sessionId,
    title = title,
    summary = summary,
    actionItems = actionItems,
    keyPoints = keyPoints,
    status = status
)

fun Summary.toEntity() = SummaryEntity(
    id = id,
    sessionId = sessionId,
    title = title,
    summary = summary,
    actionItems = actionItems,
    keyPoints = keyPoints,
    status = status
)
