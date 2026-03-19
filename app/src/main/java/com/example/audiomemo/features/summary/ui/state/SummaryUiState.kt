package com.example.audiomemo.features.summary.ui.state

import com.example.audiomemo.features.summary.domain.model.Summary

sealed class SummaryUiState {
    object Loading : SummaryUiState()
    data class Success(val summary: Summary) : SummaryUiState()
    object Failed : SummaryUiState()
}
