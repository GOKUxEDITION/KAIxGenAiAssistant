package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.FindPhoneConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FindPhoneViewModel(
    private val repository: KaiLocalRepository
) : ViewModel() {

    val uiState: StateFlow<FindPhoneConfig> = repository.findPhoneConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FindPhoneConfig()
        )

    fun onToggleRing() {
        repository.toggleFindPhoneRinging(!uiState.value.isRinging)
    }

    fun onStopRinging() {
        repository.toggleFindPhoneRinging(false)
    }

    fun onToggleVoiceTrigger(enabled: Boolean) {
        repository.updateFindPhoneConfig { it.copy(isVoiceTriggerEnabled = enabled) }
    }

    fun onToggleDoubleClap(enabled: Boolean) {
        repository.updateFindPhoneConfig { it.copy(isDoubleClapEnabled = enabled) }
    }

    fun onToggleFlashlight(enabled: Boolean) {
        repository.updateFindPhoneConfig { it.copy(isFlashlightStrobeEnabled = enabled) }
    }

    fun onToggleVibrate(enabled: Boolean) {
        repository.updateFindPhoneConfig { it.copy(isVibrateEnabled = enabled) }
    }
}
