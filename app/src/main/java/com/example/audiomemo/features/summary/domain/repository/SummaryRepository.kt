package com.example.audiomemo.features.summary.domain.repository

import com.example.audiomemo.features.summary.domain.model.Summary
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    suspend fun insert(summary: Summary): Long
    suspend fun update(summary: Summary)
    fun getSummaryForSession(sessionId: Long): Flow<Summary?>
    suspend fun updateStatus(sessionId: Long, status: SummaryStatus)
    suspend fun deleteForSession(sessionId: Long)
}
