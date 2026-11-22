package com.kaixgen.assistant.security

import android.app.Activity
import android.view.WindowManager

object ScreenSecurityHelper {

    private val digitsRegex = Regex("""\b\d{4,12}\b""")
    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")

    fun enableScreenCaptureProtection(activity: Activity?) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun disableScreenCaptureProtection(activity: Activity?) {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun scrubPii(rawLog: String): String {
        return rawLog
            .replace(emailRegex, "[REDACTED_EMAIL]")
            .replace(digitsRegex, "[REDACTED_NUM]")
    }
}
