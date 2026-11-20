package com.kaixgen.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.domain.model.AppLockGracePeriod
import com.kaixgen.assistant.domain.model.AppLockItem
import com.kaixgen.assistant.domain.model.SecurityMode
import com.kaixgen.assistant.domain.model.VoiceLockConfig
import com.kaixgen.assistant.security.PinVerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceLockUiState(
    val config: VoiceLockConfig = VoiceLockConfig(),
    val lockedApps: List<AppLockItem> = emptyList(),
    val appSearchQuery: String = "",
    val showPinRequiredWarning: Boolean = false,
    val showTestLockDialog: Boolean = false,
    val testLockMessage: String? = null,
    val testLockUnlocked: Boolean = false,
    val failedPinAttempts: Int = 0
) {
    val lockedAppCount: Int get() = lockedApps.count { it.isLocked }
    val filteredApps: List<AppLockItem>
        get() = if (appSearchQuery.isBlank()) {
            lockedApps
        } else {
            lockedApps.filter {
                it.appName.contains(appSearchQuery, ignoreCase = true) ||
                    it.category.contains(appSearchQuery, ignoreCase = true)
            }
        }
}

class VoiceLockViewModel(
    private val repository: KaiLocalRepository
) : ViewModel() {

    private val localUiState = MutableStateFlow(LocalVoiceLockState())

    val uiState: StateFlow<VoiceLockUiState> = combine(
        repository.voiceLockConfig,
        repository.lockedApps,
        localUiState
    ) { config, apps, local ->
        VoiceLockUiState(
            config = config,
            lockedApps = apps,
            appSearchQuery = local.appSearchQuery,
            showPinRequiredWarning = local.showPinRequiredWarning,
            showTestLockDialog = local.showTestLockDialog,
            testLockMessage = local.testLockMessage,
            testLockUnlocked = local.testLockUnlocked,
            failedPinAttempts = local.failedPinAttempts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VoiceLockUiState()
    )

    fun onToggleVoiceLock(enabled: Boolean) {
        val success = repository.setVoiceLockEnabled(enabled)
        if (!success) {
            localUiState.update { it.copy(showPinRequiredWarning = true) }
        } else {
            localUiState.update { it.copy(showPinRequiredWarning = false) }
        }
    }

    fun dismissPinWarning() {
        localUiState.update { it.copy(showPinRequiredWarning = false) }
    }

    fun onSelectSecurityMode(mode: SecurityMode) {
        repository.updateSecurityMode(mode)
    }

    fun onUpdateGracePeriod(period: AppLockGracePeriod) {
        repository.updateGracePeriod(period)
    }

    fun onToggleAppLock(packageName: String) {
        repository.toggleAppLock(packageName)
    }

    fun onAppSearchQueryChange(query: String) {
        localUiState.update { it.copy(appSearchQuery = query) }
    }

    fun openTestLockPreview() {
        localUiState.update {
            it.copy(
                showTestLockDialog = true,
                testLockMessage = null,
                testLockUnlocked = false,
                failedPinAttempts = 0
            )
        }
    }

    fun dismissTestLockPreview() {
        localUiState.update {
            it.copy(
                showTestLockDialog = false,
                testLockMessage = null,
                testLockUnlocked = false
            )
        }
    }

    fun onTestVoiceUnlock(spokenText: String) {
        val targetPhrase = repository.voiceLockConfig.value.voicePhrase.trim()
        val matched = spokenText.trim().equals(targetPhrase, ignoreCase = true) ||
            spokenText.trim().contains(targetPhrase, ignoreCase = true)
        if (matched && targetPhrase.isNotEmpty()) {
            repository.recordUnlockEvent("Test unlock verified • Voice + ${repository.voiceLockConfig.value.securityMode.badgeText}")
            localUiState.update {
                it.copy(
                    testLockUnlocked = true,
                    testLockMessage = "Voice phrase & speaker verified! Unlocked."
                )
            }
        } else {
            localUiState.update {
                it.copy(
                    testLockUnlocked = false,
                    testLockMessage = "Phrase didn't match \"$targetPhrase\". Try again or use Backup PIN."
                )
            }
        }
    }

    fun onTestPinUnlock(pin: String) {
        viewModelScope.launch {
            when (val result = repository.verifyBackupPasscodeSecure(pin)) {
                is PinVerificationResult.Success -> {
                    repository.recordUnlockEvent("Test unlock verified • Backup PIN (Keystore Vault)")
                    localUiState.update {
                        it.copy(
                            testLockUnlocked = true,
                            failedPinAttempts = 0,
                            testLockMessage = "Backup PIN verified via Keystore Vault! Unlocked."
                        )
                    }
                }
                is PinVerificationResult.WrongPin -> {
                    localUiState.update {
                        it.copy(
                            failedPinAttempts = result.failedCount,
                            testLockMessage = "Incorrect PIN (${result.attemptsBeforeCooldown} attempts left before cooldown)."
                        )
                    }
                }
                is PinVerificationResult.Cooldown -> {
                    localUiState.update {
                        it.copy(
                            testLockMessage = "Too many failed attempts. Wait ${result.remainingSeconds}s before retrying."
                        )
                    }
                }
                is PinVerificationResult.NotConfigured -> {
                    localUiState.update {
                        it.copy(testLockMessage = "No backup PIN configured yet.")
                    }
                }
            }
        }
    }

    fun onBiometricUnlockSuccess(authLabel: String) {
        repository.recordUnlockEvent("Test unlock verified • $authLabel")
        localUiState.update {
            it.copy(
                testLockUnlocked = true,
                failedPinAttempts = 0,
                testLockMessage = "Verified via $authLabel! Unlocked."
            )
        }
    }

    fun onBiometricFeedback(message: String) {
        localUiState.update {
            it.copy(
                testLockUnlocked = false,
                testLockMessage = message
            )
        }
    }

    private data class LocalVoiceLockState(
        val appSearchQuery: String = "",
        val showPinRequiredWarning: Boolean = false,
        val showTestLockDialog: Boolean = false,
        val testLockMessage: String? = null,
        val testLockUnlocked: Boolean = false,
        val failedPinAttempts: Int = 0
    )
}
