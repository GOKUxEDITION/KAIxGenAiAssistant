package com.kaixgen.assistant.ui.screens.voicelock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.domain.model.SecurityMode
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.components.KaiCircularIconBadge
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.VoiceLockUiState

/**
 * Voice Lock Home Screen matching 02_voice_lock_home.jpg while implementing all PRD 04 audit fixes:
 * - Brand Blue active toggle (not orange)
 * - Protection Status & Security Mode card filling empty whitespace productively
 * - Test Lock interactive preview modal
 * - Mandatory Backup Passcode guard before enabling Voice Lock (FR-VL-03)
 */
@Composable
fun VoiceLockHomeScreen(
    uiState: VoiceLockUiState,
    isWakeWordActive: Boolean,
    onToggleVoiceLock: (Boolean) -> Unit,
    onDismissPinWarning: () -> Unit,
    onNavigateToSetPhrase: () -> Unit,
    onNavigateToSetPasscode: () -> Unit,
    onNavigateToAppLock: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectSecurityMode: (SecurityMode) -> Unit,
    onOpenTestLock: () -> Unit,
    onDismissTestLock: () -> Unit,
    onTestVoiceUnlock: (String) -> Unit,
    onTestPinUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors
    val config = uiState.config

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KaiBrandTopBar(
            isListeningActive = isWakeWordActive,
            onSettingsClick = onNavigateToSettings
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Master Voice Lock Toggle Card (Matches screenshot 2 top card with Brand Blue switch)
            KaiElevatedCard(
                modifier = Modifier.testTag("voice_lock_master_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.voice_lock_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(R.string.voice_lock_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = config.isVoiceLockEnabled,
                        onCheckedChange = onToggleVoiceLock,
                        modifier = Modifier.testTag("voice_lock_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Inline Warning if user tries enabling Voice Lock without Backup Passcode (FR-VL-03)
            AnimatedVisibility(visible = uiState.showPinRequiredWarning) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = extendedColors.warningContainer,
                    border = BorderStroke(1.dp, extendedColors.warning.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = extendedColors.warning,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.pin_required_banner),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TextButton(
                            onClick = {
                                onDismissPinWarning()
                                onNavigateToSetPasscode()
                            },
                            modifier = Modifier.testTag("warning_set_pin_button")
                        ) {
                            Text("Set PIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Voice Phrase Row Card (Matches screenshot 2 row 2)
            KaiElevatedCard(
                onClick = onNavigateToSetPhrase,
                modifier = Modifier.testTag("voice_phrase_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KaiCircularIconBadge(
                        icon = Icons.Rounded.Mic,
                        containerColor = extendedColors.micContainer,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.voice_phrase_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = config.voicePhrase.ifBlank { "Not configured yet" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = extendedColors.micContainer
                            ) {
                                Text(
                                    text = config.voicePhraseLanguage.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = onNavigateToSetPhrase,
                        modifier = Modifier.testTag("change_voice_phrase_button")
                    ) {
                        Text(
                            text = stringResource(R.string.action_change),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Backup Passcode Row Card (Matches screenshot 2 row 3)
            KaiElevatedCard(
                onClick = onNavigateToSetPasscode,
                modifier = Modifier.testTag("backup_passcode_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KaiCircularIconBadge(
                        icon = Icons.Rounded.Lock,
                        containerColor = extendedColors.lockContainer,
                        iconTint = extendedColors.success
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.backup_passcode_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (config.hasBackupPasscode) {
                                "${"\u2022".repeat(config.backupPinLength)}  \u2022  ${config.lastPinUpdatedLabel}"
                            } else {
                                "Required before enabling lock"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = onNavigateToSetPasscode,
                        modifier = Modifier.testTag("change_backup_passcode_button")
                    ) {
                        Text(
                            text = if (config.hasBackupPasscode) {
                                stringResource(R.string.action_change)
                            } else {
                                "Set PIN"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // App Lock with Voice Phrase Card (Matches screenshot 2 row 4)
            KaiElevatedCard(
                onClick = onNavigateToAppLock,
                modifier = Modifier.testTag("app_lock_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KaiCircularIconBadge(
                        icon = Icons.Rounded.GridView,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_lock_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (uiState.lockedAppCount > 0) {
                                "${uiState.lockedAppCount} apps protected \u2022 ${config.appLockGracePeriod.label}"
                            } else {
                                stringResource(R.string.app_lock_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Protection Status & Security Mode Card (PRD 04 Audit #8: utilizes whitespace cleanly)
            KaiElevatedCard(
                modifier = Modifier.testTag("security_status_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Protection Level",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenTestLock,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("test_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.action_test_lock),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecurityMode.entries.forEach { mode ->
                            val selected = config.securityMode == mode
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSecurityMode(mode) },
                                label = {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("security_mode_${mode.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text(
                        text = config.securityMode.shortDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = extendedColors.success,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = config.lastUnlockEvent,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Transparent Positioning Footer (PRD 06 §1)
            Text(
                text = stringResource(R.string.voice_lock_extra_layer_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KaiBrandSignatureBadge()
            }

            // Extra clearance above floating pill navigation bar
            Spacer(modifier = Modifier.height(104.dp))
        }
    }

    if (uiState.showTestLockDialog) {
        TestVoiceLockDialog(
            expectedPhrase = config.voicePhrase.ifBlank { "Hare Krishna" },
            securityMode = config.securityMode,
            isUnlocked = uiState.testLockUnlocked,
            feedbackMessage = uiState.testLockMessage,
            onDismiss = onDismissTestLock,
            onTestVoice = onTestVoiceUnlock,
            onTestPin = onTestPinUnlock
        )
    }
}

@Composable
private fun TestVoiceLockDialog(
    expectedPhrase: String,
    securityMode: SecurityMode,
    isUnlocked: Boolean,
    feedbackMessage: String?,
    onDismiss: () -> Unit,
    onTestVoice: (String) -> Unit,
    onTestPin: (String) -> Unit
) {
    var phraseInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var usePinTab by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (isUnlocked) KaiTheme.extendedColors.success else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        },
        title = {
            Text(
                text = if (isUnlocked) "Unlocked!" else "Test Voice Lock (${securityMode.badgeText})",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (usePinTab) {
                        "Enter your backup passcode to test fallback unlock."
                    } else {
                        "Simulate saying your unlock phrase: \"$expectedPhrase\""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isUnlocked) {
                    if (!usePinTab) {
                        OutlinedTextField(
                            value = phraseInput,
                            onValueChange = { phraseInput = it },
                            label = { Text("Spoken phrase") },
                            placeholder = { Text(expectedPhrase) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { input -> pinInput = input.filter { it.isDigit() }.take(6) },
                            label = { Text("Backup PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (feedbackMessage != null) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isUnlocked) KaiTheme.extendedColors.success else MaterialTheme.colorScheme.error
                    )
                }

                if (!isUnlocked) {
                    TextButton(onClick = { usePinTab = !usePinTab }) {
                        Text(if (usePinTab) "Switch to Voice Phrase" else "Use Backup PIN instead")
                    }
                }
            }
        },
        confirmButton = {
            if (isUnlocked) {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            } else {
                Button(
                    onClick = {
                        if (usePinTab) onTestPin(pinInput) else onTestVoice(phraseInput.ifBlank { expectedPhrase })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (usePinTab) "Verify PIN" else "Verify Phrase")
                }
            }
        },
        dismissButton = {
            if (!isUnlocked) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
