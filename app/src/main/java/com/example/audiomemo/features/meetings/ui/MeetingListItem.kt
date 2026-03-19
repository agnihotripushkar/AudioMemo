package com.example.audiomemo.features.meetings.ui

import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState

data class MeetingListItem(
    val sessionId: Long,
    val title: String,
    val startTime: Long,
    val durationMs: Long,
    val sessionState: SessionState,
    val summaryStatus: SummaryStatus?
)
