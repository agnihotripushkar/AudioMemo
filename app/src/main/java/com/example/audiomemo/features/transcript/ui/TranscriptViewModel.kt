package com.example.audiomemo.features.transcript.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiomemo.features.transcript.util.TranscriptStitcher
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import com.example.audiomemo.features.transcript.domain.repository.ChunkRepository
import com.example.audiomemo.features.transcript.domain.repository.TranscriptRepository
import com.example.audiomemo.features.transcript.ui.state.TranscriptUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TranscriptViewModel @Inject constructor(
    private val transcriptRepository: TranscriptRepository,
    private val chunkRepository: ChunkRepository
) : ViewModel() {

    private val stitcher = TranscriptStitcher()
    private val _isRecording = MutableStateFlow(true)
    private val _sessionId = MutableStateFlow(-1L)

    fun onRecordingStopped(sessionId: Long) {
        if (sessionId > 0L) _sessionId.value = sessionId
        _isRecording.value = false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stitchedTranscript = _sessionId
        .flatMapLatest { id ->
            if (id < 0L) flowOf(emptyList())
            else transcriptRepository.getTranscriptsForSession(id)
        }
        .map { transcripts -> stitcher.stitch(transcripts) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val isTranscriptionComplete = _sessionId
        .flatMapLatest { id ->
            if (id < 0L) flowOf(emptyList())
            else chunkRepository.getChunksForSession(id)
        }
        .map { chunks ->
            chunks.isNotEmpty() && chunks.all {
                it.status == ChunkStatus.DONE || it.status == ChunkStatus.FAILED
            }
        }

    val uiState: StateFlow<TranscriptUiState> = combine(
        _isRecording,
        stitchedTranscript,
        isTranscriptionComplete
    ) { isRecording, transcript, complete ->
        if (isRecording) {
            TranscriptUiState.Recording
        } else {
            TranscriptUiState.PostRecording(
                sessionId = _sessionId.value,
                transcript = transcript,
                isTranscriptionComplete = complete
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TranscriptUiState.Recording)
}
