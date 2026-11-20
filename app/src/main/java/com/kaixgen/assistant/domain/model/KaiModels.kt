package com.kaixgen.assistant.domain.model

enum class SecurityMode(
    val label: String,
    val shortDescription: String,
    val badgeText: String
) {
    CONVENIENCE(
        label = "Convenience",
        shortDescription = "Phrase match only • Fast unlock at home",
        badgeText = "Phrase Only"
    ),
    BALANCED(
        label = "Balanced",
        shortDescription = "Phrase + on-device speaker verification",
        badgeText = "Voiceprint"
    ),
    STRICT(
        label = "Strict",
        shortDescription = "Phrase + voiceprint + challenge word",
        badgeText = "Strict Guard"
    )
}

enum class VoiceOrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class CommandRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class AppLockGracePeriod(val label: String) {
    IMMEDIATE("Immediate"),
    ONE_MINUTE("1 min"),
    FIVE_MINUTES("5 min"),
    UNTIL_SCREEN_OFF("Screen off")
}

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    HINGLISH("hi-IN", "Hinglish", "Hinglish"),
    ENGLISH("en-IN", "English", "English"),
    HINDI("hi-IN", "Hindi", "हिन्दी"),
    TELUGU("te-IN", "Telugu", "తెలుగు"),
    TAMIL("ta-IN", "Tamil", "தமிழ்"),
    KANNADA("kn-IN", "Kannada", "ಕನ್ನಡ"),
    MALAYALAM("ml-IN", "Malayalam", "മലയാളം"),
    MARATHI("mr-IN", "Marathi", "मराठी"),
    ODIA("or-IN", "Odia", "ଓଡ଼ିଆ"),
    BANGLA("bn-IN", "Bangla", "বাংলা")
}

enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class WakeSensitivity(val label: String) {
    LOW("Low"),
    MEDIUM("Balanced"),
    HIGH("High")
}

data class VoiceLockConfig(
    val isVoiceLockEnabled: Boolean = true,
    val voicePhrase: String = "Hare Krishna",
    val voicePhraseLanguage: SupportedLanguage = SupportedLanguage.HINGLISH,
    val isVoiceprintEnrolled: Boolean = true,
    val hasBackupPasscode: Boolean = true,
    val backupPinLength: Int = 4,
    val backupPinPreviewHash: String = "1234",
    val lastPinUpdatedLabel: String = "Updated today",
    val securityMode: SecurityMode = SecurityMode.BALANCED,
    val appLockGracePeriod: AppLockGracePeriod = AppLockGracePeriod.ONE_MINUTE,
    val lastUnlockEvent: String = "Unlocked by voice • Just now"
)

data class AppLockItem(
    val packageName: String,
    val appName: String,
    val category: String,
    val isLocked: Boolean
)

data class VoiceSuggestion(
    val id: String,
    val phrase: String,
    val intentLabel: String,
    val riskLevel: CommandRiskLevel = CommandRiskLevel.LOW,
    val isWhatsApp: Boolean = false
)

data class CommandFeedback(
    val id: Long = 0L,
    val heardText: String,
    val intentName: String,
    val responseText: String,
    val confidencePercent: Int,
    val riskLevel: CommandRiskLevel,
    val slotsSummary: String = "",
    val latencyMs: Long = 0L,
    val requiresConfirmation: Boolean = false,
    val isExecuted: Boolean = false,
    val thumbsUp: Boolean? = null
)

data class CommandHistoryItem(
    val id: Long,
    val timestampMs: Long,
    val transcript: String,
    val intentName: String,
    val responseText: String,
    val status: String,
    val confidencePercent: Int,
    val latencyMs: Long
)

data class RoutineItem(
    val id: String,
    val name: String,
    val triggerPhrase: String,
    val actionsSummary: String,
    val enabled: Boolean
)

data class VoiceAliasItem(
    val id: Long,
    val kind: String,
    val spoken: String,
    val target: String
)

data class FindPhoneConfig(
    val voiceTriggerPhrase: String = "Hey KAI, phone kahan hai?",
    val isVoiceTriggerEnabled: Boolean = true,
    val isDoubleClapEnabled: Boolean = true,
    val isFlashlightStrobeEnabled: Boolean = true,
    val isVibrateEnabled: Boolean = true,
    val isRinging: Boolean = false
)

data class AssistantSettings(
    val language: SupportedLanguage = SupportedLanguage.HINGLISH,
    val wakeWordEnabled: Boolean = true,
    val wakeWordPhrase: String = "Hey KAI",
    val wakeSensitivity: WakeSensitivity = WakeSensitivity.MEDIUM,
    val ttsEnabled: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.LIGHT,
    val dynamicColorEnabled: Boolean = false,
    val cloudAiOptIn: Boolean = false
)
