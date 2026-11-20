package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.VoiceAliasItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CommandHelpViewModel(
    private val repository: KaiLocalRepository
) : ViewModel() {

    val aliases: StateFlow<List<VoiceAliasItem>> = repository.aliases
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onAddAlias(kind: String, spoken: String, target: String) {
        repository.addAlias(kind, spoken, target)
    }

    fun onDeleteAlias(id: Long) {
        repository.deleteAlias(id)
    }
}
