package com.kaixgen.assistant.domain.hardware

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FindPhoneAlarmEngine(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var mediaPlayer: MediaPlayer? = null
    private var strobeJob: Job? = null
    private var autoStopJob: Job? = null
    private var previousAlarmVolume: Int? = null

    private val _isRinging = MutableStateFlow(false)
    val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

    @Synchronized
    fun startAlarm(
        enableFlashlight: Boolean = true,
        enableVibrate: Boolean = true,
        maxDurationMs: Long = 60_000L
    ) {
        if (_isRinging.value) return
        _isRinging.value = true

        startSiren()
        if (enableFlashlight) startFlashlightStrobe()
        if (enableVibrate) startVibration()

        autoStopJob?.cancel()
        autoStopJob = scope.launch {
            delay(maxDurationMs)
            stopAlarm()
        }
    }

    @Synchronized
    fun stopAlarm() {
        if (!_isRinging.value) return
        _isRinging.value = false

        autoStopJob?.cancel()
        autoStopJob = null

        strobeJob?.cancel()
        strobeJob = null
        setTorchSafe(false)

        stopVibration()
        stopSiren()
    }

    private fun startSiren() {
        runCatching {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            previousAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        }
    }

    private fun stopSiren() {
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            previousAlarmVolume?.let { prev ->
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, prev, 0)
            }
            previousAlarmVolume = null
        }
    }

    private fun startFlashlightStrobe() {
        strobeJob?.cancel()
        strobeJob = scope.launch {
            var torchOn = false
            while (isActive && _isRinging.value) {
                torchOn = !torchOn
                setTorchSafe(torchOn)
                delay(280L)
            }
            setTorchSafe(false)
        }
    }

    private fun setTorchSafe(enabled: Boolean) {
        runCatching {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val backCameraId = cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facingBack = chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                hasFlash && facingBack
            } ?: return
            cm.setTorchMode(backCameraId, enabled)
        }
    }

    private fun startVibration() {
        runCatching {
            val vibrator = getVibrator() ?: return
            val timings = longArrayOf(0, 400, 200, 400, 200, 700, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, 0)
            }
        }
    }

    private fun stopVibration() {
        runCatching { getVibrator()?.cancel() }
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
