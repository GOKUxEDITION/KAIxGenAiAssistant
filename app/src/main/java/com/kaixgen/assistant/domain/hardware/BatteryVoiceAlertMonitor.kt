package com.kaixgen.assistant.domain.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.kaixgen.assistant.domain.voice.KaiTtsManager

/**
 * Announces helpful Hindi/Hinglish voice alerts when phone reaches 100% Full Charge
 * (save battery health) or drops below 15% Low Battery while unplugged.
 */
class BatteryVoiceAlertMonitor(
    private val context: Context,
    private val ttsManager: KaiTtsManager
) {
    private var receiver: BroadcastReceiver? = null
    private var announcedFull = false
    private var announcedLow = false

    fun start() {
        if (receiver != null) return
        val r = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                val pct = (level * 100) / scale
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

                if (isCharging && pct >= 100 && !announcedFull) {
                    announcedFull = true
                    ttsManager.speak("Battery 100 percent full ho gayi hai, charger nikal dein.", languageCode = "hi-IN")
                } else if (pct < 98) {
                    announcedFull = false
                }

                if (!isCharging && pct in 1..15 && !announcedLow) {
                    announcedLow = true
                    ttsManager.speak("Battery sirf $pct percent bachi hai, kripya charger lagayein.", languageCode = "hi-IN")
                } else if (pct > 20) {
                    announcedLow = false
                }
            }
        }
        receiver = r
        runCatching { context.registerReceiver(r, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }
    }

    fun stop() {
        receiver?.let { runCatching { context.unregisterReceiver(it) } }
        receiver = null
    }
}
