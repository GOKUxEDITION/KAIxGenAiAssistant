package com.kaixgen.assistant.ui.screens.more

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.components.KaiCircularIconBadge
import com.kaixgen.assistant.ui.components.KaiDeveloperSignatureCard
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiTwoToneHeadline
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.MoreHubUiState

/**
 * Full "More" Hub Section (PRD S0 / S7 Productivity / S9 Driving / S11 Device & Tools / Signature):
 * 1. Live Device Status & Flashlight + Timer Utilities
 * 2. Driving Mode Launchpad (extra-large hands-free cards)
 * 3. Quick Productivity Tasks (on-device interactive list)
 * 4. Quick Access to Command Guide, OEM Diagnostics, and Settings & Privacy
 * 5. Full Developer & Brand Signature Card (Dipak Choudhary • GOKUxEDITION • KAIxGenShopLabs)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreHubScreen(
    uiState: MoreHubUiState,
    isWakeWordActive: Boolean,
    onRefreshDeviceStatus: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenDrivingMode: () -> Unit,
    onCloseDrivingMode: () -> Unit,
    onLaunchMaps: () -> Unit,
    onRunVoiceAction: (String) -> Unit,
    onLaunchTimer: (Int) -> Unit,
    onAddTask: (String) -> Unit,
    onToggleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDismissBanner: () -> Unit,
    onNavigateToVoiceBuddy: () -> Unit,
    onNavigateToCommandHelp: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSecurityHealth: () -> Unit = {},
    onNavigateToVoiceprint: () -> Unit = {},
    onNavigateToSafetyHub: () -> Unit = {},
    onNavigateToFullDriving: () -> Unit = {},
    onNavigateToInterpreterMemory: () -> Unit = {},
    onNavigateToProductivityHub: () -> Unit = {},
    onNavigateToPrivacyOem: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onNavigateToAiToolsHub: () -> Unit = {},
    onNavigateToAboutKai: () -> Unit = {},
    onNavigateToRealLifeHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors
    var newTaskInput by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            KaiTwoToneHeadline(
                prefix = "Tools & ",
                highlighted = "More"
            )

            AnimatedVisibility(visible = uiState.statusBanner != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = extendedColors.lockContainer,
                    border = BorderStroke(1.dp, extendedColors.success.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = extendedColors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.statusBanner.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissBanner, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Section 1: Live Device Utilities (Battery, Flashlight, Quick Timer)
            KaiElevatedCard(modifier = Modifier.testTag("more_device_utilities_card")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            KaiCircularIconBadge(
                                icon = if (uiState.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                                containerColor = extendedColors.lockContainer,
                                iconTint = extendedColors.success
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Device Status • ${uiState.batteryPercent}%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (uiState.isCharging) "Charging • Ready for background wake" else "On battery • Tap refresh to update",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onRefreshDeviceStatus,
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Refresh", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onToggleFlashlight,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isTorchOn) extendedColors.warning else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("more_torch_button")
                        ) {
                            Icon(Icons.Rounded.FlashlightOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isTorchOn) "Torch ON" else "Flashlight")
                        }
                        OutlinedButton(
                            onClick = { onLaunchTimer(5) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("more_timer_button")
                        ) {
                            Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("5m Timer")
                        }
                    }
                }
            }

            // Section 2: Driving Mode Launchpad (PRD S9-01)
            KaiElevatedCard(
                onClick = onOpenDrivingMode,
                modifier = Modifier.testTag("more_driving_mode_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KaiCircularIconBadge(
                        icon = Icons.Rounded.DirectionsCar,
                        containerColor = extendedColors.micContainer,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Driving Mode Launchpad",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Large hands-free tiles for Maps, Calls, Music & KAI Mic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


        // Section 2B: KAI v2.1 Pro Suite
        Text(
            text = "KAI v2.1 Pro Suite",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        MoreNavRowCard(
            icon = Icons.Rounded.Security,
            title = "Custom Voice Lock/Unlock & Anti-Theft",
            subtitle = "Custom lock/unlock words, anti-theft guard & Hinglish calculator",
            onClick = onNavigateToRealLifeHub,
            testTag = "more_real_life_hub_row"
        )
            MoreNavRowCard(
                icon = Icons.AutoMirrored.Rounded.Chat,
                title = "KAI AI Chat (Text + Hybrid AI)",
                subtitle = "Ask KAI anything with text & hybrid AI",
                onClick = onNavigateToAiChat,
                testTag = "more_ai_chat_row"
            )
            MoreNavRowCard(
                icon = Icons.Rounded.AutoAwesome,
                title = "KAI AI Tools Hub",
                subtitle = "Writing, summarize, translate & code tools",
                onClick = onNavigateToAiToolsHub,
                testTag = "more_ai_tools_hub_row"
            )
            MoreNavRowCard(
            icon = Icons.Rounded.Security,
            title = "Security Health & Intruder Vault",
            subtitle = "Security score, intruder selfies & lock hardening",
            onClick = onNavigateToSecurityHealth,
            testTag = "more_security_health_row"
        )
        MoreNavRowCard(
            icon = Icons.Rounded.GraphicEq,
            title = "Speaker Voiceprint Biometrics",
            subtitle = "Enroll & verify your personal voiceprint",
            onClick = onNavigateToVoiceprint,
            testTag = "more_voiceprint_row"
        )
        MoreNavRowCard(
            icon = Icons.Rounded.Shield,
            title = "Personal Safety, SOS & Fake Call",
            subtitle = "Emergency SOS, loud siren & fake incoming call",
            onClick = onNavigateToSafetyHub,
            testTag = "more_safety_hub_row"
        )
        MoreNavRowCard(
            icon = Icons.Rounded.DirectionsCar,
            title = "Full-Screen Driving Mode & Parking",
            subtitle = "Hands-free dashboard & save/find parked car",
            onClick = onNavigateToFullDriving,
            testTag = "more_full_driving_row"
        )
        MoreNavRowCard(
            icon = Icons.Rounded.Translate,
            title = "Hindi/English Interpreter & Memory Vault",
            subtitle = "Live bilingual translation & saved personal facts",
            onClick = onNavigateToInterpreterMemory,
            testTag = "more_interpreter_memory_row"
        )
        MoreNavRowCard(
            icon = Icons.Rounded.Timer,
            title = "Productivity: Expenses & Focus",
            subtitle = "Track daily expenses, Pomodoro focus & notes",
            onClick = onNavigateToProductivityHub,
            testTag = "more_productivity_hub_row"
        )
        MoreNavRowCard(
            icon = Icons.Rounded.PrivacyTip,
            title = "Privacy Center & OEM Battery Wizard",
            subtitle = "Permissions audit & background autostart setup",
            onClick = onNavigateToPrivacyOem,
            testTag = "more_privacy_oem_row"
        )
            MoreNavRowCard(
                icon = Icons.Rounded.Info,
                title = "About KAIxGenAssistant",
                subtitle = "App info, version & capabilities",
                onClick = onNavigateToAboutKai,
                testTag = "more_about_kai_row"
            )
            // Section 3: Productivity & Quick Tasks (PRD S7-05)
            KaiElevatedCard(modifier = Modifier.testTag("more_tasks_card")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick Tasks & Voice Reminders",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskInput,
                            onValueChange = { newTaskInput = it },
                            placeholder = { Text("Add quick task...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("more_task_input")
                        )
                        Button(
                            onClick = {
                                onAddTask(newTaskInput)
                                newTaskInput = ""
                            },
                            enabled = newTaskInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("more_add_task_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add task")
                        }
                    }

                    uiState.tasks.forEach { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { onToggleTask(task.id) }
                            )
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteTask(task.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Shortcuts to Guide, Diagnostics & Settings
            MoreNavRowCard(
                icon = Icons.Rounded.HelpOutline,
                title = "Voice Command Guide & Custom Aliases",
                subtitle = "Browse Hinglish commands & add personal contact aliases",
                onClick = onNavigateToCommandHelp,
                testTag = "more_command_guide_row"
            )

            MoreNavRowCard(
                icon = Icons.Rounded.Build,
                title = "Diagnostics & OEM Battery Guide",
                subtitle = "Run NLU health check & prevent background kills",
                onClick = onNavigateToDiagnostics,
                testTag = "more_diagnostics_row"
            )

            MoreNavRowCard(
                icon = Icons.Rounded.Settings,
                title = "Settings & Privacy Center",
                subtitle = "Languages, wake word sensitivity, theme & data wipe",
                onClick = onNavigateToSettings,
                testTag = "more_settings_row"
            )

            // Section 5: Full Developer & Brand Signature Card
            KaiDeveloperSignatureCard(
                modifier = Modifier.testTag("more_developer_signature_card")
            )

            Spacer(modifier = Modifier.height(104.dp))
        }
    }

    if (uiState.isDrivingModeSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onCloseDrivingMode,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Driving Mode — Hands-Free",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Large-target actions designed for quick eyes-free control.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DrivingTileButton(
                        icon = Icons.Rounded.Map,
                        label = "Maps",
                        onClick = {
                            onCloseDrivingMode()
                            onLaunchMaps()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DrivingTileButton(
                        icon = Icons.Rounded.Mic,
                        label = "Ask KAI",
                        onClick = {
                            onCloseDrivingMode()
                            onNavigateToVoiceBuddy()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DrivingTileButton(
                        icon = Icons.Rounded.Call,
                        label = "Quick Dial",
                        onClick = {
                            onCloseDrivingMode()
                            onRunVoiceAction("call")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DrivingTileButton(
                        icon = Icons.Rounded.MusicNote,
                        label = "Play Music",
                        onClick = {
                            onCloseDrivingMode()
                            onRunVoiceAction("play music")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreNavRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val extendedColors = KaiTheme.extendedColors
    KaiElevatedCard(
        onClick = onClick,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KaiCircularIconBadge(
                icon = icon,
                containerColor = extendedColors.micContainer,
                iconTint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DrivingTileButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier.height(84.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}
