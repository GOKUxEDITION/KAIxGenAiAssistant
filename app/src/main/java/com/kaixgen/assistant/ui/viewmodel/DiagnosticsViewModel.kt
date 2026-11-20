package com.kaixgen.assistant.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import com.kaixgen.assistant.domain.voice.HinglishVoiceCommandParser
import com.kaixgen.assistant.domain.voice.VoiceIntentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BenchmarkResult(
    val totalCases: Int,
    val passedCases: Int,
    val avgLatencyMs: Float
)

data class OemBatteryGuide(
    val oemName: String,
    val steps: List<String>
)

data class DiagnosticsUiState(
    val deviceManufacturer: String = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() },
    val deviceModel: String = Build.MODEL,
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    val oemGuide: OemBatteryGuide = resolveOemGuide(Build.MANUFACTURER),
    val benchmarkResult: BenchmarkResult? = null
)

class DiagnosticsViewModel(
    private val parser: HinglishVoiceCommandParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    fun runNluBenchmark() {
        val suite = listOf(
            "Hey KAI, WhatsApp kholo" to VoiceIntentType.OPEN_APP,
            "व्हाट्सऐप खोलो" to VoiceIntentType.OPEN_APP,
            "Mummy ko call karo" to VoiceIntentType.CALL_CONTACT,
            "Rahul ko message karo main late hoon" to VoiceIntentType.SEND_MESSAGE,
            "Torch chalu karo" to VoiceIntentType.TOGGLE_TORCH,
            "Kitni battery hai?" to VoiceIntentType.BATTERY_STATUS,
            "Kitne baje hain?" to VoiceIntentType.TIME_DATE,
            "Volume 60 percent" to VoiceIntentType.VOLUME_CONTROL,
            "10 minute ka timer" to VoiceIntentType.SET_TIMER,
            "KAI, phone kahan hai?" to VoiceIntentType.FIND_PHONE,
            "Instagram lock karo" to VoiceIntentType.LOCK_APP,
            "Good morning" to VoiceIntentType.RUN_ROUTINE
        )

        var passed = 0
        var totalNs = 0L
        suite.forEach { (phrase, expected) ->
            val start = System.nanoTime()
            val parsed = parser.parse(phrase)
            totalNs += (System.nanoTime() - start)
            if (parsed.intent == expected) passed++
        }
        val avgMs = (totalNs / suite.size) / 1_000_000f
        _uiState.update {
            it.copy(
                benchmarkResult = BenchmarkResult(
                    totalCases = suite.size,
                    passedCases = passed,
                    avgLatencyMs = avgMs
                )
            )
        }
    }
}

private fun resolveOemGuide(manufacturer: String): OemBatteryGuide {
    val oem = manufacturer.lowercase()
    return when {
        oem.contains("xiaomi") || oem.contains("redmi") || oem.contains("poco") -> OemBatteryGuide(
            oemName = "Xiaomi / HyperOS / MIUI",
            steps = listOf(
                "1. Open App Info \u2192 Enable 'Autostart' for KAIxGen Assistant.",
                "2. Tap 'Battery saver' \u2192 Select 'No restrictions'.",
                "3. Open Recent Apps and lock KAIxGen card so MIUI doesn't kill wake word."
            )
        )
        oem.contains("samsung") -> OemBatteryGuide(
            oemName = "Samsung One UI",
            steps = listOf(
                "1. Open App Info \u2192 Battery \u2192 Set to 'Unrestricted'.",
                "2. Go to Device Care \u2192 Battery \u2192 Background usage limits \u2192 Add KAIxGen to 'Never auto sleeping apps'."
            )
        )
        oem.contains("oppo") || oem.contains("realme") || oem.contains("oneplus") -> OemBatteryGuide(
            oemName = "ColorOS / Realme UI / OxygenOS",
            steps = listOf(
                "1. Open App Info \u2192 Battery Usage \u2192 Enable 'Allow background activity' and 'Allow auto-launch'.",
                "2. Disable 'Pause app activity if unused'."
            )
        )
        oem.contains("vivo") || oem.contains("iqoo") -> OemBatteryGuide(
            oemName = "Vivo Funtouch OS",
            steps = listOf(
                "1. Open App Info \u2192 Battery \u2192 Select 'Unrestricted'.",
                "2. Enable 'Autostart' in iManager \u2192 App Management."
            )
        )
        else -> OemBatteryGuide(
            oemName = "Standard Android",
            steps = listOf(
                "1. Open App Info \u2192 App battery usage \u2192 Select 'Unrestricted'.",
                "2. Keep 'KAI Voice Listening' notification channel enabled for always-on wake word."
            )
        )
    }
}
