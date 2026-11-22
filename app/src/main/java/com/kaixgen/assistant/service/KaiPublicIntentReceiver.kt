package com.kaixgen.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaixgen.assistant.KaiApplication

class KaiPublicIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? KaiApplication ?: return
        when (intent.action) {
            ACTION_SPEAK -> {
                val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
                val lang = intent.getStringExtra(EXTRA_LANG) ?: "hi-IN"
                if (text.isNotBlank()) {
                    app.appContainer.ttsManager.speak(text, languageCode = lang)
                }
            }
            ACTION_FIND_PHONE_RING -> {
                val cfg = app.appContainer.localRepository.findPhoneConfig.value
                app.findPhoneAlarmEngine.startAlarm(
                    enableFlashlight = cfg.isFlashlightStrobeEnabled,
                    enableVibrate = cfg.isVibrateEnabled
                )
            }
            ACTION_STOP_RING -> {
                app.findPhoneAlarmEngine.stopAlarm()
            }
        }
    }

    companion object {
        const val ACTION_SPEAK = "com.kai.action.SPEAK"
        const val ACTION_LOCK_NOW = "com.kai.action.LOCK_NOW"
        const val ACTION_FIND_PHONE_RING = "com.kai.action.FIND_PHONE_RING"
        const val ACTION_STOP_RING = "com.kai.action.STOP_RING"
        const val EXTRA_TEXT = "text"
        const val EXTRA_LANG = "lang"
    }
}
