package com.example.audiomemo.features.transcript.manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
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
 *  4. Mic hardware mute toggle (API 27+) → pause; unmute → resume
 *  5. Bluetooth SCO connect/disconnect → source-changed notification + SCO lifecycle
 *  6. USB audio device attach/detach → source-changed notification
 */
class AudioInterruptionManager(
    private val context: Context,
    private val onPauseRequested: (PauseReason) -> Unit,
    private val onResumeRequested: () -> Unit,
    private val onSourceChanged: (sourceName: String) -> Unit
) {
    enum class PauseReason { PHONE_CALL, AUDIO_FOCUS, MIC_MUTED }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @Volatile private var pausedForCall = false
    @Volatile private var pausedForFocus = false
    @Volatile private var pausedForMicMute = false

    private fun shouldResume() = !pausedForCall && !pausedForFocus && !pausedForMicMute

    // ── Audio Focus ────────────────────────────────────────────────────────────

    private var focusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (!pausedForFocus) {
                    pausedForFocus = true
                    if (!pausedForCall && !pausedForMicMute) onPauseRequested(PauseReason.AUDIO_FOCUS)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (pausedForFocus) {
                    pausedForFocus = false
                    if (shouldResume()) onResumeRequested()
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
                    if (shouldResume()) {
                        onResumeRequested()
                    } else if (pausedForFocus) {
                        // The ringtone or phone app may have taken audio focus before the
                        // call connected and may never return it. Re-request focus so the
                        // system re-evaluates; if granted, focusChangeListener will resume.
                        abandonAudioFocus()
                        requestAudioFocus()
                    }
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

    // ── Mic hardware mute toggle (API 27+) ────────────────────────────────────

    private var micMuteReceiverRegistered = false

    private val micMuteReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val muted = audioManager.isMicrophoneMute
            if (muted && !pausedForMicMute) {
                pausedForMicMute = true
                if (!pausedForCall && !pausedForFocus) onPauseRequested(PauseReason.MIC_MUTED)
            } else if (!muted && pausedForMicMute) {
                pausedForMicMute = false
                if (shouldResume()) onResumeRequested()
            }
        }
    }

    private fun registerMicMuteReceiver() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return // API 27+
        val filter = IntentFilter(AudioManager.ACTION_MICROPHONE_MUTE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(micMuteReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(micMuteReceiver, filter)
        }
        micMuteReceiverRegistered = true
        // Check initial state in case mic was already muted when recording started
        if (audioManager.isMicrophoneMute) {
            pausedForMicMute = true
            if (!pausedForCall && !pausedForFocus) onPauseRequested(PauseReason.MIC_MUTED)
        }
    }

    private fun unregisterMicMuteReceiver() {
        if (micMuteReceiverRegistered) {
            try { context.unregisterReceiver(micMuteReceiver) } catch (_: Exception) {}
            micMuteReceiverRegistered = false
        }
    }

    // ── Bluetooth SCO ──────────────────────────────────────────────────────────

    private var scoReceiverRegistered = false

    private val scoReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val state = intent?.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1) ?: return
            when (state) {
                AudioManager.SCO_AUDIO_STATE_CONNECTED ->
                    onSourceChanged("Bluetooth headset")
                AudioManager.SCO_AUDIO_STATE_DISCONNECTED,
                AudioManager.SCO_AUDIO_STATE_ERROR -> {
                    audioManager.stopBluetoothSco()
                    onSourceChanged("device microphone")
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun registerScoReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(scoReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(scoReceiver, filter)
        }
        scoReceiverRegistered = true
        // Start SCO if a Bluetooth SCO input device is already connected
        val hasBtSco = audioManager
            .getDevices(AudioManager.GET_DEVICES_INPUTS)
            .any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
        if (hasBtSco) {
            audioManager.startBluetoothSco()
        }
    }

    @Suppress("DEPRECATION")
    private fun unregisterScoReceiver() {
        if (scoReceiverRegistered) {
            try { context.unregisterReceiver(scoReceiver) } catch (_: Exception) {}
            scoReceiverRegistered = false
        }
        audioManager.stopBluetoothSco()
    }

    // ── USB audio device attach/detach ─────────────────────────────────────────

    private var usbReceiverRegistered = false

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    // Only notify if the newly attached device has an audio input
                    val hasUsbInput = audioManager
                        .getDevices(AudioManager.GET_DEVICES_INPUTS)
                        .any {
                            it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                        }
                    if (hasUsbInput) onSourceChanged("USB microphone")
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    // If the detached device was an audio input, fall back to device mic
                    val stillHasUsbInput = audioManager
                        .getDevices(AudioManager.GET_DEVICES_INPUTS)
                        .any {
                            it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                        }
                    if (!stillHasUsbInput) onSourceChanged("device microphone")
                }
            }
        }
    }

    private fun registerUsbReceiver() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        // USB attach/detach are system broadcasts — RECEIVER_NOT_EXPORTED is fine.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
        usbReceiverRegistered = true
    }

    private fun unregisterUsbReceiver() {
        if (usbReceiverRegistered) {
            try { context.unregisterReceiver(usbReceiver) } catch (_: Exception) {}
            usbReceiverRegistered = false
        }
    }

    // ── Audio device add/remove (API 23+) ─────────────────────────────────────

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            val micDevice = addedDevices.firstOrNull { it.isMicInput() }
            if (micDevice != null) onSourceChanged(micDevice.toSourceName())
        }
        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            val micRemoved = removedDevices.any { it.isMicInput() }
            if (micRemoved) {
                val remaining = audioManager
                    .getDevices(AudioManager.GET_DEVICES_INPUTS)
                    .firstOrNull { it.isMicInput() }
                onSourceChanged(remaining?.toSourceName() ?: "device microphone")
            }
        }
    }

    private fun AudioDeviceInfo.isMicInput(): Boolean = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> true
        else -> false
    }

    private fun AudioDeviceInfo.toSourceName(): String = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC   -> "device microphone"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "wired headset"
        AudioDeviceInfo.TYPE_USB_HEADSET   -> "USB headset"
        AudioDeviceInfo.TYPE_USB_DEVICE    -> "USB microphone"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth headset"
        else -> productName?.toString()?.takeIf { it.isNotBlank() } ?: "external microphone"
    }

    private fun registerAudioDeviceCallback() {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }

    private fun unregisterAudioDeviceCallback() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    fun start() {
        requestAudioFocus()
        registerPhoneStateListener()
        registerHeadsetReceiver()
        registerMicMuteReceiver()
        registerScoReceiver()
        registerUsbReceiver()
        registerAudioDeviceCallback()
    }

    fun stop() {
        abandonAudioFocus()
        unregisterPhoneStateListener()
        if (headsetReceiverRegistered) {
            try { context.unregisterReceiver(headsetReceiver) } catch (_: Exception) {}
            headsetReceiverRegistered = false
        }
        unregisterMicMuteReceiver()
        unregisterScoReceiver()
        unregisterUsbReceiver()
        unregisterAudioDeviceCallback()
    }
}
