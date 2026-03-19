package com.example.audiomemo.features.summary.data.repository

import com.example.audiomemo.data.db.dao.SummaryDao
import com.example.audiomemo.features.summary.data.mapper.toDomain
import com.example.audiomemo.features.summary.data.mapper.toEntity
import com.example.audiomemo.features.summary.domain.model.Summary
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.summary.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SummaryRepositoryImpl @Inject constructor(
    private val summaryDao: SummaryDao
) : SummaryRepository {

    override suspend fun insert(summary: Summary): Long =
        summaryDao.insert(summary.toEntity())

    override suspend fun update(summary: Summary) =
        summaryDao.update(summary.toEntity())

    override fun getSummaryForSession(sessionId: Long): Flow<Summary?> =
        summaryDao.getSummaryForSession(sessionId).map { it?.toDomain() }

    override suspend fun updateStatus(sessionId: Long, status: SummaryStatus) =
        summaryDao.updateStatus(sessionId, status)

    override suspend fun deleteForSession(sessionId: Long) =
        summaryDao.deleteForSession(sessionId)
}
