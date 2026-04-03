package com.example.audiomemo.features.transcript.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager

/**
 * Monitors battery state during recording.
 *
 * Fires [onBatteryLow] when:
 *  - The system broadcasts ACTION_BATTERY_LOW (battery has reached the low threshold), or
 *  - Battery Saver (power save mode) is enabled while recording, which defers WorkManager
 *    jobs and can leave chunks stuck in PENDING indefinitely.
 */
class BatteryGuard(
    private val context: Context,
    private val onBatteryLow: () -> Unit
) {
    private var receiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_LOW -> onBatteryLow()
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (pm.isPowerSaveMode) onBatteryLow()
                }
            }
        }
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        receiverRegistered = true
    }

    fun stop() {
        if (receiverRegistered) {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
            receiverRegistered = false
        }
    }
}
