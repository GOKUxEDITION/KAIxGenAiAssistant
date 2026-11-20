package com.kaixgen.assistant.ui.screens.buddy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kaixgen.assistant.R
import com.kaixgen.assistant.domain.model.CommandFeedback
import com.kaixgen.assistant.domain.model.VoiceOrbState
import com.kaixgen.assistant.domain.model.VoiceSuggestion
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiTwoToneHeadline
import com.kaixgen.assistant.ui.components.KaiVoiceOrb
import com.kaixgen.assistant.ui.theme.KaiGreenSuccess
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.KaiBuddyUiState

/**
 * KAI Assistant ("Buddy") Tab matching 03_buddy_tab.jpg with all PRD 04 audit fixes:
 * - Fixed typo: "Get things done on your phone, hands-free"
 * - Animated fluid multi-layered Canvas orb with real Android SpeechRecognizer integration
 * - Rotating localized "Try saying..." pill + interactive quick-test chips
 * - Live "What I heard" transcript + intent confirmation card with thumbs feedback
 */
@Composable
fun KaiBuddyScreen(
    uiState: KaiBuddyUiState,
    languageCode: String,
    onStartListening: () -> Unit,
    onAmplitudeChanged: (Float) -> Unit,
    onPartialTranscript: (String) -> Unit,
    onFinalTranscript: (String) -> Unit,
    onVoiceError: (String) -> Unit,
    onCancelListening: () -> Unit,
    onSuggestionTapped: (VoiceSuggestion) -> Unit,
    onReplayHistoryCommand: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onFeedbackVote: (Boolean) -> Unit,
    onDismissFeedback: () -> Unit,
    onNavigateToCommandHelp: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = KaiTheme.extendedColors
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    fun startRealVoiceCapture() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onVoiceError("Speech recognizer unavailable on this device. Tap any suggestion chip below to test KAI.")
            return
        }
        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = onStartListening()
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = onAmplitudeChanged(rmsdB)
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Kuch sunai nahi diya, phir se bolo."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap the orb and speak."
                        SpeechRecognizer.ERROR_AUDIO -> "Microphone busy. Close other recording apps."
                        else -> "Voice capture interrupted. Tap a suggestion below or retry."
                    }
                    onVoiceError(msg)
                }
                override fun onResults(results: Bundle?) {
                    val best = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    onFinalTranscript(best)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onPartialTranscript(partial)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            onStartListening()
            recognizer.startListening(intent)
        } catch (_: Exception) {
            onVoiceError("Unable to start microphone. Tap a sample command below to test.")
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRealVoiceCapture()
        } else {
            onVoiceError("Microphone permission is needed for hands-free voice commands.")
        }
    }

    fun handleOrbClick() {
        if (uiState.orbState == VoiceOrbState.LISTENING) {
            speechRecognizer?.stopListening()
            onCancelListening()
            return
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            startRealVoiceCapture()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KaiBrandTopBar(
            isListeningActive = uiState.isWakeWordActive,
            onSettingsClick = onNavigateToSettings
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Headline matching 03_buddy_tab.jpg ("Just say it")
            KaiTwoToneHeadline(
                prefix = stringResource(R.string.buddy_heading_prefix),
                highlighted = stringResource(R.string.buddy_heading_highlight)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fixed typo subtitle ("Get things done on your phone, hands-free")
            Text(
                text = stringResource(R.string.buddy_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Centerpiece Animated Fluid KAI Orb
            KaiVoiceOrb(
                orbState = uiState.orbState,
                amplitude = uiState.soundAmplitude,
                onClick = ::handleOrbClick,
                size = 264.dp
            )

            // Live Transcript or Status Notice
            AnimatedVisibility(visible = uiState.liveTranscript.isNotBlank() && uiState.orbState == VoiceOrbState.LISTENING) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = extendedColors.micContainer,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = "\u201C${uiState.liveTranscript}\u201D",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            AnimatedVisibility(visible = uiState.statusMessage != null) {
                Text(
                    text = uiState.statusMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (uiState.orbState == VoiceOrbState.ERROR) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Command Result / Safety Confirmation Sheet Card (PRD 04 S2-03 / S2-04)
            AnimatedVisibility(visible = uiState.lastFeedback != null) {
                uiState.lastFeedback?.let { feedback ->
                    CommandResultCard(
                        feedback = feedback,
                        onConfirm = onConfirmAction,
                        onCancel = onCancelAction,
                        onVote = onFeedbackVote,
                        onDismiss = onDismissFeedback,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Try saying..." Section matching 03_buddy_tab.jpg with Guide shortcut
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.try_saying_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToCommandHelp,
                    modifier = Modifier.testTag("buddy_command_guide_button")
                ) {
                    Text(
                        text = "What can I say?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = uiState.featuredSuggestion,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "featured_suggestion_pill"
            ) { featured ->
                Surface(
                    onClick = { onSuggestionTapped(featured) },
                    shape = RoundedCornerShape(50),
                    color = extendedColors.cardSurface,
                    border = BorderStroke(1.dp, extendedColors.cardBorder),
                    shadowElevation = 4.dp,
                    modifier = Modifier.testTag("featured_suggestion_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (featured.isWhatsApp) KaiGreenSuccess else MaterialTheme.colorScheme.primary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (featured.isWhatsApp) Icons.Rounded.Chat else Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = featured.phrase,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick-tap Localized Hinglish / English Suggestion Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.suggestions, key = { it.id }) { suggestion ->
                    SuggestionChip(
                        onClick = { onSuggestionTapped(suggestion) },
                        label = {
                            Text(
                                text = suggestion.phrase.replace("\u201C", "").replace("\u201D", ""),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("suggestion_chip_${suggestion.id}")
                    )
                }
            }

            // Room-Persisted Local Command History (PRD 02 FR-AS-13 & 03 F7)
            AnimatedVisibility(visible = uiState.commandHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Recent Commands (On-device)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        TextButton(
                            onClick = onClearHistory,
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Text("Clear All", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    uiState.commandHistory.take(5).forEach { item ->
                        KaiElevatedCard(
                            cornerRadius = 18.dp,
                            onClick = { onReplayHistoryCommand(item.transcript) },
                            modifier = Modifier.testTag("history_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = extendedColors.micContainer
                                        ) {
                                            Text(
                                                text = item.intentName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "${item.latencyMs} ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\u201C${item.transcript}\u201D",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.responseText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onReplayHistoryCommand(item.transcript) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Replay,
                                        contentDescription = "Replay command",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteHistoryItem(item.id) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete history entry",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            KaiBrandSignatureBadge()
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun CommandResultCard(
    feedback: CommandFeedback,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onVote: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors
    KaiElevatedCard(
        cornerRadius = 20.dp,
        modifier = modifier.testTag("command_result_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = extendedColors.micContainer
                    ) {
                        Text(
                            text = feedback.intentName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "${feedback.confidencePercent}% \u2022 ${feedback.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss result",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Heard: \u201C${feedback.heardText}\u201D",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = feedback.responseText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (feedback.requiresConfirmation) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_cancel_button")
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_confirm_button")
                    ) {
                        Text("Confirm")
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Was this right?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onVote(true) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (feedback.thumbsUp == true) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Helpful",
                            tint = if (feedback.thumbsUp == true) extendedColors.success else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onVote(false) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (feedback.thumbsUp == false) Icons.Rounded.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Not helpful",
                            tint = if (feedback.thumbsUp == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
