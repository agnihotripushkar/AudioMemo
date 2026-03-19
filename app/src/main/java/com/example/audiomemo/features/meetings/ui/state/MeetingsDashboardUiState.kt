package com.example.audiomemo.features.meetings.ui.state

import com.example.audiomemo.features.meetings.ui.MeetingListItem

sealed interface MeetingsDashboardUiState {
    data object Loading : MeetingsDashboardUiState
    data class Success(val meetings: List<MeetingListItem>) : MeetingsDashboardUiState
}
