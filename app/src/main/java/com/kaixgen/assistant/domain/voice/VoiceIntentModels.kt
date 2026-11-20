package com.kaixgen.assistant.domain.voice

import com.kaixgen.assistant.domain.model.CommandRiskLevel

/**
 * Full MVP + v1.5 Voice Command Intent catalog per PRD 11 & PRD 15 §4.
 */
enum class VoiceIntentType {
    OPEN_APP,
    OPEN_SETTINGS,
    SEARCH_WEB,
    CALL_CONTACT,
    CALL_NUMBER,
    ANSWER_CALL,
    REJECT_CALL,
    PLAY_MEDIA,
    MEDIA_CONTROL,
    VOLUME_CONTROL,
    LOCK_PHONE,
    LOCK_APP,
    FIND_PHONE,
    STOP_RING,
    TOGGLE_TORCH,
    BATTERY_STATUS,
    TIME_DATE,
    SET_ALARM,
    SET_TIMER,
    SEND_MESSAGE,
    RUN_ROUTINE,
    HELP,
    CANCEL,
    CONFIRM,
    CLARIFY
}

data class ParsedVoiceCommand(
    val rawText: String,
    val normalizedText: String,
    val intent: VoiceIntentType,
    val slots: Map<String, String> = emptyMap(),
    val confidence: Float,
    val riskLevel: CommandRiskLevel,
    val requiresConfirmation: Boolean,
    val latencyMs: Long
) {
    fun slotsToCompactJson(): String {
        if (slots.isEmpty()) return "{}"
        return slots.entries.joinToString(prefix = "{", postfix = "}") { (k, v) ->
            "\"${k.replace("\"", "")}\":\"${v.replace("\"", "")}\""
        }
    }

    fun slotsReadableSummary(): String {
        if (slots.isEmpty()) return ""
        return slots.entries.joinToString(" \u2022 ") { (k, v) -> "$k: $v" }
    }
}

sealed interface ActionExecutionResult {
    data class Executed(
        val spokenResponse: String,
        val displayMessage: String
    ) : ActionExecutionResult

    data class NeedsConfirmation(
        val promptMessage: String,
        val spokenPrompt: String
    ) : ActionExecutionResult

    data class Failure(
        val errorMessage: String
    ) : ActionExecutionResult
}
