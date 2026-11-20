package com.kaixgen.assistant.ui.screens.voicelock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kaixgen.assistant.R
import com.kaixgen.assistant.domain.model.SupportedLanguage
import com.kaixgen.assistant.ui.components.KaiCircularIconBadge
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiSubScreenTopBar
import com.kaixgen.assistant.ui.components.KaiTwoToneHeadline
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.SetVoicePhraseUiState

/**
 * Set Voice Phrase Screen matching 01_set_voice_phrase.jpg with:
 * - Crisp custom text-free illustration + accessible >=12sp tagline pill
 * - Regional Indian language chips (Hinglish, Hindi, English, Telugu, Tamil, etc.)
 * - Real Android SpeechRecognizer integration + "What I heard" confirmation bottom sheet (FR-VL-02)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetVoicePhraseScreen(
    uiState: SetVoicePhraseUiState,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSelectLanguage: (SupportedLanguage) -> Unit,
    onOpenRecordSheet: () -> Unit,
    onCloseRecordSheet: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSpeechRecognized: (String) -> Unit,
    onSpeechError: (String) -> Unit,
    onDraftPhraseTyped: (String) -> Unit,
    onConfirmSavePhrase: () -> Unit,
    onNavigateToBackupPasscode: () -> Unit,
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

    fun startSpeechCapture() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSpeechError("On-device speech service unavailable on this emulator. You can type & confirm your phrase below.")
            return
        }
        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = onStartListening()
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = onStopListening()
                override fun onError(error: Int) {
                    onSpeechError("Could not capture speech clearly. Tap mic to retry or edit text below.")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val best = matches?.firstOrNull()
                    if (!best.isNullOrBlank()) {
                        onSpeechRecognized(best)
                    } else {
                        onSpeechError("Didn't catch a clear phrase. Try again.")
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onDraftPhraseTyped(partial)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, uiState.selectedLanguage.code)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            onStartListening()
            recognizer.startListening(intent)
        } catch (_: Exception) {
            onSpeechError("Microphone capture could not start. You can type your phrase below.")
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechCapture()
        } else {
            onSpeechError("Microphone permission denied. Grant permission or type your phrase to preview.")
        }
    }

    fun triggerVoiceRecording() {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            startSpeechCapture()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
                .padding(horizontal = 20.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KaiTwoToneHeadline(
                prefix = stringResource(R.string.set_phrase_heading_prefix),
                highlighted = stringResource(R.string.set_phrase_heading_highlight),
                suffix = stringResource(R.string.set_phrase_heading_suffix)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.set_phrase_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Clean Hero Illustration Card matching 01_set_voice_phrase.jpg
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, extendedColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_voice_unlock_hero_1795173001374),
                        contentDescription = stringResource(R.string.cd_voice_unlock_hero),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = extendedColors.micContainer.copy(alpha = 0.94f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.set_phrase_tagline),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Supported Lock-Phrase Language Chips (README Baseline)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(SupportedLanguage.entries) { language ->
                    val selected = uiState.selectedLanguage == language
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectLanguage(language) },
                        label = {
                            Text(
                                text = language.nativeName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.testTag("phrase_lang_${language.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = uiState.savedConfirmationBanner) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = extendedColors.lockContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = extendedColors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Saved phrase: \"${uiState.savedPhrase}\" (${uiState.selectedLanguage.displayName})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Card 1: Set Your Voice Phrase (Matches 01_set_voice_phrase.jpg)
            KaiElevatedCard(
                onClick = onOpenRecordSheet,
                modifier = Modifier.testTag("action_card_set_voice_phrase")
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
                            text = stringResource(R.string.card_set_phrase_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (uiState.savedPhrase.isNotBlank()) {
                                "Current: \u201C${uiState.savedPhrase}\u201D \u2022 Tap to re-record"
                            } else {
                                stringResource(R.string.card_set_phrase_subtitle)
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

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: Create Backup Passcode (Matches 01_set_voice_phrase.jpg)
            KaiElevatedCard(
                onClick = onNavigateToBackupPasscode,
                modifier = Modifier.testTag("action_card_create_backup_pin")
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
                            text = stringResource(R.string.card_create_pin_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (uiState.hasBackupPasscode) {
                                "Backup PIN configured \u2022 Tap to update"
                            } else {
                                stringResource(R.string.card_create_pin_subtitle)
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

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    if (uiState.showRecordSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                speechRecognizer?.stopListening()
                onCloseRecordSheet()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            VoicePhraseRecordSheetContent(
                uiState = uiState,
                onMicTap = {
                    if (uiState.isRecording) {
                        speechRecognizer?.stopListening()
                        onStopListening()
                    } else {
                        triggerVoiceRecording()
                    }
                },
                onDraftChanged = onDraftPhraseTyped,
                onSavePhrase = onConfirmSavePhrase,
                onCancel = {
                    speechRecognizer?.stopListening()
                    onCloseRecordSheet()
                }
            )
        }
    }
}

@Composable
private fun VoicePhraseRecordSheetContent(
    uiState: SetVoicePhraseUiState,
    onMicTap: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSavePhrase: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Record Your Voice Phrase",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Say 2\u20133 natural words in ${uiState.selectedLanguage.displayName}. Voiceprint stays strictly on-device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Large Accessible Record Button
        Surface(
            onClick = onMicTap,
            shape = CircleShape,
            color = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(84.dp)
                .testTag("record_phrase_mic_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (uiState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = if (uiState.isRecording) "Stop recording" else "Start recording",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Text(
            text = if (uiState.isRecording) "Listening\u2026 Speak clearly now" else "Tap microphone to speak",
            style = MaterialTheme.typography.labelLarge,
            color = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )

        // "What I heard" Preview & Edit Field (PRD FR-VL-02)
        OutlinedTextField(
            value = uiState.draftPhrase,
            onValueChange = onDraftChanged,
            label = { Text(stringResource(R.string.what_i_heard_label)) },
            placeholder = { Text("e.g. Hare Krishna / Open Sesame") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("what_i_heard_input")
        )

        if (uiState.qualityScore > 0f) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Voiceprint Clarity (${uiState.enrollmentStep}/3 samples)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(uiState.qualityScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = KaiTheme.extendedColors.success
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { uiState.qualityScore },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = KaiTheme.extendedColors.success,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSavePhrase,
                enabled = uiState.draftPhrase.trim().length >= 3,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp)
                    .testTag("confirm_save_phrase_button")
            ) {
                Text("Save Phrase", fontWeight = FontWeight.Bold)
            }
        }
    }
}
