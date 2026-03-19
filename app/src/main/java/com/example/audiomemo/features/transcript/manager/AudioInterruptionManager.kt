package com.example.audiomemo.features.transcript.manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Handles all audio interruption edge cases:
 *  1. Phone call (RINGING / OFFHOOK) → pause; IDLE → resume
 *  2. Audio focus loss (another app takes focus) → pause; AUDIOFOCUS_GAIN → resume
 *  3. Wired headset plug/unplug and ACTION_AUDIO_BECOMING_NOISY → source-changed notification
 */
class AudioInterruptionManager(
    private val context: Context,
    private val onPauseRequested: (PauseReason) -> Unit,
    private val onResumeRequested: () -> Unit,
    private val onSourceChanged: (sourceName: String) -> Unit
) {
    enum class PauseReason { PHONE_CALL, AUDIO_FOCUS }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @Volatile private var pausedForCall = false
    @Volatile private var pausedForFocus = false

    // ── Audio Focus ────────────────────────────────────────────────────────────

    private var focusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (!pausedForFocus) {
                    pausedForFocus = true
                    if (!pausedForCall) onPauseRequested(PauseReason.AUDIO_FOCUS)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (pausedForFocus) {
                    pausedForFocus = false
                    if (!pausedForCall) onResumeRequested()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .setWillPauseWhenDucked(true)
                .build()
            focusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(focusChangeListener)
        }
        focusRequest = null
    }

    // ── Phone State ────────────────────────────────────────────────────────────

    private var legacyPhoneListener: PhoneStateListener? = null
    private var modernPhoneCallback: TelephonyCallback? = null

    private fun registerPhoneStateListener() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerModernCallback()
        } else {
            registerLegacyListener()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun registerModernCallback() {
        val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) = handleCallState(state)
        }
        modernPhoneCallback = cb
        telephonyManager.registerTelephonyCallback(context.mainExecutor, cb)
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun registerLegacyListener() {
        val listener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) =
                handleCallState(state)
        }
        legacyPhoneListener = listener
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun handleCallState(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING,
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (!pausedForCall) {
                    pausedForCall = true
                    onPauseRequested(PauseReason.PHONE_CALL)
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (pausedForCall) {
                    pausedForCall = false
                    if (!pausedForFocus) onResumeRequested()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun unregisterPhoneStateListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modernPhoneCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
                modernPhoneCallback = null
            }
        } else {
            legacyPhoneListener?.let {
                telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
                legacyPhoneListener = null
            }
        }
    }

    // ── Headset / Bluetooth source changes ────────────────────────────────────

    private var headsetReceiverRegistered = false

    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    onSourceChanged("device microphone")
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    if (isInitialStickyBroadcast) return
                    val state = intent.getIntExtra("state", -1)
                    val name = intent.getStringExtra("name") ?: "headset"
                    when (state) {
                        1 -> onSourceChanged("wired $name")
                        0 -> onSourceChanged("device microphone")
                    }
                }
            }
        }
    }

    private fun registerHeadsetReceiver() {
        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(headsetReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(headsetReceiver, filter)
        }
        headsetReceiverRegistered = true
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    fun start() {
        requestAudioFocus()
        registerPhoneStateListener()
        registerHeadsetReceiver()
    }

    fun stop() {
        abandonAudioFocus()
        unregisterPhoneStateListener()
        if (headsetReceiverRegistered) {
            try { context.unregisterReceiver(headsetReceiver) } catch (_: Exception) {}
            headsetReceiverRegistered = false
        }
    }
}
