package com.example.audiomemo.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiomemo.features.home.domain.HomeRepository
import com.example.audiomemo.features.meetings.ui.MeetingListItem
import com.example.audiomemo.features.summary.domain.repository.SummaryRepository
import com.example.audiomemo.features.transcript.domain.repository.ChunkRepository
import com.example.audiomemo.features.transcript.domain.repository.SessionRepository
import com.example.audiomemo.features.transcript.domain.repository.TranscriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val sessionRepository: SessionRepository,
    private val summaryRepository: SummaryRepository,
    private val chunkRepository: ChunkRepository,
    private val transcriptRepository: TranscriptRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentMeetings: StateFlow<List<MeetingListItem>> = sessionRepository
        .getAllSessions()
        .flatMapLatest { sessions ->
            val recent = sessions.sortedByDescending { it.startTime }.take(5)
            if (recent.isEmpty()) {
                flowOf(emptyList())
            } else {
                val flows = recent.map { session ->
                    summaryRepository.getSummaryForSession(session.id).map { summary ->
                        MeetingListItem(
                            sessionId = session.id,
                            title = summary?.title?.takeIf { it.isNotBlank() }
                                ?: buildDefaultTitle(session.startTime),
                            startTime = session.startTime,
                            durationMs = session.totalDuration,
                            sessionState = session.state,
                            summaryStatus = summary?.status
                        )
                    }
                }
                combine(flows) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            chunkRepository.deleteForSession(sessionId)
            transcriptRepository.deleteForSession(sessionId)
            summaryRepository.deleteForSession(sessionId)
            sessionRepository.deleteById(sessionId)
        }
    }

    private fun buildDefaultTitle(startTimeMs: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return "Recording – ${formatter.format(Date(startTimeMs))}"
    }
}
