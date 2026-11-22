package com.kaixgen.assistant.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent

/**
 * PRD F-DC-10 & F-DC-04:
 * Optional, user-consented Accessibility Service enabling true hardware Screen Lock
 * when the user speaks their custom Lock Word, plus hands-free Screenshot/Home/Back.
 */
class KaiAccessibilityActionService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive: only performs user-initiated global actions on voice command.
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun lockScreenNow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    fun takeScreenshotNow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    fun goHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun goBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    companion object {
        @Volatile
        private var instance: KaiAccessibilityActionService? = null

        fun isConnected(): Boolean = instance != null
        fun triggerLockScreen(): Boolean = instance?.lockScreenNow() == true
        fun triggerScreenshot(): Boolean = instance?.takeScreenshotNow() == true
        fun triggerHome(): Boolean = instance?.goHome() == true
        fun triggerBack(): Boolean = instance?.goBack() == true
        fun triggerNotifications(): Boolean = instance?.openNotifications() == true
    }
}
