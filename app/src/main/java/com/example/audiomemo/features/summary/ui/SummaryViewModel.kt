package com.example.audiomemo.features.summary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.summary.domain.repository.SummaryRepository
import com.example.audiomemo.features.summary.ui.state.SummaryUiState
import com.example.audiomemo.features.summary.data.worker.SummaryGenerationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val sessionIdFlow = MutableStateFlow(-1L)

    fun setSessionId(id: Long) {
        sessionIdFlow.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SummaryUiState> = sessionIdFlow
        .flatMapLatest { id ->
            if (id < 0L) flowOf(null)
            else summaryRepository.getSummaryForSession(id)
        }
        .map { summary ->
            when {
                summary == null
                    || summary.status == SummaryStatus.PENDING
                    || summary.status == SummaryStatus.GENERATING -> SummaryUiState.Loading
                summary.status == SummaryStatus.FAILED -> SummaryUiState.Failed
                else -> SummaryUiState.Success(summary)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryUiState.Loading)

    fun retrySummary() {
        val sessionId = sessionIdFlow.value
        if (sessionId < 0L) return
        viewModelScope.launch {
            val existing = summaryRepository.getSummaryForSession(sessionId).first()
            if (existing != null) {
                summaryRepository.updateStatus(sessionId, SummaryStatus.PENDING)
            }
            workManager.enqueue(
                OneTimeWorkRequestBuilder<SummaryGenerationWorker>()
                    .setInputData(workDataOf(SummaryGenerationWorker.KEY_SESSION_ID to sessionId))
                    .build()
            )
        }
    }
}
