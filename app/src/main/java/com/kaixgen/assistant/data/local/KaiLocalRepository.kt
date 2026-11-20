package com.kaixgen.assistant.data.local

import com.kaixgen.assistant.domain.model.AppLockGracePeriod
import com.kaixgen.assistant.domain.model.AppLockItem
import com.kaixgen.assistant.domain.model.AssistantSettings
import com.kaixgen.assistant.domain.model.CommandHistoryItem
import com.kaixgen.assistant.domain.model.FindPhoneConfig
import com.kaixgen.assistant.domain.model.RoutineItem
import com.kaixgen.assistant.domain.model.SecurityMode
import com.kaixgen.assistant.domain.model.SupportedLanguage
import com.kaixgen.assistant.domain.model.ThemePreference
import com.kaixgen.assistant.domain.model.VoiceAliasItem
import com.kaixgen.assistant.domain.model.VoiceLockConfig
import com.kaixgen.assistant.domain.model.WakeSensitivity
import com.kaixgen.assistant.security.PinVerificationResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean Architecture Repository contract backed by Room Database + Keystore PinVault.
 */
interface KaiLocalRepository {
    val voiceLockConfig: StateFlow<VoiceLockConfig>
    val lockedApps: StateFlow<List<AppLockItem>>
    val commandHistory: StateFlow<List<CommandHistoryItem>>
    val routines: StateFlow<List<RoutineItem>>
    val aliases: StateFlow<List<VoiceAliasItem>>
    val findPhoneConfig: StateFlow<FindPhoneConfig>
    val settings: StateFlow<AssistantSettings>

    fun setVoiceLockEnabled(enabled: Boolean): Boolean
    fun updateVoicePhrase(phrase: String, language: SupportedLanguage)
    fun updateBackupPasscode(pin: String)
    suspend fun verifyBackupPasscodeSecure(pin: String): PinVerificationResult
    fun updateSecurityMode(mode: SecurityMode)
    fun updateGracePeriod(period: AppLockGracePeriod)
    fun toggleAppLock(packageName: String)
    fun lockAppByName(appName: String): String?
    fun recordUnlockEvent(summary: String)

    fun logCommandExecution(
        transcript: String,
        intentName: String,
        slotsJson: String,
        responseText: String,
        status: String,
        confidencePercent: Int,
        latencyMs: Long
    )
    fun deleteCommandHistoryItem(id: Long)
    fun clearCommandHistory()
    suspend fun resolveAppAlias(spoken: String): String?

    fun saveRoutine(name: String, triggerPhrase: String, actionsSummary: String)
    fun toggleRoutine(id: String, enabled: Boolean)
    fun deleteRoutine(id: String)

    fun addAlias(kind: String, spoken: String, target: String)
    fun deleteAlias(id: Long)

    fun toggleFindPhoneRinging(ringing: Boolean)
    fun updateFindPhoneConfig(transform: (FindPhoneConfig) -> FindPhoneConfig)

    fun updateLanguage(language: SupportedLanguage)
    fun updateWakeWordEnabled(enabled: Boolean)
    fun updateWakeSensitivity(sensitivity: WakeSensitivity)
    fun updateTtsEnabled(enabled: Boolean)
    fun updateThemePreference(preference: ThemePreference)
    fun updateDynamicColor(enabled: Boolean)
    fun deleteAllLocalData()
}
