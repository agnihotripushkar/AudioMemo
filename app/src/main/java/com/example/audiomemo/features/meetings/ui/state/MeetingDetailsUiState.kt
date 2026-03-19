package com.example.audiomemo.features.meetings.ui.state

import com.example.audiomemo.features.summary.domain.model.Summary
import com.example.audiomemo.features.transcript.domain.model.Transcript

sealed interface MeetingDetailsUiState {
    data object Loading : MeetingDetailsUiState
    data class Success(
        val sessionTitle: String,
        val startTime: Long,
        val durationMs: Long,
        val transcripts: List<Transcript>,
        val summary: Summary?
    ) : MeetingDetailsUiState
}
