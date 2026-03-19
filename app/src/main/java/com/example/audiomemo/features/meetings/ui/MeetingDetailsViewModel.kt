package com.example.audiomemo.features.meetings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiomemo.features.meetings.ui.state.MeetingDetailsUiState
import com.example.audiomemo.features.summary.domain.repository.SummaryRepository
import com.example.audiomemo.features.transcript.domain.repository.SessionRepository
import com.example.audiomemo.features.transcript.domain.repository.TranscriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MeetingDetailsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val transcriptRepository: TranscriptRepository,
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val sessionIdFlow = MutableStateFlow(-1L)

    fun setSessionId(id: Long) {
        sessionIdFlow.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MeetingDetailsUiState> = sessionIdFlow
        .flatMapLatest { id ->
            if (id < 0L) flowOf(MeetingDetailsUiState.Loading)
            else {
                val transcriptFlow = transcriptRepository.getTranscriptsForSession(id)
                val summaryFlow = summaryRepository.getSummaryForSession(id)
                combine(transcriptFlow, summaryFlow) { transcripts, summary ->
                    val session = sessionRepository.getById(id)
                    if (session == null) {
                        MeetingDetailsUiState.Loading
                    } else {
                        val title = summary?.title?.takeIf { it.isNotBlank() }
                            ?: buildDefaultTitle(session.startTime)
                        MeetingDetailsUiState.Success(
                            sessionTitle = title,
                            startTime = session.startTime,
                            durationMs = session.totalDuration,
                            transcripts = transcripts,
                            summary = summary
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeetingDetailsUiState.Loading)

    private fun buildDefaultTitle(startTimeMs: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return "Meeting – ${formatter.format(Date(startTimeMs))}"
    }
}
