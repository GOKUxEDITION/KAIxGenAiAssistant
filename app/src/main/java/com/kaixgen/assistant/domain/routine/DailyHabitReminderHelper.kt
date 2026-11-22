package com.kaixgen.assistant.domain.routine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock

/**
 * Play-compliant real-life helpers:
 * - Schedule Water / Medicine / Study alarms via standard AlarmClock Intent
 * - Open pre-filled India Emergency Dialer (112 National Emergency / 108 Ambulance / 1091 Women Helpline)
 */
object DailyHabitReminderHelper {

    fun scheduleDailyAlarm(context: Context, label: String, hour: Int, minute: Int): Boolean {
        return runCatching {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_HOUR, hour.coerceIn(0, 23))
                putExtra(AlarmClock.EXTRA_MINUTES, minute.coerceIn(0, 59))
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openEmergencyDialer(context: Context, helplineNumber: String = "112"): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$helplineNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
