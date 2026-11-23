package com.kaixgen.assistant.ui.screens.voicelock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.domain.model.SecurityMode
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.viewmodel.VoiceLockUiState

private val IosBlue = Color(0xFF0A84FF)
private val IosGreen = Color(0xFF30D158)
private val IosAmber = Color(0xFFFF9F0A)
private val IosRed = Color(0xFFFF453A)
private val IosPurple = Color(0xFFBF5AF2)

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
    val config = uiState.config
    val accentColor by animateColorAsState(
        targetValue = if (config.isVoiceLockEnabled) IosGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "shield_accent"
    )
    val pulseTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (config.isVoiceLockEnabled) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KaiBrandTopBar(
            isListeningActive = isWakeWordActive,
            onSettingsClick = onNavigateToSettings
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_lock_master_card"),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        0.5.dp,
                        if (config.isVoiceLockEnabled) IosGreen.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.14f))
                            )
                            Icon(
                                imageVector = if (config.isVoiceLockEnabled) Icons.Rounded.VerifiedUser else Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Lock",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (config.isVoiceLockEnabled) "Shield Active • On-device" else "Paused • Tap switch to arm",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (config.isVoiceLockEnabled) IosGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Switch(
                            checked = config.isVoiceLockEnabled,
                            onCheckedChange = onToggleVoiceLock,
                            modifier = Modifier.testTag("voice_lock_master_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IosGreen
                            )
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.showPinRequiredWarning) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = IosAmber.copy(alpha = 0.14f),
                        border = BorderStroke(0.5.dp, IosAmber.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = IosAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Backup PIN required",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Set a fallback PIN before arming Voice Lock.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = {
                                    onDismissPinWarning()
                                    onNavigateToSetPasscode()
                                },
                                modifier = Modifier.testTag("warning_set_pin_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Set PIN",
                                    color = IosAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CREDENTIALS & VAULT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            VoiceLockInsetRow(
                                icon = Icons.Rounded.Mic,
                                iconTint = IosBlue,
                                title = "Voice Phrase",
                                subtitle = "\"${config.voicePhrase}\" • ${config.voicePhraseLanguage.displayName}",
                                trailingLabel = "Edit",
                                rowTestTag = "voice_phrase_card",
                                actionTestTag = "change_voice_phrase_button",
                                onClick = onNavigateToSetPhrase
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            VoiceLockInsetRow(
                                icon = Icons.Rounded.Lock,
                                iconTint = if (config.hasBackupPasscode) IosGreen else IosAmber,
                                title = "Backup Passcode",
                                subtitle = if (config.hasBackupPasscode) {
                                    "${config.backupPinLength}-digit PIN • ${config.lastPinUpdatedLabel}"
                                } else {
                                    "Not configured yet"
                                },
                                trailingLabel = if (config.hasBackupPasscode) "Change" else "Set",
                                rowTestTag = "backup_passcode_card",
                                actionTestTag = "change_backup_passcode_button",
                                onClick = onNavigateToSetPasscode
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            VoiceLockInsetRow(
                                icon = Icons.Rounded.GridView,
                                iconTint = IosPurple,
                                title = "App Lock Vault",
                                subtitle = "${uiState.lockedAppCount} protected • Re-lock ${config.appLockGracePeriod.label}",
                                trailingLabel = "Manage",
                                rowTestTag = "app_lock_card",
                                actionTestTag = null,
                                onClick = onNavigateToAppLock
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PROTECTION LEVEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_status_card"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SecurityMode.entries.forEach { mode ->
                                        val isSelected = config.securityMode == mode
                                        val label = mode.name.lowercase().replaceFirstChar { it.uppercase() }
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .testTag("security_mode_${mode.name.lowercase()}")
                                                .clickable { onSelectSecurityMode(mode) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) IosBlue else Color.Transparent
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = config.securityMode.shortDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Last Activity",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = config.lastUnlockEvent,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = onOpenTestLock,
                                    modifier = Modifier.testTag("test_lock_button"),
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IosBlue,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Test Lock",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    KaiBrandSignatureBadge()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (uiState.showTestLockDialog) {
        TestVoiceLockDialog(
            expectedPhrase = config.voicePhrase,
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
private fun VoiceLockInsetRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingLabel: String,
    rowTestTag: String,
    actionTestTag: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(rowTestTag)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (actionTestTag != null) {
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(actionTestTag)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            } else {
                Modifier
            }
        ) {
            Text(
                text = trailingLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = if (isUnlocked) IosGreen else IosBlue
                )
                Text(
                    text = if (isUnlocked) "Shield Unlocked" else "Test Voice Lock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mode: ${securityMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !usePinTab,
                        onClick = { usePinTab = false },
                        label = { Text("Voice Phrase") }
                    )
                    FilterChip(
                        selected = usePinTab,
                        onClick = { usePinTab = true },
                        label = { Text("Backup PIN") }
                    )
                }

                if (!usePinTab) {
                    OutlinedTextField(
                        value = phraseInput,
                        onValueChange = { phraseInput = it },
                        label = { Text("Say or type phrase") },
                        placeholder = { Text(expectedPhrase) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Enter Backup PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!feedbackMessage.isNullOrBlank()) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isUnlocked) IosGreen else IosRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (usePinTab) onTestPin(pinInput) else onTestVoice(phraseInput)
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue)
            ) {
                Text(if (usePinTab) "Verify PIN" else "Verify Voice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
