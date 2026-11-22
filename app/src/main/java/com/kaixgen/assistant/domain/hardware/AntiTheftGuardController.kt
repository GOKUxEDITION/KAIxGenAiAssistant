package com.kaixgen.assistant.domain.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * PRD F-FP-14 Real-Life Anti-Theft Guard:
 * 1. Charger-Removal Alarm: triggers loud siren + strobe if someone unplugs your phone while charging
 * 2. Motion/Pocket-Snatch Alarm: triggers alarm if sudden snatch acceleration (> 22 m/s^2) is detected
 */
class AntiTheftGuardController(
    private val context: Context,
    private val alarmEngine: FindPhoneAlarmEngine
) : SensorEventListener {

    private val _isChargerGuardArmed = MutableStateFlow(false)
    val isChargerGuardArmed: StateFlow<Boolean> = _isChargerGuardArmed.asStateFlow()

    private val _isMotionGuardArmed = MutableStateFlow(false)
    val isMotionGuardArmed: StateFlow<Boolean> = _isMotionGuardArmed.asStateFlow()

    private var powerReceiver: BroadcastReceiver? = null
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    fun setChargerGuardArmed(armed: Boolean) {
        if (_isChargerGuardArmed.value == armed) return
        _isChargerGuardArmed.value = armed
        if (armed) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_POWER_DISCONNECTED && _isChargerGuardArmed.value) {
                        alarmEngine.startAlarm(enableFlashlight = true, enableVibrate = true)
                    }
                }
            }
            powerReceiver = receiver
            runCatching {
                context.registerReceiver(receiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
            }
        } else {
            powerReceiver?.let { runCatching { context.unregisterReceiver(it) } }
            powerReceiver = null
            alarmEngine.stopAlarm()
        }
    }

    fun setMotionGuardArmed(armed: Boolean) {
        if (_isMotionGuardArmed.value == armed) return
        _isMotionGuardArmed.value = armed
        if (armed) {
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accel != null) {
                sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            sensorManager?.unregisterListener(this)
            alarmEngine.stopAlarm()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!_isMotionGuardArmed.value || event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        // Standard gravity is ~9.8 m/s^2; sudden snatch exceeds ~22 m/s^2
        if (magnitude > 22.0f) {
            alarmEngine.startAlarm(enableFlashlight = true, enableVibrate = true)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun disarmAll() {
        setChargerGuardArmed(false)
        setMotionGuardArmed(false)
    }
}
