package com.kaixgen.assistant.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.kaixgen.assistant.KaiApplication

@RequiresApi(Build.VERSION_CODES.N)
class KaiListenTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile(KaiEventBus.isListeningServiceRunning.value)
    }

    override fun onClick() {
        super.onClick()
        val running = KaiEventBus.isListeningServiceRunning.value
        val intent = Intent(this, KaiListeningService::class.java).apply {
            if (running) action = KaiListeningService.ACTION_STOP_LISTENING
        }
        if (running) {
            startService(intent)
        } else {
            ContextCompat.startForegroundService(this, intent)
        }
        updateTile(!running)
    }

    private fun updateTile(active: Boolean) {
        qsTile?.apply {
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = if (active) "KAI Listening" else "KAI Listen"
            updateTile()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
class KaiFindPhoneTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val app = application as? KaiApplication
        val ringing = app?.findPhoneAlarmEngine?.isRinging?.value == true
        qsTile?.apply {
            state = if (ringing) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = if (ringing) "Stop Siren" else "Find Phone"
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val app = application as? KaiApplication ?: return
        val engine = app.findPhoneAlarmEngine
        if (engine.isRinging.value) {
            engine.stopAlarm()
        } else {
            engine.startAlarm(enableFlashlight = true, enableVibrate = true)
        }
        onStartListening()
    }
}
