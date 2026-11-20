package com.kaixgen.assistant.data.local

import com.kaixgen.assistant.data.local.db.CommandHistoryEntity
import com.kaixgen.assistant.data.local.db.KaiDatabase
import com.kaixgen.assistant.data.local.db.LockedAppEntity
import com.kaixgen.assistant.data.local.db.RoutineEntity
import com.kaixgen.assistant.data.local.db.SettingEntity
import com.kaixgen.assistant.data.local.db.VoiceAliasEntity
import com.kaixgen.assistant.data.local.db.VoicePhraseEntity
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
import com.kaixgen.assistant.security.PinVault
import com.kaixgen.assistant.security.PinVerificationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Production Room + Keystore PinVault implementation of KaiLocalRepository (PRD 05 §5 & 06 §3).
 * All reads flow reactively from Room DAOs; all writes execute off the main thread on Dispatchers.IO.
 */
class RoomKaiLocalRepository(
    private val database: KaiDatabase,
    private val pinVault: PinVault,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : KaiLocalRepository {

    private val phraseDao = database.voicePhraseDao()
    private val pinDao = database.pinRecordDao()
    private val lockedAppDao = database.lockedAppDao()
    private val commandHistoryDao = database.commandHistoryDao()
    private val aliasDao = database.voiceAliasDao()
    private val routineDao = database.routineDao()
    private val settingDao = database.settingDao()

    private val isRingingFlow = MutableStateFlow(false)

    init {
        scope.launch {
            seedInitialDefaultsIfNeeded()
        }
    }

    override val voiceLockConfig: StateFlow<VoiceLockConfig> = combine(
        phraseDao.observeAllPhrases(),
        pinDao.observePinRecord(),
        settingDao.observeAllSettings()
    ) { phrases, pinRecord, settingsList ->
        val settingsMap = settingsList.associate { it.key to it.value }
        val lockPhrase = phrases.firstOrNull { it.id == PHRASE_ID_LOCK }
        val hasPin = pinRecord != null
        val enabledFlag = settingsMap[KEY_VOICE_LOCK_ENABLED]?.toBooleanStrictOrNull() ?: hasPin

        VoiceLockConfig(
            isVoiceLockEnabled = enabledFlag && hasPin,
            voicePhrase = lockPhrase?.textNormalized ?: "Hare Krishna",
            voicePhraseLanguage = lockPhrase?.languageName
                ?.let { name -> SupportedLanguage.entries.firstOrNull { it.name == name } }
                ?: SupportedLanguage.HINGLISH,
            isVoiceprintEnrolled = lockPhrase != null,
            hasBackupPasscode = hasPin,
            backupPinLength = pinRecord?.pinLength ?: 4,
            backupPinPreviewHash = pinRecord?.hashHex?.take(8).orEmpty(),
            lastPinUpdatedLabel = if (pinRecord != null) formatUpdatedDate(pinRecord.changedAt) else "Not set",
            securityMode = settingsMap[KEY_SECURITY_MODE]
                ?.let { name -> SecurityMode.entries.firstOrNull { it.name == name } }
                ?: SecurityMode.BALANCED,
            appLockGracePeriod = settingsMap[KEY_GRACE_PERIOD]
                ?.let { name -> AppLockGracePeriod.entries.firstOrNull { it.name == name } }
                ?: AppLockGracePeriod.ONE_MINUTE,
            lastUnlockEvent = settingsMap[KEY_LAST_UNLOCK_EVENT] ?: "Ready • On-device Keystore active"
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = VoiceLockConfig()
    )

    override val lockedApps: StateFlow<List<AppLockItem>> = lockedAppDao.observeAllApps()
        .map { entities ->
            entities.map {
                AppLockItem(
                    packageName = it.packageName,
                    appName = it.appName,
                    category = it.category,
                    isLocked = it.isLocked
                )
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override val commandHistory: StateFlow<List<CommandHistoryItem>> =
        commandHistoryDao.observeRecentCommands(limit = 25)
            .map { entities ->
                entities.map {
                    CommandHistoryItem(
                        id = it.id,
                        timestampMs = it.timestampMs,
                        transcript = it.transcript,
                        intentName = it.intentName,
                        responseText = it.responseText,
                        status = it.status,
                        confidencePercent = it.confidencePercent,
                        latencyMs = it.latencyMs
                    )
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    override val routines: StateFlow<List<RoutineItem>> = routineDao.observeAllRoutines()
        .map { entities ->
            entities.map {
                RoutineItem(
                    id = it.id,
                    name = it.name,
                    triggerPhrase = it.triggerPhrase,
                    actionsSummary = it.actionsSummary,
                    enabled = it.enabled
                )
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override val aliases: StateFlow<List<VoiceAliasItem>> = aliasDao.observeAllAliases()
        .map { entities ->
            entities.map {
                VoiceAliasItem(
                    id = it.id,
                    kind = it.kind,
                    spoken = it.spoken,
                    target = it.target
                )
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override val findPhoneConfig: StateFlow<FindPhoneConfig> = combine(
        settingDao.observeAllSettings(),
        isRingingFlow
    ) { settingsList, ringing ->
        val map = settingsList.associate { it.key to it.value }
        FindPhoneConfig(
            voiceTriggerPhrase = map[KEY_FIND_PHRASE] ?: "Hey KAI, phone kahan hai?",
            isVoiceTriggerEnabled = map[KEY_FIND_VOICE]?.toBooleanStrictOrNull() ?: true,
            isDoubleClapEnabled = map[KEY_FIND_CLAP]?.toBooleanStrictOrNull() ?: true,
            isFlashlightStrobeEnabled = map[KEY_FIND_FLASH]?.toBooleanStrictOrNull() ?: true,
            isVibrateEnabled = map[KEY_FIND_VIBRATE]?.toBooleanStrictOrNull() ?: true,
            isRinging = ringing
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = FindPhoneConfig()
    )

    override val settings: StateFlow<AssistantSettings> = settingDao.observeAllSettings()
        .map { settingsList ->
            val map = settingsList.associate { it.key to it.value }
            AssistantSettings(
                language = map[KEY_LANGUAGE]
                    ?.let { name -> SupportedLanguage.entries.firstOrNull { it.name == name } }
                    ?: SupportedLanguage.HINGLISH,
                wakeWordEnabled = map[KEY_WAKE_WORD]?.toBooleanStrictOrNull() ?: true,
                wakeWordPhrase = map[KEY_WAKE_PHRASE] ?: "Hey KAI",
                wakeSensitivity = map[KEY_WAKE_SENSITIVITY]
                    ?.let { name -> WakeSensitivity.entries.firstOrNull { it.name == name } }
                    ?: WakeSensitivity.MEDIUM,
                ttsEnabled = map[KEY_TTS_ENABLED]?.toBooleanStrictOrNull() ?: true,
                themePreference = map[KEY_THEME]
                    ?.let { name -> ThemePreference.entries.firstOrNull { it.name == name } }
                    ?: ThemePreference.LIGHT,
                dynamicColorEnabled = map[KEY_DYNAMIC_COLOR]?.toBooleanStrictOrNull() ?: false,
                cloudAiOptIn = false
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AssistantSettings()
        )

    override fun setVoiceLockEnabled(enabled: Boolean): Boolean {
        if (enabled && !voiceLockConfig.value.hasBackupPasscode) {
            return false
        }
        persistSetting(KEY_VOICE_LOCK_ENABLED, enabled.toString())
        return true
    }

    override fun updateVoicePhrase(phrase: String, language: SupportedLanguage) {
        val cleaned = phrase.trim()
        if (cleaned.isEmpty()) return
        scope.launch {
            phraseDao.upsert(
                VoicePhraseEntity(
                    id = PHRASE_ID_LOCK,
                    type = "LOCK",
                    textNormalized = cleaned,
                    phoneticKey = computePhoneticKey(cleaned),
                    languageName = language.name,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun updateBackupPasscode(pin: String) {
        scope.launch {
            pinVault.setPin(pin)
        }
    }

    override suspend fun verifyBackupPasscodeSecure(pin: String): PinVerificationResult {
        return pinVault.verifyPin(pin)
    }

    override fun updateSecurityMode(mode: SecurityMode) {
        persistSetting(KEY_SECURITY_MODE, mode.name)
    }

    override fun updateGracePeriod(period: AppLockGracePeriod) {
        persistSetting(KEY_GRACE_PERIOD, period.name)
        scope.launch {
            lockedAppDao.updateGraceModeForAll(period.name)
        }
    }

    override fun toggleAppLock(packageName: String) {
        scope.launch {
            lockedAppDao.toggleAppLock(packageName)
        }
    }

    override fun lockAppByName(appName: String): String? {
        val target = lockedApps.value.firstOrNull {
            it.appName.contains(appName, ignoreCase = true) ||
                appName.contains(it.appName, ignoreCase = true)
        } ?: return null
        if (!target.isLocked) {
            toggleAppLock(target.packageName)
        }
        return target.appName
    }

    override fun recordUnlockEvent(summary: String) {
        persistSetting(KEY_LAST_UNLOCK_EVENT, summary)
    }

    override fun logCommandExecution(
        transcript: String,
        intentName: String,
        slotsJson: String,
        responseText: String,
        status: String,
        confidencePercent: Int,
        latencyMs: Long
    ) {
        scope.launch {
            commandHistoryDao.insert(
                CommandHistoryEntity(
                    transcript = transcript,
                    intentName = intentName,
                    slotsJson = slotsJson,
                    responseText = responseText,
                    status = status,
                    confidencePercent = confidencePercent,
                    latencyMs = latencyMs
                )
            )
        }
    }

    override fun deleteCommandHistoryItem(id: Long) {
        scope.launch {
            commandHistoryDao.deleteById(id)
        }
    }

    override fun clearCommandHistory() {
        scope.launch {
            commandHistoryDao.clearAll()
        }
    }

    override suspend fun resolveAppAlias(spoken: String): String? {
        val normalized = spoken.trim().lowercase()
        val aliases = aliasDao.getAllOnce()
        return aliases.firstOrNull {
            it.kind == "APP" && (it.spoken.equals(normalized, ignoreCase = true) || normalized.contains(it.spoken, ignoreCase = true))
        }?.target
    }

    override fun saveRoutine(name: String, triggerPhrase: String, actionsSummary: String) {
        val cleanName = name.trim()
        val cleanTrigger = triggerPhrase.trim().lowercase(Locale.ROOT)
        if (cleanName.isBlank() || cleanTrigger.isBlank()) return
        scope.launch {
            val id = "custom_${System.currentTimeMillis()}"
            routineDao.upsert(
                RoutineEntity(
                    id = id,
                    name = cleanName,
                    triggerPhrase = cleanTrigger,
                    actionsSummary = actionsSummary.trim().ifBlank { "Custom voice automation" },
                    enabled = true
                )
            )
        }
    }

    override fun toggleRoutine(id: String, enabled: Boolean) {
        scope.launch {
            routineDao.setEnabled(id, enabled)
        }
    }

    override fun deleteRoutine(id: String) {
        scope.launch {
            routineDao.deleteById(id)
        }
    }

    override fun addAlias(kind: String, spoken: String, target: String) {
        val cleanSpoken = spoken.trim()
        val cleanTarget = target.trim()
        if (cleanSpoken.isBlank() || cleanTarget.isBlank()) return
        scope.launch {
            aliasDao.upsert(
                VoiceAliasEntity(
                    kind = kind.uppercase(Locale.ROOT),
                    spoken = cleanSpoken,
                    target = cleanTarget
                )
            )
        }
    }

    override fun deleteAlias(id: Long) {
        scope.launch {
            aliasDao.deleteById(id)
        }
    }

    override fun toggleFindPhoneRinging(ringing: Boolean) {
        isRingingFlow.update { ringing }
    }

    override fun updateFindPhoneConfig(transform: (FindPhoneConfig) -> FindPhoneConfig) {
        val updated = transform(findPhoneConfig.value)
        isRingingFlow.value = updated.isRinging
        scope.launch {
            settingDao.upsert(SettingEntity(KEY_FIND_PHRASE, updated.voiceTriggerPhrase))
            settingDao.upsert(SettingEntity(KEY_FIND_VOICE, updated.isVoiceTriggerEnabled.toString()))
            settingDao.upsert(SettingEntity(KEY_FIND_CLAP, updated.isDoubleClapEnabled.toString()))
            settingDao.upsert(SettingEntity(KEY_FIND_FLASH, updated.isFlashlightStrobeEnabled.toString()))
            settingDao.upsert(SettingEntity(KEY_FIND_VIBRATE, updated.isVibrateEnabled.toString()))
        }
    }

    override fun updateLanguage(language: SupportedLanguage) {
        persistSetting(KEY_LANGUAGE, language.name)
    }

    override fun updateWakeWordEnabled(enabled: Boolean) {
        persistSetting(KEY_WAKE_WORD, enabled.toString())
    }

    override fun updateWakeSensitivity(sensitivity: WakeSensitivity) {
        persistSetting(KEY_WAKE_SENSITIVITY, sensitivity.name)
    }

    override fun updateTtsEnabled(enabled: Boolean) {
        persistSetting(KEY_TTS_ENABLED, enabled.toString())
    }

    override fun updateThemePreference(preference: ThemePreference) {
        persistSetting(KEY_THEME, preference.name)
    }

    override fun updateDynamicColor(enabled: Boolean) {
        persistSetting(KEY_DYNAMIC_COLOR, enabled.toString())
    }

    override fun deleteAllLocalData() {
        scope.launch {
            phraseDao.clearAll()
            pinDao.clearAll()
            commandHistoryDao.clearAll()
            lockedAppDao.unlockAllApps()
            settingDao.upsert(SettingEntity(KEY_VOICE_LOCK_ENABLED, "false"))
            settingDao.upsert(SettingEntity(KEY_LAST_UNLOCK_EVENT, "All local voice & lock data wiped"))
        }
    }

    private fun persistSetting(key: String, value: String) {
        scope.launch {
            settingDao.upsert(SettingEntity(key, value))
        }
    }

    private suspend fun seedInitialDefaultsIfNeeded() {
        if (phraseDao.getById(PHRASE_ID_LOCK) == null && settingDao.getValue(KEY_INITIALIZED) == null) {
            phraseDao.upsert(
                VoicePhraseEntity(
                    id = PHRASE_ID_LOCK,
                    type = "LOCK",
                    textNormalized = "Hare Krishna",
                    phoneticKey = computePhoneticKey("Hare Krishna"),
                    languageName = SupportedLanguage.HINGLISH.name
                )
            )
            pinVault.setPin("1234")
            settingDao.upsert(SettingEntity(KEY_VOICE_LOCK_ENABLED, "true"))
            settingDao.upsert(SettingEntity(KEY_INITIALIZED, "true"))
        }

        if (lockedAppDao.countApps() == 0) {
            lockedAppDao.insertAll(defaultLockedApps())
        }

        if (aliasDao.countAliases() == 0) {
            aliasDao.insertAll(defaultHinglishAliases())
        }

        if (routineDao.countRoutines() == 0) {
            routineDao.insertAll(defaultRoutines())
        }
    }

    private fun computePhoneticKey(phrase: String): String {
        return phrase.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
    }

    private fun formatUpdatedDate(timestampMs: Long): String {
        val diffHours = (System.currentTimeMillis() - timestampMs) / (1000 * 60 * 60)
        return when {
            diffHours < 1 -> "Updated just now"
            diffHours < 24 -> "Updated today"
            else -> {
                val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                "Updated ${fmt.format(Date(timestampMs))}"
            }
        }
    }

    private fun defaultLockedApps(): List<LockedAppEntity> = listOf(
        LockedAppEntity("com.whatsapp", "WhatsApp", "Messaging", true),
        LockedAppEntity("com.google.android.apps.photos", "Photos & Gallery", "Media", true),
        LockedAppEntity("com.instagram.android", "Instagram", "Social", true),
        LockedAppEntity("com.google.android.apps.nbu.paisa.user", "Google Pay", "Finance", false),
        LockedAppEntity("com.phonepe.app", "PhonePe", "Finance", false),
        LockedAppEntity("com.android.vending", "Play Store", "System", false),
        LockedAppEntity("com.google.android.gm", "Gmail", "Productivity", false),
        LockedAppEntity("com.android.settings", "Device Settings", "System", false)
    )

    private fun defaultHinglishAliases(): List<VoiceAliasEntity> = listOf(
        VoiceAliasEntity(kind = "APP", spoken = "insta", target = "Instagram"),
        VoiceAliasEntity(kind = "APP", spoken = "yt", target = "YouTube"),
        VoiceAliasEntity(kind = "APP", spoken = "wa", target = "WhatsApp"),
        VoiceAliasEntity(kind = "APP", spoken = "वॉट्सएप", target = "WhatsApp"),
        VoiceAliasEntity(kind = "APP", spoken = "व्हाट्सऐप", target = "WhatsApp"),
        VoiceAliasEntity(kind = "APP", spoken = "यूट्यूब", target = "YouTube"),
        VoiceAliasEntity(kind = "APP", spoken = "इंस्टाग्राम", target = "Instagram"),
        VoiceAliasEntity(kind = "APP", spoken = "कैमरा", target = "Camera"),
        VoiceAliasEntity(kind = "APP", spoken = "गैलरी", target = "Photos"),
        VoiceAliasEntity(kind = "APP", spoken = "मैप्स", target = "Maps"),
        VoiceAliasEntity(kind = "APP", spoken = "क्रोम", target = "Chrome"),
        VoiceAliasEntity(kind = "APP", spoken = "सेटिंग्स", target = "Settings")
    )

    private fun defaultRoutines(): List<RoutineEntity> = listOf(
        RoutineEntity(
            id = "good_morning",
            name = "Good Morning",
            triggerPhrase = "good morning",
            actionsSummary = "Announce current time, battery status & greet user"
        ),
        RoutineEntity(
            id = "driving_mode",
            name = "Driving Mode",
            triggerPhrase = "driving mode",
            actionsSummary = "Set volume for hands-free voice & open Google Maps"
        ),
        RoutineEntity(
            id = "sleep_mode",
            name = "Sleep Mode",
            triggerPhrase = "sleep mode",
            actionsSummary = "Lower media volume & arm Voice Lock protection"
        )
    )

    companion object {
        private const val PHRASE_ID_LOCK = "LOCK"
        private const val KEY_INITIALIZED = "db_seeded_v1"
        private const val KEY_VOICE_LOCK_ENABLED = "voice_lock_enabled"
        private const val KEY_SECURITY_MODE = "security_mode"
        private const val KEY_GRACE_PERIOD = "app_lock_grace_period"
        private const val KEY_LAST_UNLOCK_EVENT = "last_unlock_event"
        private const val KEY_LANGUAGE = "assistant_language"
        private const val KEY_WAKE_WORD = "wake_word_enabled"
        private const val KEY_WAKE_PHRASE = "wake_word_phrase"
        private const val KEY_WAKE_SENSITIVITY = "wake_sensitivity"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_THEME = "theme_preference"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_FIND_PHRASE = "find_phone_phrase"
        private const val KEY_FIND_VOICE = "find_phone_voice"
        private const val KEY_FIND_CLAP = "find_phone_clap"
        private const val KEY_FIND_FLASH = "find_phone_flash"
        private const val KEY_FIND_VIBRATE = "find_phone_vibrate"
    }
}
