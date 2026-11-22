package com.kaixgen.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.kaixgen.assistant.KaiApplication

class KaiBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? KaiApplication ?: return
        val lockEnabled = app.appContainer.localRepository.voiceLockConfig.value.isEnabled
        if (lockEnabled) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, KaiAppVaultMonitorService::class.java)
            )
        }
    }
}
