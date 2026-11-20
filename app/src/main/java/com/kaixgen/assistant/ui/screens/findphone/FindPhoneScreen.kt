package com.kaixgen.assistant.ui.screens.findphone

import android.media.Ringtone
import android.media.RingtoneManager
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.domain.model.FindPhoneConfig
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.components.KaiCircularIconBadge
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiTwoToneHeadline
import com.kaixgen.assistant.ui.theme.KaiBlueDeep
import com.kaixgen.assistant.ui.theme.KaiCyanAccent
import com.kaixgen.assistant.ui.theme.KaiDangerRed
import com.kaixgen.assistant.ui.theme.KaiGreenSuccess
import com.kaixgen.assistant.ui.theme.KaiTheme
import kotlinx.coroutines.delay

/**
 * Full Safety & Find Phone Section (PRD S4-01 / S4-02 / S4-03 / S4-07 / S4-08 / S4-09):
 * - Find Phone Ring Hero + Voice / Clap / Flashlight / Vibrate toggles
 * - Safe-Walk Check-In Countdown Timer (triggers Find Phone alarm if timer hits zero)
 * - Quick Fake Call Simulator UI (for personal safety exit situations)
 * - On-device Emergency ICE Card
 */
@Composable
fun FindPhoneScreen(
    uiState: FindPhoneConfig,
    isWakeWordActive: Boolean,
    onToggleRing: () -> Unit,
    onStopRinging: () -> Unit,
    onToggleVoiceTrigger: (Boolean) -> Unit,
    onToggleDoubleClap: (Boolean) -> Unit,
    onToggleFlashlight: (Boolean) -> Unit,
    onToggleVibrate: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = KaiTheme.extendedColors
    var activeRingtone by remember { mutableStateOf<Ringtone?>(null) }

    // Safe-Walk Countdown State
    var safeWalkRemainingSec by remember { mutableIntStateOf(0) }
    var isSafeWalkActive by remember { mutableStateOf(false) }

    // Fake Call State
    var showFakeCallDialog by remember { mutableStateOf(false) }
    var fakeCallerName by remember { mutableStateOf("Mom") }
    var fakeCallAnswered by remember { mutableStateOf(false) }

    // Emergency ICE Card State
    var iceContactName by remember { mutableStateOf("Home Emergency") }
    var iceBloodGroup by remember { mutableStateOf("O+") }
    var isEditingIce by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isRinging || (showFakeCallDialog && !fakeCallAnswered)) {
        val shouldPlaySound = uiState.isRinging || (showFakeCallDialog && !fakeCallAnswered)
        if (shouldPlaySound) {
            try {
                val uri = if (showFakeCallDialog && !fakeCallAnswered) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
                val ringtone = RingtoneManager.getRingtone(context, uri)
                activeRingtone = ringtone
                ringtone?.play()
            } catch (_: Exception) {
                // Gracefully degrade on devices/emulators without alarm stream audio
            }
        } else {
            try {
                activeRingtone?.stop()
                activeRingtone = null
            } catch (_: Exception) {
                activeRingtone = null
            }
        }
    }

    LaunchedEffect(isSafeWalkActive, safeWalkRemainingSec) {
        if (isSafeWalkActive && safeWalkRemainingSec > 0) {
            delay(1_000)
            safeWalkRemainingSec -= 1
            if (safeWalkRemainingSec == 0) {
                isSafeWalkActive = false
                if (!uiState.isRinging) {
                    onToggleRing()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                activeRingtone?.stop()
            } catch (_: Exception) {
                // Ignore cleanup failure
            }
            onStopRinging()
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isRinging) 1.24f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (uiState.isRinging) 650 else 1_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (uiState.isRinging) KaiDangerRed else MaterialTheme.colorScheme.primary,
        label = "ring_button_color"
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            KaiTwoToneHeadline(
                prefix = stringResource(R.string.find_phone_heading_prefix),
                highlighted = stringResource(R.string.find_phone_heading_highlight)
            )

            Text(
                text = stringResource(R.string.find_phone_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Animated Ring / Stop Hero Button
            Box(
                modifier = Modifier.size(196.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(buttonColor.copy(alpha = if (uiState.isRinging) 0.22f else 0.14f))
                )
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (uiState.isRinging) {
                                    listOf(KaiDangerRed, Color(0xFFFF6B5B))
                                } else {
                                    listOf(MaterialTheme.colorScheme.primary, KaiCyanAccent)
                                }
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = onToggleRing
                        )
                        .testTag("find_phone_ring_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (uiState.isRinging) Icons.Rounded.Stop else Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.isRinging) {
                                stringResource(R.string.find_phone_stop_button)
                            } else {
                                stringResource(R.string.find_phone_ring_button)
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Trigger & Alert Toggle Cards
            FindPhoneOptionCard(
                icon = Icons.Rounded.RecordVoiceOver,
                iconContainer = extendedColors.micContainer,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Voice Trigger Phrase",
                subtitle = "\u201C${uiState.voiceTriggerPhrase}\u201D",
                checked = uiState.isVoiceTriggerEnabled,
                onCheckedChange = onToggleVoiceTrigger,
                testTag = "find_phone_voice_trigger_switch"
            )

            FindPhoneOptionCard(
                icon = Icons.Rounded.GraphicEq,
                iconContainer = extendedColors.lockContainer,
                iconTint = extendedColors.success,
                title = "Double-Clap Detection",
                subtitle = "Clap twice within 1 second to ring phone",
                checked = uiState.isDoubleClapEnabled,
                onCheckedChange = onToggleDoubleClap,
                testTag = "find_phone_clap_switch"
            )

            FindPhoneOptionCard(
                icon = Icons.Rounded.FlashlightOn,
                iconContainer = extendedColors.warningContainer,
                iconTint = extendedColors.warning,
                title = "Flashlight Strobe",
                subtitle = "Blink rear LED while ringing in dark rooms",
                checked = uiState.isFlashlightStrobeEnabled,
                onCheckedChange = onToggleFlashlight,
                testTag = "find_phone_flash_switch"
            )

            FindPhoneOptionCard(
                icon = Icons.Rounded.Vibration,
                iconContainer = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "Max Vibration Pattern",
                subtitle = "Strong pulse even when device is on Silent",
                checked = uiState.isVibrateEnabled,
                onCheckedChange = onToggleVibrate,
                testTag = "find_phone_vibrate_switch"
            )

            // Safe-Walk Check-In Timer Card (PRD S4-08)
            KaiElevatedCard(modifier = Modifier.testTag("safety_safe_walk_card")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KaiCircularIconBadge(
                            icon = Icons.Rounded.DirectionsWalk,
                            containerColor = extendedColors.micContainer,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Safe-Walk Check-In Timer",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isSafeWalkActive) {
                                    val mins = safeWalkRemainingSec / 60
                                    val secs = safeWalkRemainingSec % 60
                                    "Check-in due in %02d:%02d — alarm rings if missed".format(mins, secs)
                                } else {
                                    "Auto-rings siren alert if you don\u2019t check in on time"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSafeWalkActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isSafeWalkActive) {
                        Button(
                            onClick = {
                                isSafeWalkActive = false
                                safeWalkRemainingSec = 0
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = KaiGreenSuccess),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("safe_walk_im_safe_button")
                        ) {
                            Text("I\u2019m Safe — Cancel Timer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5 to "5 min", 10 to "10 min", 15 to "15 min").forEach { (mins, label) ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        safeWalkRemainingSec = mins * 60
                                        isSafeWalkActive = true
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Fake Call Quick Exit Card (PRD S4-07)
            KaiElevatedCard(modifier = Modifier.testTag("safety_fake_call_card")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KaiCircularIconBadge(
                        icon = Icons.Rounded.Call,
                        containerColor = extendedColors.lockContainer,
                        iconTint = extendedColors.success
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fake Call Helper",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Simulate an incoming call from \u201C$fakeCallerName\u201D anytime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            fakeCallAnswered = false
                            showFakeCallDialog = true
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("trigger_fake_call_button")
                    ) {
                        Text("Trigger")
                    }
                }
            }

            // Emergency ICE Info Card (PRD S4-09)
            KaiElevatedCard(modifier = Modifier.testTag("safety_ice_card")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KaiCircularIconBadge(
                                icon = Icons.Rounded.MedicalServices,
                                containerColor = extendedColors.warningContainer,
                                iconTint = extendedColors.warning
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Emergency ICE Card",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Contact: $iceContactName \u2022 Blood Group: $iceBloodGroup",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = { isEditingIce = !isEditingIce }) {
                            Text(if (isEditingIce) "Done" else "Edit")
                        }
                    }

                    AnimatedVisibility(visible = isEditingIce) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = iceContactName,
                                onValueChange = { iceContactName = it },
                                label = { Text("Emergency Contact Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = iceBloodGroup,
                                onValueChange = { iceBloodGroup = it },
                                label = { Text("Blood Group & Notes") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            KaiBrandSignatureBadge()

            Spacer(modifier = Modifier.height(104.dp))
        }
    }

    if (showFakeCallDialog) {
        AlertDialog(
            onDismissRequest = { showFakeCallDialog = false },
            containerColor = KaiBlueDeep,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = null,
                    tint = KaiGreenSuccess,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = fakeCallerName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (fakeCallAnswered) "Call connected • 00:04" else "Incoming mobile call...",
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                if (!fakeCallAnswered) {
                    Button(
                        onClick = { fakeCallAnswered = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KaiGreenSuccess)
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Answer")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showFakeCallDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = KaiDangerRed)
                ) {
                    Icon(Icons.Rounded.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("End")
                }
            }
        )
    }
}

@Composable
private fun FindPhoneOptionCard(
    icon: ImageVector,
    iconContainer: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    KaiElevatedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KaiCircularIconBadge(
                icon = icon,
                containerColor = iconContainer,
                iconTint = iconTint
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
