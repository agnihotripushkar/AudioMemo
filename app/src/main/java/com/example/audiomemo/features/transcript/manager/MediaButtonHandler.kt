package com.example.audiomemo.features.transcript.manager

import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.view.KeyEvent

/**
 * Handles physical media button events (wired headset, Bluetooth headset controls).
 *
 * Maps button events to pause/resume actions so users can control recording
 * without touching the screen — useful in meetings.
 *
 *  - KEYCODE_HEADSETHOOK / KEYCODE_MEDIA_PLAY_PAUSE → toggle
 *  - KEYCODE_MEDIA_PAUSE → pause only
 *  - KEYCODE_MEDIA_PLAY  → resume only
 */
class MediaButtonHandler(
    private val context: Context,
    private val onToggle: () -> Unit,
    private val onPauseOnly: () -> Unit,
    private val onPlayOnly: () -> Unit
) {
    private var mediaSession: MediaSession? = null

    fun start() {
        val session = MediaSession(context, "AudioMemo")
        session.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                val keyEvent: KeyEvent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                // Only act on the DOWN event to avoid double-firing
                if (keyEvent?.action != KeyEvent.ACTION_DOWN) return false
                return when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_HEADSETHOOK,
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { onToggle(); true }
                    KeyEvent.KEYCODE_MEDIA_PAUSE      -> { onPauseOnly(); true }
                    KeyEvent.KEYCODE_MEDIA_PLAY       -> { onPlayOnly(); true }
                    else -> false
                }
            }
        })
        session.isActive = true
        mediaSession = session
    }

    fun stop() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }
}
