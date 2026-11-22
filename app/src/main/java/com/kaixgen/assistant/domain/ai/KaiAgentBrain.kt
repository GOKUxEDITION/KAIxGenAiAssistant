package com.kaixgen.assistant.domain.ai

import java.util.Locale

enum class AgentVoiceState {
    IDLE, LISTENING, RECOGNIZING, THINKING, PLANNING,
    EXECUTING, SPEAKING, WAITING, COMPLETED, FAILED, CANCELLED
}

data class AgentTaskStep(
    val stepIndex: Int,
    val rawClause: String,
    val resolvedIntent: String,
    val requiresConfirmation: Boolean
)

data class AgentTaskPlan(
    val planId: Long,
    val originalUtterance: String,
    val steps: List<AgentTaskStep>,
    val isFollowUp: Boolean,
    val state: AgentVoiceState = AgentVoiceState.PLANNING
)

data class AgentDialogContext(
    val lastUtterance: String? = null,
    val lastOpenedApp: String? = null,
    val lastPlan: AgentTaskPlan? = null
)

/**
 * Phase 1 & 2 Modular AI Brain & Task Planner:
 * - Decomposes multi-step Hinglish/Hindi/English utterances ("aur", "and", "phir")
 * - Tracks short-term conversational DialogContext for follow-ups ("jo abhi khola tha", "isko hindi mein")
 * - Supports safe task cancellation ("pehle wala task cancel kar do")
 * - Never performs uncontrolled arbitrary code execution
 */
class KaiAgentBrain {

    private var context = AgentDialogContext()

    fun getContext(): AgentDialogContext = context

    fun plan(utterance: String, nowMs: Long = System.currentTimeMillis()): AgentTaskPlan {
        val clean = utterance.trim()
        val lower = clean.lowercase(Locale.ROOT)

        // 1. Cancellation check
        if (lower.contains("cancel") || lower.contains("band karo task") || lower.contains("ruk jao")) {
            val cancelledPlan = AgentTaskPlan(
                planId = nowMs,
                originalUtterance = clean,
                steps = listOf(AgentTaskStep(0, clean, "CANCEL_TASK", requiresConfirmation = false)),
                isFollowUp = true,
                state = AgentVoiceState.CANCELLED
            )
            context = context.copy(lastUtterance = clean, lastPlan = cancelledPlan)
            return cancelledPlan
        }

        // 2. Follow-up detection
        val isFollowUp = lower.startsWith("isko ") || lower.startsWith("usko ") ||
            lower.contains("jo abhi khola") || lower.contains("phir se")

        // 3. Multi-step clause decomposition ("aur", "and", "phir", "then")
        val rawClauses = clean
            .split(Regex("""\s+(?:aur|and|phir|then)\s+|,\s*""", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(5)

        val steps = rawClauses.mapIndexed { idx, clause ->
            val intent = classifyClause(clause)
            val highRisk = intent in setOf("SEND_MESSAGE", "CALL_CONTACT", "SOS")
            AgentTaskStep(
                stepIndex = idx,
                rawClause = clause,
                resolvedIntent = intent,
                requiresConfirmation = highRisk
            )
        }

        val openedApp = steps.firstOrNull { it.resolvedIntent == "OPEN_APP" }?.rawClause
        val plan = AgentTaskPlan(
            planId = nowMs,
            originalUtterance = clean,
            steps = steps,
            isFollowUp = isFollowUp,
            state = AgentVoiceState.PLANNING
        )
        context = context.copy(
            lastUtterance = clean,
            lastOpenedApp = openedApp ?: context.lastOpenedApp,
            lastPlan = plan
        )
        return plan
    }

    private fun classifyClause(clause: String): String {
        val c = clause.lowercase(Locale.ROOT)
        return when {
            c.contains("whatsapp") || c.contains("kholo") || c.contains("open ") -> "OPEN_APP"
            c.contains("message") || c.contains("bhejo") -> "SEND_MESSAGE"
            c.contains("battery") -> "BATTERY_STATUS"
            c.contains("alarm") || c.contains("utha dena") -> "SET_ALARM"
            c.contains("reminder") || c.contains("yaad dilana") -> "SET_REMINDER"
            c.contains("hindi") || c.contains("samjhao") || c.contains("explain") -> "EXPLAIN_OR_TRANSLATE"
            c.contains("summary") -> "SUMMARIZE"
            c.contains("dhoondho") || c.contains("find") -> "FIND_PHONE"
            c.contains("driving") -> "DRIVING_MODE"
            c.contains("study") || c.contains("padhai") -> "STUDENT_MODE"
            c.contains("senior") || c.contains("bujurg") -> "SENIOR_MODE"
            c.contains("call") -> "CALL_CONTACT"
            else -> "CONVERSATION_AI"
        }
    }
}
