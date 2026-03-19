package com.example.audiomemo.features.meetings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiomemo.features.meetings.ui.state.MeetingsDashboardUiState
import com.example.audiomemo.features.summary.domain.repository.SummaryRepository
import com.example.audiomemo.features.transcript.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MeetingsDashboardViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MeetingsDashboardUiState> = sessionRepository
        .getAllSessions()
        .flatMapLatest { sessions ->
            if (sessions.isEmpty()) {
                flowOf(emptyList<MeetingListItem>())
            } else {
                val summaryFlows = sessions.map { session ->
                    summaryRepository.getSummaryForSession(session.id).map { summary ->
                        val title = summary?.title?.takeIf { it.isNotBlank() }
                            ?: buildDefaultTitle(session.startTime)
                        MeetingListItem(
                            sessionId = session.id,
                            title = title,
                            startTime = session.startTime,
                            durationMs = session.totalDuration,
                            sessionState = session.state,
                            summaryStatus = summary?.status
                        )
                    }
                }
                combine(summaryFlows) { it.toList() }
                    .map { items -> items.sortedByDescending { it.startTime } }
            }
        }
        .map<List<MeetingListItem>, MeetingsDashboardUiState> { meetings ->
            MeetingsDashboardUiState.Success(meetings)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeetingsDashboardUiState.Loading)

    private fun buildDefaultTitle(startTimeMs: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return "Meeting – ${formatter.format(Date(startTimeMs))}"
    }
}
