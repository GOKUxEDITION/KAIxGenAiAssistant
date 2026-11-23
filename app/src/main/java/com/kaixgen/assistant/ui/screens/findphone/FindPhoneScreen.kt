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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.domain.model.FindPhoneConfig
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import kotlinx.coroutines.delay

private val SafetyBlue = Color(0xFF0A84FF)
private val SafetyCyan = Color(0xFF64D2FF)
private val SafetyGreen = Color(0xFF30D158)
private val SafetyAmber = Color(0xFFFF9F0A)
private val SafetyRed = Color(0xFFFF453A)
private val SafetyPurple = Color(0xFFBF5AF2)

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

    LaunchedEffect(uiState.isRinging) {
        if (uiState.isRinging) {
            runCatching {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val ringtone = RingtoneManager.getRingtone(context, uri)
                activeRingtone = ringtone
                ringtone?.play()
            }
        } else {
            runCatching { activeRingtone?.stop() }
            activeRingtone = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { activeRingtone?.stop() }
            if (uiState.isRinging) onStopRinging()
        }
    }

    LaunchedEffect(isSafeWalkActive, safeWalkRemainingSec) {
        if (isSafeWalkActive && safeWalkRemainingSec > 0) {
            delay(1000L)
            safeWalkRemainingSec -= 1
        } else if (isSafeWalkActive && safeWalkRemainingSec == 0) {
            isSafeWalkActive = false
            if (!uiState.isRinging) onToggleRing()
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.isRinging) 1.18f else 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (uiState.isRinging) 650 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_scale"
    )
    val ringAccent by animateColorAsState(
        targetValue = if (uiState.isRinging) SafetyRed else SafetyBlue,
        animationSpec = tween(250),
        label = "ring_accent"
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
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Hero Acoustic Radar Ring
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Safety & Find Phone",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier.size(164.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(152.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(ringAccent.copy(alpha = if (uiState.isRinging) 0.22f else 0.12f))
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = if (uiState.isRinging) {
                                            listOf(SafetyRed, Color(0xFFB71C1C))
                                        } else {
                                            listOf(SafetyBlue, Color(0xFF0051B5))
                                        }
                                    )
                                )
                                .testTag("find_phone_ring_button")
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = onToggleRing
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (uiState.isRinging) Icons.Rounded.Stop else Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.isRinging) "Stop Ring" else "Ring Now",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = if (uiState.isRinging) {
                            "Alarm ringing at max volume • Tap to silence"
                        } else {
                            "Say \"${uiState.voiceTriggerPhrase}\" or double-clap to locate"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.isRinging) SafetyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Section 1: iOS Grouped Inset Acoustic Triggers
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LOCATOR TRIGGERS",
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
                            FindPhoneInsetToggleRow(
                                icon = Icons.Rounded.RecordVoiceOver,
                                iconTint = SafetyBlue,
                                title = "Voice Trigger",
                                subtitle = "\"${uiState.voiceTriggerPhrase}\"",
                                checked = uiState.isVoiceTriggerEnabled,
                                onCheckedChange = onToggleVoiceTrigger,
                                testTag = "find_phone_voice_trigger_switch"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                            FindPhoneInsetToggleRow(
                                icon = Icons.Rounded.GraphicEq,
                                iconTint = SafetyCyan,
                                title = "Double-Clap Radar",
                                subtitle = "On-device clap detection when idle",
                                checked = uiState.isDoubleClapEnabled,
                                onCheckedChange = onToggleDoubleClap,
                                testTag = "find_phone_clap_switch"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                            FindPhoneInsetToggleRow(
                                icon = Icons.Rounded.FlashlightOn,
                                iconTint = SafetyAmber,
                                title = "Flashlight Strobe",
                                subtitle = "Blink camera LED in dark rooms",
                                checked = uiState.isFlashlightStrobeEnabled,
                                onCheckedChange = onToggleFlashlight,
                                testTag = "find_phone_flash_switch"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                            FindPhoneInsetToggleRow(
                                icon = Icons.Rounded.Vibration,
                                iconTint = SafetyPurple,
                                title = "High-Pulse Vibrate",
                                subtitle = "Strong tactile pattern while ringing",
                                checked = uiState.isVibrateEnabled,
                                onCheckedChange = onToggleVibrate,
                                testTag = "find_phone_vibrate_switch"
                            )
                        }
                    }
                }

                // Section 2: Personal Safety Suite
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PERSONAL SAFETY",
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
                            // Safe-Walk Check-In
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("safety_safe_walk_card")
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SafetyGreen.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DirectionsWalk,
                                            contentDescription = null,
                                            tint = SafetyGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Safe-Walk Check-In",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isSafeWalkActive) {
                                                val mins = safeWalkRemainingSec / 60
                                                val secs = safeWalkRemainingSec % 60
                                                "Active • %02d:%02d remaining".format(mins, secs)
                                            } else {
                                                "Auto-alarm if you don't check in"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSafeWalkActive) SafetyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isSafeWalkActive) {
                                        listOf(5, 10, 15).forEach { mins ->
                                            FilterChip(
                                                selected = false,
                                                onClick = {
                                                    safeWalkRemainingSec = mins * 60
                                                    isSafeWalkActive = true
                                                },
                                                label = { Text("${mins}m") }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = {
                                            isSafeWalkActive = false
                                            safeWalkRemainingSec = 0
                                            if (uiState.isRinging) onStopRinging()
                                        },
                                        modifier = Modifier.testTag("safe_walk_im_safe_button"),
                                        shape = RoundedCornerShape(999.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSafeWalkActive) SafetyGreen else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            contentColor = if (isSafeWalkActive) Color.White else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "I'm Safe",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            // Fake Call Shield
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("safety_fake_call_card")
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SafetyBlue.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Call,
                                        contentDescription = null,
                                        tint = SafetyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Fake Call Shield",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Caller: $fakeCallerName • Instant exit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = {
                                        fakeCallAnswered = false
                                        showFakeCallDialog = true
                                    },
                                    modifier = Modifier.testTag("trigger_fake_call_button"),
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SafetyBlue,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Trigger",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            // Emergency ICE Medical Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("safety_ice_card")
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SafetyRed.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MedicalServices,
                                            contentDescription = null,
                                            tint = SafetyRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Emergency ICE Card",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$iceContactName • Blood: $iceBloodGroup",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    TextButton(
                                        onClick = { isEditingIce = !isEditingIce },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isEditingIce) "Done" else "Edit",
                                            color = SafetyBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isEditingIce) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = iceContactName,
                                            onValueChange = { iceContactName = it },
                                            label = { Text("Emergency Contact") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = iceBloodGroup,
                                            onValueChange = { iceBloodGroup = it },
                                            label = { Text("Blood Group") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
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

    if (showFakeCallDialog) {
        AlertDialog(
            onDismissRequest = { showFakeCallDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = if (fakeCallAnswered) "$fakeCallerName • Connected" else "Incoming Call",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (fakeCallAnswered) {
                            "00:08 • Speaker active (simulated call)"
                        } else {
                            "$fakeCallerName is calling your phone..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!fakeCallAnswered) {
                        OutlinedTextField(
                            value = fakeCallerName,
                            onValueChange = { fakeCallerName = it },
                            label = { Text("Caller Label") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!fakeCallAnswered) {
                    Button(
                        onClick = { fakeCallAnswered = true },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Answer")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        fakeCallAnswered = false
                        showFakeCallDialog = false
                    },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Rounded.CallEnd, contentDescription = null, tint = SafetyRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (fakeCallAnswered) "End Call" else "Decline", color = SafetyRed)
                }
            }
        )
    }
}

@Composable
private fun FindPhoneInsetToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SafetyGreen
            )
        )
    }
}
