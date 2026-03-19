package com.example.audiomemo.features.transcript.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Monitors the MediaRecorder amplitude stream. After [SILENCE_TIMEOUT_MS] of continuous
 * silence (amplitude below [SILENCE_THRESHOLD]), fires [onSilenceDetected] once per
 * silence period so the service can show a "No audio detected – Check microphone" warning.
 */
class SilenceDetector(
    private val amplitude: StateFlow<Int>,
    private val onSilenceDetected: () -> Unit
) {
    companion object {
        private const val SILENCE_THRESHOLD = 500
        private const val SILENCE_TIMEOUT_MS = 10_000L
        private const val CHECK_INTERVAL_MS = 1_000L
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private var silentDurationMs = 0L
    private var warningEmitted = false

    fun start() {
        silentDurationMs = 0L
        warningEmitted = false
        job = scope.launch {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                if (amplitude.value < SILENCE_THRESHOLD) {
                    silentDurationMs += CHECK_INTERVAL_MS
                    if (silentDurationMs >= SILENCE_TIMEOUT_MS && !warningEmitted) {
                        warningEmitted = true
                        onSilenceDetected()
                    }
                } else {
                    silentDurationMs = 0L
                    warningEmitted = false
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    /** Reset counters when recording resumes so the 10-second window restarts. */
    fun reset() {
        silentDurationMs = 0L
        warningEmitted = false
    }
}
