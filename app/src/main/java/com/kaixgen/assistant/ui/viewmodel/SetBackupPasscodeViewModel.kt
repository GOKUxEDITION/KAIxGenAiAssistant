package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.kaixgen.assistant.data.local.KaiLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PasscodeStep {
    ENTER_PIN,
    CONFIRM_PIN
}

data class SetBackupPasscodeUiState(
    val pinLength: Int = 4,
    val step: PasscodeStep = PasscodeStep.ENTER_PIN,
    val firstPin: String = "",
    val currentInput: String = "",
    val isPinVisible: Boolean = false,
    val errorMessage: String? = null,
    val strengthLabel: String = "",
    val isWeakPinWarning: Boolean = false,
    val isSetupComplete: Boolean = false
) {
    val canProceed: Boolean get() = currentInput.length == pinLength
}

class SetBackupPasscodeViewModel(
    private val repository: KaiLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SetBackupPasscodeUiState(pinLength = repository.voiceLockConfig.value.backupPinLength)
    )
    val uiState: StateFlow<SetBackupPasscodeUiState> = _uiState.asStateFlow()

    fun onTogglePinLength(length: Int) {
        if (length != 4 && length != 6) return
        _uiState.update {
            it.copy(
                pinLength = length,
                step = PasscodeStep.ENTER_PIN,
                firstPin = "",
                currentInput = "",
                errorMessage = null,
                strengthLabel = "",
                isWeakPinWarning = false
            )
        }
    }

    fun onDigitPressed(digit: Char) {
        if (!digit.isDigit()) return
        _uiState.update { state ->
            if (state.currentInput.length >= state.pinLength) return@update state
            val updated = state.currentInput + digit
            val (strength, isWeak) = evaluateStrength(updated, state.pinLength)
            state.copy(
                currentInput = updated,
                errorMessage = null,
                strengthLabel = strength,
                isWeakPinWarning = isWeak
            )
        }
    }

    fun onBackspacePressed() {
        _uiState.update { state ->
            if (state.currentInput.isEmpty()) return@update state
            val updated = state.currentInput.dropLast(1)
            val (strength, isWeak) = evaluateStrength(updated, state.pinLength)
            state.copy(
                currentInput = updated,
                errorMessage = null,
                strengthLabel = strength,
                isWeakPinWarning = isWeak
            )
        }
    }

    fun onToggleVisibility() {
        _uiState.update { it.copy(isPinVisible = !it.isPinVisible) }
    }

    fun onBackStep(): Boolean {
        val state = _uiState.value
        return if (state.step == PasscodeStep.CONFIRM_PIN) {
            _uiState.update {
                it.copy(
                    step = PasscodeStep.ENTER_PIN,
                    currentInput = it.firstPin,
                    errorMessage = null
                )
            }
            true
        } else {
            false
        }
    }

    fun onPrimaryAction(): Boolean {
        val state = _uiState.value
        if (state.currentInput.length < state.pinLength) {
            _uiState.update {
                it.copy(errorMessage = "Please enter all ${state.pinLength} digits.")
            }
            return false
        }

        return when (state.step) {
            PasscodeStep.ENTER_PIN -> {
                _uiState.update {
                    it.copy(
                        step = PasscodeStep.CONFIRM_PIN,
                        firstPin = state.currentInput,
                        currentInput = "",
                        errorMessage = null
                    )
                }
                false
            }
            PasscodeStep.CONFIRM_PIN -> {
                if (state.currentInput == state.firstPin) {
                    repository.updateBackupPasscode(state.currentInput)
                    _uiState.update {
                        it.copy(
                            isSetupComplete = true,
                            errorMessage = null
                        )
                    }
                    true
                } else {
                    _uiState.update {
                        it.copy(
                            currentInput = "",
                            errorMessage = "Passcodes didn't match. Please re-enter."
                        )
                    }
                    false
                }
            }
        }
    }

    fun resetSetupFlag() {
        _uiState.update {
            it.copy(
                isSetupComplete = false,
                step = PasscodeStep.ENTER_PIN,
                firstPin = "",
                currentInput = ""
            )
        }
    }

    private fun evaluateStrength(pin: String, targetLength: Int): Pair<String, Boolean> {
        if (pin.length < targetLength) return "" to false
        val weakPins = setOf("0000", "1111", "1234", "2222", "4321", "9999", "000000", "123456")
        val allSame = pin.toSet().size == 1
        return if (pin in weakPins || allSame) {
            "Common PIN — consider a less predictable code" to true
        } else if (targetLength == 6) {
            "Strong 6-digit backup PIN" to false
        } else {
            "Good 4-digit backup PIN" to false
        }
    }
}
