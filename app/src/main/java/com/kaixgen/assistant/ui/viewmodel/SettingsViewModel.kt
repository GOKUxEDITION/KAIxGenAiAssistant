package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.AssistantSettings
import com.kaixgen.assistant.domain.model.SupportedLanguage
import com.kaixgen.assistant.domain.model.ThemePreference
import com.kaixgen.assistant.domain.model.WakeSensitivity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val repository: KaiLocalRepository
) : ViewModel() {

    val uiState: StateFlow<AssistantSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssistantSettings()
        )

    fun onSelectLanguage(language: SupportedLanguage) = repository.updateLanguage(language)
    fun onToggleWakeWord(enabled: Boolean) = repository.updateWakeWordEnabled(enabled)
    fun onSelectSensitivity(sensitivity: WakeSensitivity) = repository.updateWakeSensitivity(sensitivity)
    fun onToggleTts(enabled: Boolean) = repository.updateTtsEnabled(enabled)
    fun onSelectTheme(preference: ThemePreference) = repository.updateThemePreference(preference)
    fun onToggleDynamicColor(enabled: Boolean) = repository.updateDynamicColor(enabled)
    fun onDeleteAllData() = repository.deleteAllLocalData()
}
