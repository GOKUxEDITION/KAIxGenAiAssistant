package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.SupportedLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SetVoicePhraseUiState(
    val savedPhrase: String = "Hare Krishna",
    val selectedLanguage: SupportedLanguage = SupportedLanguage.HINGLISH,
    val hasBackupPasscode: Boolean = true,
    val isVoiceprintEnrolled: Boolean = true,
    val showRecordSheet: Boolean = false,
    val isRecording: Boolean = false,
    val draftPhrase: String = "",
    val enrollmentStep: Int = 1,
    val qualityScore: Float = 0.0f,
    val errorMessage: String? = null,
    val savedConfirmationBanner: Boolean = false
)

class SetVoicePhraseViewModel(
    private val repository: KaiLocalRepository
) : ViewModel() {

    private val localState = MutableStateFlow(LocalPhraseState())

    val uiState: StateFlow<SetVoicePhraseUiState> = combine(
        repository.voiceLockConfig,
        localState
    ) { config, local ->
        SetVoicePhraseUiState(
            savedPhrase = config.voicePhrase,
            selectedLanguage = local.selectedLanguage ?: config.voicePhraseLanguage,
            hasBackupPasscode = config.hasBackupPasscode,
            isVoiceprintEnrolled = config.isVoiceprintEnrolled,
            showRecordSheet = local.showRecordSheet,
            isRecording = local.isRecording,
            draftPhrase = local.draftPhrase,
            enrollmentStep = local.enrollmentStep,
            qualityScore = local.qualityScore,
            errorMessage = local.errorMessage,
            savedConfirmationBanner = local.savedConfirmationBanner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SetVoicePhraseUiState()
    )

    fun onSelectLanguage(language: SupportedLanguage) {
        localState.update { it.copy(selectedLanguage = language) }
    }

    fun openRecordSheet() {
        localState.update {
            it.copy(
                showRecordSheet = true,
                draftPhrase = repository.voiceLockConfig.value.voicePhrase,
                isRecording = false,
                enrollmentStep = 1,
                qualityScore = if (repository.voiceLockConfig.value.voicePhrase.isNotBlank()) 0.88f else 0f,
                errorMessage = null,
                savedConfirmationBanner = false
            )
        }
    }

    fun closeRecordSheet() {
        localState.update { it.copy(showRecordSheet = false, isRecording = false, errorMessage = null) }
    }

    fun onStartListening() {
        localState.update { it.copy(isRecording = true, errorMessage = null) }
    }

    fun onStopListening() {
        localState.update { it.copy(isRecording = false) }
    }

    fun onSpeechRecognized(heardText: String) {
        val cleaned = heardText.trim()
        if (cleaned.length < 3) {
            localState.update {
                it.copy(
                    isRecording = false,
                    errorMessage = "Phrase is too short. Say 2 to 3 clear words."
                )
            }
            return
        }
        localState.update { current ->
            val nextStep = (current.enrollmentStep + 1).coerceAtMost(3)
            current.copy(
                isRecording = false,
                draftPhrase = cleaned,
                enrollmentStep = nextStep,
                qualityScore = (0.72f + nextStep * 0.09f).coerceAtMost(0.98f),
                errorMessage = null
            )
        }
    }

    fun onSpeechError(message: String) {
        localState.update {
            it.copy(
                isRecording = false,
                errorMessage = message
            )
        }
    }

    fun onDraftPhraseTyped(text: String) {
        localState.update {
            it.copy(
                draftPhrase = text,
                qualityScore = if (text.trim().split(" ").size >= 2) 0.90f else 0.55f,
                errorMessage = null
            )
        }
    }

    fun onConfirmSavePhrase() {
        val current = localState.value
        val phraseToSave = current.draftPhrase.trim()
        if (phraseToSave.length < 3) {
            localState.update { it.copy(errorMessage = "Please speak or enter at least 2 words.") }
            return
        }
        val language = current.selectedLanguage ?: repository.voiceLockConfig.value.voicePhraseLanguage
        repository.updateVoicePhrase(phraseToSave, language)
        localState.update {
            it.copy(
                showRecordSheet = false,
                isRecording = false,
                savedConfirmationBanner = true,
                errorMessage = null
            )
        }
    }

    fun dismissSavedBanner() {
        localState.update { it.copy(savedConfirmationBanner = false) }
    }

    private data class LocalPhraseState(
        val selectedLanguage: SupportedLanguage? = null,
        val showRecordSheet: Boolean = false,
        val isRecording: Boolean = false,
        val draftPhrase: String = "",
        val enrollmentStep: Int = 1,
        val qualityScore: Float = 0f,
        val errorMessage: String? = null,
        val savedConfirmationBanner: Boolean = false
    )
}
