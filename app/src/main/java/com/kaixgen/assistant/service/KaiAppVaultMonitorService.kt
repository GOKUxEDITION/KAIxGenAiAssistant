package com.kaixgen.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.MainActivity
import com.kaixgen.assistant.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KaiAppVaultMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private val unlockedUntilMap = mutableMapOf<String, Long>()
    private var lastInterceptedPkg: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_VAULT -> {
                monitorJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_MARK_UNLOCKED -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val graceMs = intent.getLongExtra(EXTRA_GRACE_MS, 60_000L)
                if (!pkg.isNullOrBlank()) {
                    unlockedUntilMap[pkg] = System.currentTimeMillis() + graceMs
                }
                return START_STICKY
            }
        }

        ensureChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startWatchingForegroundApps()
        return START_STICKY
    }

    private fun startWatchingForegroundApps() {
        if (monitorJob?.isActive == true) return
        val app = application as? KaiApplication ?: return
        val repo = app.appContainer.repository

        monitorJob = scope.launch {
            while (isActive) {
                val topPkg = queryTopForegroundPackage()
                if (topPkg != null && topPkg != packageName) {
                    val lockedSet = repo.lockedApps.value
                        .filter { it.isLocked }
                        .map { it.packageName }
                        .toSet()

                    val now = System.currentTimeMillis()
                    val graceUntil = unlockedUntilMap[topPkg] ?: 0L
                    if (topPkg in lockedSet && now > graceUntil && topPkg != lastInterceptedPkg) {
                        lastInterceptedPkg = topPkg
                        KaiEventBus.emitEvent(KaiHardwareEvent.LockedAppIntercepted(topPkg, now))
                        launchLockOverlay(topPkg)
                    } else if (topPkg !in lockedSet) {
                        lastInterceptedPkg = null
                    }
                }
                delay(650L)
            }
        }
    }

    private fun queryTopForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val end = System.currentTimeMillis()
        val events = usm.queryEvents(end - 5_000L, end)
        val event = UsageEvents.Event()
        var latestPkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                latestPkg = event.packageName
            }
        }
        return latestPkg
    }

    private fun launchLockOverlay(targetPackage: String) {
        val lockIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_LOCK_OVERLAY_PACKAGE, targetPackage)
        }
        startActivity(lockIntent)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "KAI App Vault Guard",
                    NotificationManager.IMPORTANCE_MIN
                )
            )
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("KAI App Vault Active")
            .setContentText("Protecting your locked apps with Voice + PIN")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "kai_app_vault_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_STOP_VAULT = "com.kaixgen.assistant.action.STOP_VAULT"
        const val ACTION_MARK_UNLOCKED = "com.kaixgen.assistant.action.MARK_UNLOCKED"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_GRACE_MS = "extra_grace_ms"
        const val EXTRA_LOCK_OVERLAY_PACKAGE = "extra_lock_overlay_package"
    }
}
