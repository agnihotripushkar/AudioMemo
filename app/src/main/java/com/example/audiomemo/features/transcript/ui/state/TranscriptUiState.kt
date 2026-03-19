package com.example.audiomemo.features.transcript.ui.state

sealed class TranscriptUiState {
    object Recording : TranscriptUiState()
    data class PostRecording(
        val sessionId: Long,
        val transcript: String,
        val isTranscriptionComplete: Boolean
    ) : TranscriptUiState()
}
