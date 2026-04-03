package com.example.audiomemo.features.transcript.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.audiomemo.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "audio_recording_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shown while AudioMemo is recording audio"
            }
            notificationManager(context).createNotificationChannel(channel)
        }
    }

    // ── Recording states ───────────────────────────────────────────────────────

    fun buildForegroundNotification(context: Context, stopIntent: PendingIntent): Notification =
        buildOngoing(context, "Recording in progress...", stopIntent = stopIntent)

    fun buildPausedPhoneCallNotification(
        context: Context,
        resumeIntent: PendingIntent,
        stopIntent: PendingIntent
    ): Notification = buildPaused(context, "Paused \u2013 Phone call", resumeIntent, stopIntent)

    fun buildPausedAudioFocusNotification(
        context: Context,
        resumeIntent: PendingIntent,
        stopIntent: PendingIntent
    ): Notification = buildPaused(context, "Paused \u2013 Audio focus lost", resumeIntent, stopIntent)

    fun buildPausedMediaButtonNotification(
        context: Context,
        resumeIntent: PendingIntent,
        stopIntent: PendingIntent
    ): Notification = buildPaused(context, "Paused \u2013 Headset button", resumeIntent, stopIntent)

    fun buildPausedMicMutedNotification(
        context: Context,
        resumeIntent: PendingIntent,
        stopIntent: PendingIntent
    ): Notification = buildPaused(context, "Paused \u2013 Microphone muted", resumeIntent, stopIntent)

    fun buildMicSourceChangedNotification(
        context: Context,
        sourceName: String,
        stopIntent: PendingIntent
    ): Notification = buildOngoing(
        context,
        "Microphone switched to $sourceName",
        stopIntent = stopIntent
    )

    fun buildSilenceWarningNotification(
        context: Context,
        stopIntent: PendingIntent
    ): Notification = buildOngoing(
        context,
        "No audio detected \u2013 Check microphone",
        stopIntent = stopIntent
    )

    fun buildBatteryLowNotification(context: Context): Notification {
        val contentIntent = contentPendingIntent(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AudioMemo")
            .setContentText("Recording stopped \u2013 Battery too low")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            .setSilent(false)
            .setAutoCancel(true)
            .build()
    }

    fun buildPermissionRevokedNotification(context: Context): Notification {
        val contentIntent = contentPendingIntent(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AudioMemo")
            .setContentText("Recording stopped \u2013 Microphone permission was revoked")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            .setSilent(false)
            .setAutoCancel(true)
            .build()
    }

    fun buildHardwareErrorNotification(context: Context): Notification {
        val contentIntent = contentPendingIntent(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AudioMemo")
            .setContentText("Recording stopped \u2013 Microphone error")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            .setSilent(false)
            .setAutoCancel(true)
            .build()
    }

    fun buildLowStorageNotification(context: Context): Notification {
        val contentIntent = contentPendingIntent(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AudioMemo")
            .setContentText("Recording stopped \u2013 Low storage")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            .setSilent(false)
            .setAutoCancel(true)
            .build()
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    fun updateNotification(context: Context, notification: Notification) {
        notificationManager(context).notify(NOTIFICATION_ID, notification)
    }

    private fun buildOngoing(
        context: Context,
        text: String,
        stopIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AudioMemo")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentPendingIntent(context))
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .build()
    }

    private fun buildPaused(
        context: Context,
        text: String,
        resumeIntent: PendingIntent,
        stopIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AudioMemo")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentPendingIntent(context))
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_play, "Resume", resumeIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .build()
    }

    private fun contentPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
