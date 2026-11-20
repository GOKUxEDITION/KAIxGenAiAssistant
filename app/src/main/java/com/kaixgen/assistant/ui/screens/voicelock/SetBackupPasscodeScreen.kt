package com.kaixgen.assistant.ui.screens.voicelock

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiSubScreenTopBar
import com.kaixgen.assistant.ui.components.KaiTwoToneHeadline
import com.kaixgen.assistant.ui.theme.KaiBlueButton
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.PasscodeStep
import com.kaixgen.assistant.ui.viewmodel.SetBackupPasscodeUiState

/**
 * Set Backup Passcode Screen matching 04_set_backup_passcode.jpg while addressing all PRD 04 audit items:
 * - Two-step Confirm PIN flow (Enter -> Confirm -> Finish Setup)
 * - Clean dot/digit preview card with visibility eye toggle
 * - Custom accessible numeric keypad (48dp+ touch targets)
 * - 4-digit / 6-digit selector + PIN strength hint
 */
@Composable
fun SetBackupPasscodeScreen(
    uiState: SetBackupPasscodeUiState,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTogglePinLength: (Int) -> Unit,
    onDigitPressed: (Char) -> Unit,
    onBackspacePressed: () -> Unit,
    onToggleVisibility: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSetupFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors

    LaunchedEffect(uiState.isSetupComplete) {
        if (uiState.isSetupComplete) {
            onSetupFinished()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KaiSubScreenTopBar(
            onBackClick = onBackClick,
            onSettingsClick = onSettingsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.step == PasscodeStep.ENTER_PIN) {
                KaiTwoToneHeadline(
                    prefix = stringResource(R.string.set_pin_heading_prefix),
                    highlighted = stringResource(R.string.set_pin_heading_highlight)
                )
            } else {
                KaiTwoToneHeadline(
                    prefix = stringResource(R.string.confirm_pin_heading_prefix),
                    highlighted = stringResource(R.string.confirm_pin_heading_highlight)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    uiState.step == PasscodeStep.CONFIRM_PIN -> stringResource(R.string.confirm_pin_subtitle)
                    uiState.pinLength == 6 -> stringResource(R.string.set_pin_subtitle_6)
                    else -> stringResource(R.string.set_pin_subtitle_4)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4-digit vs 6-digit length chips (PRD FR-VL-06)
            AnimatedVisibility(visible = uiState.step == PasscodeStep.ENTER_PIN) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(4, 6).forEach { digits ->
                        FilterChip(
                            selected = uiState.pinLength == digits,
                            onClick = { onTogglePinLength(digits) },
                            label = { Text("$digits-digit PIN") },
                            modifier = Modifier.testTag("pin_length_$digits"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Elevated PIN Display Card matching 04_set_backup_passcode.jpg
            KaiElevatedCard(
                cornerRadius = 20.dp,
                modifier = Modifier.testTag("backup_pin_display_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (index in 0 until uiState.pinLength) {
                            val digit = uiState.currentInput.getOrNull(index)
                            PinSlotIndicator(
                                digit = digit,
                                isVisible = uiState.isPinVisible
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pin_visibility_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPinVisible) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = stringResource(R.string.cd_toggle_pin_visibility),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("pin_error_text")
                )
            } else if (uiState.strengthLabel.isNotEmpty() && uiState.step == PasscodeStep.ENTER_PIN) {
                Text(
                    text = uiState.strengthLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (uiState.isWeakPinWarning) extendedColors.warning else extendedColors.success,
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Full-width Royal Blue Pill Primary Action Button matching 04_set_backup_passcode.jpg
            Button(
                onClick = onPrimaryAction,
                enabled = uiState.canProceed,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KaiBlueButton,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("passcode_primary_button")
            ) {
                Text(
                    text = if (uiState.step == PasscodeStep.ENTER_PIN) {
                        stringResource(R.string.action_continue)
                    } else {
                        stringResource(R.string.action_finish_setup)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Built-in Accessible Numeric Keypad (PRD 04 Audit #4)
            PinNumericKeypad(
                onDigitPressed = onDigitPressed,
                onBackspacePressed = onBackspacePressed
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                    text = stringResource(R.string.pin_on_device_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun PinSlotIndicator(
    digit: Char?,
    isVisible: Boolean
) {
    val isFilled = digit != null
    Box(
        modifier = Modifier
            .size(width = 24.dp, height = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isFilled && isVisible -> {
                Text(
                    text = digit.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            isFilled -> {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 2.5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}

@Composable
private fun PinNumericKeypad(
    onDigitPressed: (Char) -> Unit,
    onBackspacePressed: () -> Unit
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowDigits.forEach { digit ->
                    KeypadDigitButton(
                        digit = digit,
                        onClick = { onDigitPressed(digit) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(64.dp))
            KeypadDigitButton(
                digit = '0',
                onClick = { onDigitPressed('0') }
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onBackspacePressed
                    )
                    .testTag("keypad_backspace")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Delete last digit",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadDigitButton(
    digit: Char,
    onClick: () -> Unit
) {
    val extendedColors = KaiTheme.extendedColors
    Surface(
        shape = CircleShape,
        color = extendedColors.cardSurface,
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .testTag("keypad_digit_$digit")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digit.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
