package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.CommandFeedback
import com.kaixgen.assistant.domain.model.CommandHistoryItem
import com.kaixgen.assistant.domain.model.CommandRiskLevel
import com.kaixgen.assistant.domain.model.VoiceOrbState
import com.kaixgen.assistant.domain.model.VoiceSuggestion
import com.kaixgen.assistant.domain.voice.ActionExecutionResult
import com.kaixgen.assistant.domain.voice.HinglishVoiceCommandParser
import com.kaixgen.assistant.domain.voice.KaiActionExecutor
import com.kaixgen.assistant.domain.voice.KaiTtsManager
import com.kaixgen.assistant.domain.voice.ParsedVoiceCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KaiBuddyUiState(
    val orbState: VoiceOrbState = VoiceOrbState.IDLE,
    val liveTranscript: String = "",
    val activeSuggestionIndex: Int = 0,
    val suggestions: List<VoiceSuggestion> = defaultVoiceSuggestions(),
    val lastFeedback: CommandFeedback? = null,
    val commandHistory: List<CommandHistoryItem> = emptyList(),
    val statusMessage: String? = null,
    val isWakeWordActive: Boolean = true,
    val soundAmplitude: Float = 0f
) {
    val featuredSuggestion: VoiceSuggestion
        get() = suggestions.getOrElse(activeSuggestionIndex % suggestions.size) { suggestions.first() }
}

private fun defaultVoiceSuggestions(): List<VoiceSuggestion> = listOf(
    VoiceSuggestion(
        id = "open_whatsapp",
        phrase = "\u201CKAI, Open WhatsApp\u201D",
        intentLabel = "OPEN_APP",
        riskLevel = CommandRiskLevel.LOW,
        isWhatsApp = true
    ),
    VoiceSuggestion(
        id = "battery_check",
        phrase = "\u201CKitni battery hai?\u201D",
        intentLabel = "BATTERY_STATUS",
        riskLevel = CommandRiskLevel.LOW
    ),
    VoiceSuggestion(
        id = "time_check",
        phrase = "\u201CKitne baje hain?\u201D",
        intentLabel = "TIME_DATE",
        riskLevel = CommandRiskLevel.LOW
    ),
    VoiceSuggestion(
        id = "toggle_torch",
        phrase = "\u201CTorch chalu karo\u201D",
        intentLabel = "TOGGLE_TORCH",
        riskLevel = CommandRiskLevel.LOW
    ),
    VoiceSuggestion(
        id = "call_mummy",
        phrase = "\u201CMummy ko call karo\u201D",
        intentLabel = "CALL_CONTACT",
        riskLevel = CommandRiskLevel.MEDIUM
    ),
    VoiceSuggestion(
        id = "find_phone",
        phrase = "\u201CKAI, phone kahan hai?\u201D",
        intentLabel = "FIND_PHONE",
        riskLevel = CommandRiskLevel.LOW
    )
)

class KaiBuddyViewModel(
    private val repository: KaiLocalRepository,
    private val parser: HinglishVoiceCommandParser,
    private val actionExecutor: KaiActionExecutor,
    private val ttsManager: KaiTtsManager
) : ViewModel() {

    private val localState = MutableStateFlow(LocalBuddyState())
    private var pendingCommand: ParsedVoiceCommand? = null
    private var activeCommandJob: Job? = null

    val uiState: StateFlow<KaiBuddyUiState> = combine(
        localState,
        repository.commandHistory,
        repository.settings
    ) { local, history, settings ->
        KaiBuddyUiState(
            orbState = local.orbState,
            liveTranscript = local.liveTranscript,
            activeSuggestionIndex = local.activeSuggestionIndex,
            suggestions = local.suggestions,
            lastFeedback = local.lastFeedback,
            commandHistory = history,
            statusMessage = local.statusMessage,
            isWakeWordActive = settings.wakeWordEnabled,
            soundAmplitude = local.soundAmplitude
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KaiBuddyUiState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(4_500)
                if (localState.value.orbState == VoiceOrbState.IDLE) {
                    localState.update { state ->
                        state.copy(activeSuggestionIndex = (state.activeSuggestionIndex + 1) % state.suggestions.size)
                    }
                }
            }
        }
    }

    fun onStartListening() {
        activeCommandJob?.cancel()
        ttsManager.stop()
        localState.update {
            it.copy(
                orbState = VoiceOrbState.LISTENING,
                liveTranscript = "",
                statusMessage = null
            )
        }
    }

    fun onAmplitudeChanged(rmsdB: Float) {
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        localState.update { it.copy(soundAmplitude = normalized) }
    }

    fun onPartialTranscript(partial: String) {
        localState.update { it.copy(liveTranscript = partial) }
    }

    fun onFinalTranscript(transcript: String) {
        val cleaned = transcript.trim()
        if (cleaned.isEmpty()) {
            onVoiceError("Kuch sunai nahi diya, phir se bolo.")
            return
        }
        processUtterance(cleaned)
    }

    fun onSuggestionTapped(suggestion: VoiceSuggestion) {
        val raw = suggestion.phrase.replace("\u201C", "").replace("\u201D", "")
        processUtterance(raw)
    }

    fun onReplayHistoryCommand(transcript: String) {
        processUtterance(transcript)
    }

    fun onDeleteHistoryItem(id: Long) {
        repository.deleteCommandHistoryItem(id)
    }

    fun onClearHistory() {
        repository.clearCommandHistory()
    }

    fun onVoiceError(message: String) {
        activeCommandJob?.cancel()
        localState.update {
            it.copy(
                orbState = VoiceOrbState.ERROR,
                statusMessage = message,
                soundAmplitude = 0f
            )
        }
        activeCommandJob = viewModelScope.launch {
            delay(2_200)
            localState.update { it.copy(orbState = VoiceOrbState.IDLE) }
        }
    }

    fun onCancelListening() {
        activeCommandJob?.cancel()
        localState.update {
            it.copy(
                orbState = VoiceOrbState.IDLE,
                liveTranscript = "",
                soundAmplitude = 0f
            )
        }
    }

    fun onConfirmPendingAction() {
        val command = pendingCommand ?: return
        pendingCommand = null
        val settings = repository.settings.value

        when (val result = actionExecutor.execute(command, confirmed = true)) {
            is ActionExecutionResult.Executed -> {
                val feedback = CommandFeedback(
                    heardText = command.rawText,
                    intentName = command.intent.name,
                    responseText = result.displayMessage,
                    confidencePercent = (command.confidence * 100).toInt(),
                    riskLevel = command.riskLevel,
                    slotsSummary = command.slotsReadableSummary(),
                    latencyMs = command.latencyMs,
                    requiresConfirmation = false,
                    isExecuted = true
                )
                repository.logCommandExecution(
                    transcript = command.rawText,
                    intentName = command.intent.name,
                    slotsJson = command.slotsToCompactJson(),
                    responseText = result.displayMessage,
                    status = "CONFIRMED",
                    confidencePercent = feedback.confidencePercent,
                    latencyMs = command.latencyMs
                )
                ttsManager.speak(result.spokenResponse, settings.language.code, settings.ttsEnabled)
                localState.update {
                    it.copy(
                        orbState = VoiceOrbState.SPEAKING,
                        lastFeedback = feedback
                    )
                }
                scheduleReturnToIdle()
            }
            is ActionExecutionResult.Failure -> {
                onVoiceError(result.errorMessage)
            }
            is ActionExecutionResult.NeedsConfirmation -> Unit
        }
    }

    fun onCancelPendingAction() {
        val cancelled = pendingCommand
        pendingCommand = null
        if (cancelled != null) {
            repository.logCommandExecution(
                transcript = cancelled.rawText,
                intentName = cancelled.intent.name,
                slotsJson = cancelled.slotsToCompactJson(),
                responseText = "Cancelled by user",
                status = "CANCELLED",
                confidencePercent = (cancelled.confidence * 100).toInt(),
                latencyMs = cancelled.latencyMs
            )
        }
        localState.update {
            it.copy(
                orbState = VoiceOrbState.IDLE,
                lastFeedback = null,
                statusMessage = "Action cancelled."
            )
        }
    }

    fun onFeedbackVote(thumbsUp: Boolean) {
        localState.update { state ->
            state.copy(lastFeedback = state.lastFeedback?.copy(thumbsUp = thumbsUp))
        }
    }

    fun dismissFeedbackCard() {
        pendingCommand = null
        localState.update { it.copy(lastFeedback = null) }
    }

    private fun processUtterance(utterance: String) {
        activeCommandJob?.cancel()
        activeCommandJob = viewModelScope.launch {
            localState.update {
                it.copy(
                    orbState = VoiceOrbState.THINKING,
                    liveTranscript = utterance,
                    soundAmplitude = 0f,
                    statusMessage = null
                )
            }

            val customPhrase = repository.voiceLockConfig.value.voicePhrase
            val aliasCache = mutableMapOf<String, String?>()
            val resolvedAlias = repository.resolveAppAlias(utterance)
            if (resolvedAlias != null) {
                aliasCache[utterance.lowercase()] = resolvedAlias
            }

            val parsed = parser.parse(
                rawUtterance = utterance,
                customLockPhrase = customPhrase,
                aliasResolver = { spoken ->
                    aliasCache[spoken.lowercase()]
                }
            )

            // Resolve app alias asynchronously if OPEN_APP / LOCK_APP slot was extracted
            val enrichedCommand = enrichAppAliasIfNeeded(parsed)
            val settings = repository.settings.value

            when (val execResult = actionExecutor.execute(enrichedCommand, confirmed = false)) {
                is ActionExecutionResult.NeedsConfirmation -> {
                    pendingCommand = enrichedCommand
                    val feedback = CommandFeedback(
                        heardText = enrichedCommand.rawText,
                        intentName = enrichedCommand.intent.name,
                        responseText = execResult.promptMessage,
                        confidencePercent = (enrichedCommand.confidence * 100).toInt(),
                        riskLevel = enrichedCommand.riskLevel,
                        slotsSummary = enrichedCommand.slotsReadableSummary(),
                        latencyMs = enrichedCommand.latencyMs,
                        requiresConfirmation = true,
                        isExecuted = false
                    )
                    ttsManager.speak(execResult.spokenPrompt, settings.language.code, settings.ttsEnabled)
                    localState.update {
                        it.copy(
                            orbState = VoiceOrbState.IDLE,
                            lastFeedback = feedback
                        )
                    }
                }
                is ActionExecutionResult.Executed -> {
                    pendingCommand = null
                    val feedback = CommandFeedback(
                        heardText = enrichedCommand.rawText,
                        intentName = enrichedCommand.intent.name,
                        responseText = execResult.displayMessage,
                        confidencePercent = (enrichedCommand.confidence * 100).toInt(),
                        riskLevel = enrichedCommand.riskLevel,
                        slotsSummary = enrichedCommand.slotsReadableSummary(),
                        latencyMs = enrichedCommand.latencyMs,
                        requiresConfirmation = false,
                        isExecuted = true
                    )
                    repository.logCommandExecution(
                        transcript = enrichedCommand.rawText,
                        intentName = enrichedCommand.intent.name,
                        slotsJson = enrichedCommand.slotsToCompactJson(),
                        responseText = execResult.displayMessage,
                        status = "EXECUTED",
                        confidencePercent = feedback.confidencePercent,
                        latencyMs = enrichedCommand.latencyMs
                    )
                    ttsManager.speak(execResult.spokenResponse, settings.language.code, settings.ttsEnabled)
                    localState.update {
                        it.copy(
                            orbState = VoiceOrbState.SPEAKING,
                            lastFeedback = feedback
                        )
                    }
                    scheduleReturnToIdle()
                }
                is ActionExecutionResult.Failure -> {
                    pendingCommand = null
                    repository.logCommandExecution(
                        transcript = enrichedCommand.rawText,
                        intentName = enrichedCommand.intent.name,
                        slotsJson = enrichedCommand.slotsToCompactJson(),
                        responseText = execResult.errorMessage,
                        status = "FAILED",
                        confidencePercent = (enrichedCommand.confidence * 100).toInt(),
                        latencyMs = enrichedCommand.latencyMs
                    )
                    onVoiceError(execResult.errorMessage)
                }
            }
        }
    }

    private suspend fun enrichAppAliasIfNeeded(command: ParsedVoiceCommand): ParsedVoiceCommand {
        val spokenApp = command.slots["app"] ?: return command
        val mapped = repository.resolveAppAlias(spokenApp) ?: return command
        return command.copy(slots = command.slots + ("app" to mapped))
    }

    private fun scheduleReturnToIdle() {
        activeCommandJob = viewModelScope.launch {
            delay(1_350)
            localState.update { it.copy(orbState = VoiceOrbState.IDLE) }
        }
    }

    private data class LocalBuddyState(
        val orbState: VoiceOrbState = VoiceOrbState.IDLE,
        val liveTranscript: String = "",
        val activeSuggestionIndex: Int = 0,
        val suggestions: List<VoiceSuggestion> = defaultVoiceSuggestions(),
        val lastFeedback: CommandFeedback? = null,
        val statusMessage: String? = null,
        val soundAmplitude: Float = 0f
    )
}
