package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.RoutineItem
import com.kaixgen.assistant.domain.voice.ActionExecutionResult
import com.kaixgen.assistant.domain.voice.HinglishVoiceCommandParser
import com.kaixgen.assistant.domain.voice.KaiActionExecutor
import com.kaixgen.assistant.domain.voice.KaiTtsManager
import com.kaixgen.assistant.domain.voice.ParsedVoiceCommand
import com.kaixgen.assistant.domain.voice.VoiceIntentType
import com.kaixgen.assistant.domain.model.CommandRiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class RoutinesUiState(
    val routines: List<RoutineItem> = emptyList(),
    val lastRunBanner: String? = null,
    val showCreateSheet: Boolean = false
)

class RoutinesViewModel(
    private val repository: KaiLocalRepository,
    private val parser: HinglishVoiceCommandParser,
    private val actionExecutor: KaiActionExecutor,
    private val ttsManager: KaiTtsManager
) : ViewModel() {

    private val localState = MutableStateFlow(LocalRoutinesState())

    val uiState: StateFlow<RoutinesUiState> = combine(
        repository.routines,
        localState
    ) { routines, local ->
        RoutinesUiState(
            routines = routines,
            lastRunBanner = local.lastRunBanner,
            showCreateSheet = local.showCreateSheet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoutinesUiState()
    )

    fun openCreateSheet() {
        localState.update { it.copy(showCreateSheet = true) }
    }

    fun closeCreateSheet() {
        localState.update { it.copy(showCreateSheet = false) }
    }

    fun onToggleRoutine(id: String, enabled: Boolean) {
        repository.toggleRoutine(id, enabled)
    }

    fun onCreateRoutine(name: String, triggerPhrase: String, selectedActions: List<String>) {
        val summary = selectedActions.joinToString(" \u2022 ").ifBlank { "Custom voice automation" }
        repository.saveRoutine(name, triggerPhrase, summary)
        localState.update {
            it.copy(
                showCreateSheet = false,
                lastRunBanner = "Routine \"$name\" saved! Say \"$triggerPhrase\" to trigger."
            )
        }
    }

    fun onDeleteRoutine(id: String) {
        repository.deleteRoutine(id)
    }

    fun onRunRoutineNow(routine: RoutineItem) {
        val command = ParsedVoiceCommand(
            rawText = routine.triggerPhrase,
            normalizedText = parser.normalizeUtterance(routine.triggerPhrase),
            intent = VoiceIntentType.RUN_ROUTINE,
            slots = mapOf("routine" to routine.name),
            confidence = 1.0f,
            riskLevel = CommandRiskLevel.LOW,
            requiresConfirmation = false,
            latencyMs = 2L
        )
        val result = actionExecutor.execute(command, confirmed = true)
        val settings = repository.settings.value
        val displayMsg = when (result) {
            is ActionExecutionResult.Executed -> {
                ttsManager.speak(result.spokenResponse, settings.language.code, settings.ttsEnabled)
                result.displayMessage
            }
            is ActionExecutionResult.Failure -> result.errorMessage
            is ActionExecutionResult.NeedsConfirmation -> result.promptMessage
        }
        repository.logCommandExecution(
            transcript = "Routine: ${routine.name}",
            intentName = VoiceIntentType.RUN_ROUTINE.name,
            slotsJson = command.slotsToCompactJson(),
            responseText = displayMsg,
            status = "EXECUTED",
            confidencePercent = 100,
            latencyMs = 2L
        )
        localState.update { it.copy(lastRunBanner = displayMsg) }
    }

    fun dismissBanner() {
        localState.update { it.copy(lastRunBanner = null) }
    }

    private data class LocalRoutinesState(
        val lastRunBanner: String? = null,
        val showCreateSheet: Boolean = false
    )
}
