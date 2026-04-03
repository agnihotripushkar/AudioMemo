package com.example.audiomemo.features.transcript.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * Persists and reads the "Wi-Fi only uploads" user preference.
 *
 * When enabled, [WhisperUploadWorker] and [TranscriptRetryWorker] are constrained to
 * [NetworkType.UNMETERED] so uploads are deferred until a Wi-Fi connection is available.
 * When disabled (the default), any connected network is accepted.
 *
 * The preference is stored in SharedPreferences and read by WorkManager at enqueue time.
 * Existing enqueued work is not retroactively updated; the constraint applies to all work
 * enqueued after the preference is changed.
 */
object UploadPreferences {
    private const val PREFS_NAME = "upload_prefs"
    const val KEY_WIFI_ONLY = "wifi_only_uploads"

    fun isWifiOnly(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WIFI_ONLY, false)

    fun setWifiOnly(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
    }

    /** Returns the [Constraints] that should be applied to every upload work request. */
    fun networkConstraints(context: Context): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(
                if (isWifiOnly(context)) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()
}
