package com.kaixgen.assistant.domain.voice

import com.kaixgen.assistant.domain.ai.GeminiHybridAssistant
import kotlinx.coroutines.withTimeoutOrNull

class CommandOrchestrator(
    private val fastParser: HinglishVoiceCommandParser,
    private val advancedParser: ExpenseAndMemoryParser,
    private val actionExecutor: KaiActionExecutor,
    private val hybridAssistant: GeminiHybridAssistant,
    private val ttsManager: KaiTtsManager
) {
    data class OrchestratorOutcome(
        val spokenText: String,
        val displayText: String,
        val intentName: String,
        val source: String,
        val latencyMs: Long,
        val requiresConfirmation: Boolean = false,
        val advancedIntent: AdvancedIntentResult = AdvancedIntentResult.None
    )

    suspend fun handleUtterance(
        utterance: String,
        customLockPhrase: String = "",
        cloudAiOptIn: Boolean = false,
        ttsEnabled: Boolean = true,
        preferredLanguage: String = "hi-IN"
    ): OrchestratorOutcome {
        val startMs = System.currentTimeMillis()

        val adv = advancedParser.parse(utterance)
        if (adv != AdvancedIntentResult.None) {
            val (spoken, intentLabel) = when (adv) {
                is AdvancedIntentResult.AddExpense ->
                    "₹${adv.amountRupees} ${adv.category} (${adv.note}) expense save ho gaya." to "ADD_EXPENSE"
                is AdvancedIntentResult.RememberFact ->
                    "Yaad rakh liya: ${adv.key} = ${adv.value}." to "REMEMBER"
                is AdvancedIntentResult.RecallFact ->
                    "Memory Vault me '${adv.queryKey}' search kar raha hoon." to "RECALL"
                is AdvancedIntentResult.TriggerSos ->
                    "SOS Alert active! Siren aur emergency share start ho raha hai." to "SOS"
                is AdvancedIntentResult.StartDrivingMode ->
                    "Driving Mode shuru kar raha hoon. Safe drive karein." to "DRIVING_MODE"
                is AdvancedIntentResult.StartFocusTimer ->
                    "${adv.minutes} minute ka Focus Pomodoro timer start ho gaya." to "START_FOCUS"
                AdvancedIntentResult.None -> "" to "NONE"
            }
            if (ttsEnabled) ttsManager.speak(spoken, languageCode = preferredLanguage)
            return OrchestratorOutcome(
                spokenText = spoken,
                displayText = spoken,
                intentName = intentLabel,
                source = "fast_v2",
                latencyMs = System.currentTimeMillis() - startMs,
                advancedIntent = adv
            )
        }

        val parsed = fastParser.parse(utterance, customLockPhrase = customLockPhrase)
        if (parsed.intent != VoiceIntentType.CLARIFY) {
            return when (val exec = actionExecutor.execute(parsed, confirmed = false)) {
                is ActionExecutionResult.Executed -> {
                    if (ttsEnabled) ttsManager.speak(exec.spokenResponse, languageCode = preferredLanguage)
                    OrchestratorOutcome(
                        spokenText = exec.spokenResponse,
                        displayText = exec.displayMessage,
                        intentName = parsed.intent.name,
                        source = "fast",
                        latencyMs = System.currentTimeMillis() - startMs
                    )
                }
                is ActionExecutionResult.NeedsConfirmation -> {
                    if (ttsEnabled) ttsManager.speak(exec.spokenPrompt, languageCode = preferredLanguage)
                    OrchestratorOutcome(
                        spokenText = exec.spokenPrompt,
                        displayText = exec.promptMessage,
                        intentName = parsed.intent.name,
                        source = "fast",
                        latencyMs = System.currentTimeMillis() - startMs,
                        requiresConfirmation = true
                    )
                }
                is ActionExecutionResult.Failure -> {
                    if (ttsEnabled) ttsManager.speak(exec.errorMessage, languageCode = preferredLanguage)
                    OrchestratorOutcome(
                        spokenText = exec.errorMessage,
                        displayText = exec.errorMessage,
                        intentName = parsed.intent.name,
                        source = "fast",
                        latencyMs = System.currentTimeMillis() - startMs
                    )
                }
            }
        }

        if (cloudAiOptIn) {
            val aiResult = withTimeoutOrNull(4_000L) {
                hybridAssistant.askHinglishAssistant(utterance, preferredLanguage)
            }
            if (aiResult is GeminiHybridAssistant.HybridAiResult.Reply) {
                if (ttsEnabled) ttsManager.speak(aiResult.spokenText, languageCode = preferredLanguage)
                return OrchestratorOutcome(
                    spokenText = aiResult.spokenText,
                    displayText = aiResult.spokenText,
                    intentName = "ANSWER",
                    source = "llm",
                    latencyMs = System.currentTimeMillis() - startMs
                )
            }
        }

        val fallback = "Mujhe command clear nahi hui. Kya aap dobara bol sakte hain?"
        if (ttsEnabled) ttsManager.speak(fallback, languageCode = preferredLanguage)
        return OrchestratorOutcome(
            spokenText = fallback,
            displayText = fallback,
            intentName = "CLARIFY",
            source = "fast",
            latencyMs = System.currentTimeMillis() - startMs
        )
    }
}
