package com.kaixgen.assistant.ui.screens.voicelock

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kaixgen.assistant.domain.model.SecurityMode
import com.kaixgen.assistant.security.BiometricAuthResult
import com.kaixgen.assistant.security.BiometricAvailability
import com.kaixgen.assistant.security.BiometricCancellationHandle
import com.kaixgen.assistant.security.BiometricHelper
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.theme.KaiBlueDeep
import com.kaixgen.assistant.ui.theme.KaiBluePrimary
import com.kaixgen.assistant.ui.theme.KaiCyanAccent
import com.kaixgen.assistant.ui.theme.KaiGreenSuccess
import com.kaixgen.assistant.ui.viewmodel.VoiceLockUiState
import kotlinx.coroutines.delay

/**
 * Full-Screen Hardened Voice Lock Overlay Screen (PRD S3-07 / S3-08 / S3-09).
 * - Applies FLAG_SECURE while active to block screenshots/recents thumbnails (PRD 06 §3.3)
 * - Supports live Voice Phrase unlock via Android SpeechRecognizer + simulated quick-verify
 * - Provides system-level Fingerprint / Face Recognition fallback via [BiometricHelper]
 * - Auto-surfaces Keystore-backed Backup PIN pad with exponential cooldown enforcement
 */
@Composable
fun VoiceLockOverlayScreen(
    uiState: VoiceLockUiState,
    biometricHelper: BiometricHelper,
    onTestVoiceUnlock: (String) -> Unit,
    onTestPinUnlock: (String) -> Unit,
    onBiometricUnlockSuccess: (String) -> Unit,
    onBiometricFeedback: (String) -> Unit,
    onExitOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config = uiState.config
    var showPinPad by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var voiceFails by remember { mutableIntStateOf(0) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var activeBiometricHandle by remember { mutableStateOf<BiometricCancellationHandle?>(null) }

    val biometricAvailability = remember(config.securityMode) {
        biometricHelper.checkAvailability(
            requireStrongBiometric = config.securityMode == SecurityMode.STRICT
        )
    }

    // Enforce FLAG_SECURE while Lock Screen Overlay is displayed (PRD 06 §3.3)
    DisposableEffect(context) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            activeBiometricHandle?.cancel()
            activeBiometricHandle = null
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    fun triggerBiometricFallback() {
        speechRecognizer?.stopListening()
        isListening = false
        val promptConfig = biometricHelper.configForSecurityMode(config.securityMode)
        activeBiometricHandle = biometricHelper.authenticateFromContext(
            context = context,
            config = promptConfig
        ) { result ->
            when (result) {
                is BiometricAuthResult.Success -> {
                    activeBiometricHandle = null
                    onBiometricUnlockSuccess(result.authType.displayLabel)
                }
                BiometricAuthResult.UseBackupPinRequested -> {
                    activeBiometricHandle = null
                    pinInput = ""
                    showPinPad = true
                    onBiometricFeedback("Switched to Backup PIN.")
                }
                BiometricAuthResult.Cancelled -> {
                    activeBiometricHandle = null
                }
                BiometricAuthResult.FailedAttempt -> {
                    onBiometricFeedback("Biometric not recognized. Try again or use Backup PIN.")
                }
                is BiometricAuthResult.Lockout -> {
                    activeBiometricHandle = null
                    pinInput = ""
                    showPinPad = true
                    onBiometricFeedback(result.message)
                }
                is BiometricAuthResult.NotAvailable -> {
                    activeBiometricHandle = null
                    onBiometricFeedback(result.message)
                    if (result.availability == BiometricAvailability.NONE_ENROLLED) {
                        pinInput = ""
                        showPinPad = true
                    }
                }
                is BiometricAuthResult.Error -> {
                    activeBiometricHandle = null
                    onBiometricFeedback(result.message)
                }
            }
        }
    }

    // Automatically return to Voice Lock Home briefly after verified unlock
    LaunchedEffect(uiState.testLockUnlocked) {
        if (uiState.testLockUnlocked) {
            delay(1_100)
            onExitOverlay()
        }
    }

    fun startVoiceUnlockListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onTestVoiceUnlock(config.voicePhrase)
            return
        }
        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) {
                    isListening = false
                    voiceFails++
                    if (voiceFails >= 3) showPinPad = true
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val spoken = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    onTestVoiceUnlock(spoken)
                }
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.voicePhraseLanguage.code)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            isListening = true
            recognizer.startListening(intent)
        } catch (_: Exception) {
            isListening = false
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceUnlockListening()
    }

    val pulse = rememberInfiniteTransition(label = "lock_pulse")
    val ringScale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = if (isListening) 1.24f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 600 else 1_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lock_ring_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF071327), KaiBlueDeep, Color(0xFF0A1931))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Security Badge & Preview Exit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = KaiCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "KAI Lock \u2022 ${config.securityMode.badgeText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            TextButton(
                onClick = onExitOverlay,
                modifier = Modifier.testTag("lock_overlay_exit_button")
            ) {
                Text("Exit Preview", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Icon(
                imageVector = if (uiState.testLockUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (uiState.testLockUnlocked) KaiGreenSuccess else KaiCyanAccent,
                modifier = Modifier.size(44.dp)
            )

            Text(
                text = when {
                    uiState.testLockUnlocked -> "Verified & Unlocked"
                    showPinPad -> "Enter Backup Passcode"
                    else -> "Say Your Unlock Phrase"
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (config.securityMode == SecurityMode.STRICT && !showPinPad && !uiState.testLockUnlocked) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Anti-Replay Challenge: Say \u201C${config.voicePhrase} Blue\u201D",
                        style = MaterialTheme.typography.labelMedium,
                        color = KaiCyanAccent,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            if (uiState.testLockMessage != null) {
                Text(
                    text = uiState.testLockMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (uiState.testLockUnlocked) KaiGreenSuccess else Color(0xFFFFB4AB),
                    textAlign = TextAlign.Center
                )
            }

            AnimatedContent(targetState = showPinPad, label = "lock_mode_switch") { pinMode ->
                if (!pinMode) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(172.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(144.dp)
                                    .scale(ringScale)
                                    .clip(CircleShape)
                                    .background(KaiCyanAccent.copy(alpha = 0.22f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(116.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(KaiBluePrimary, KaiCyanAccent))
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = {
                                            val hasMic = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasMic) {
                                                startVoiceUnlockListening()
                                            } else {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    )
                                    .testTag("lock_overlay_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Speak unlock phrase",
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onTestVoiceUnlock(config.voicePhrase) },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("lock_overlay_simulate_phrase")
                        ) {
                            Text(
                                text = "Simulate \u201C${config.voicePhrase.ifBlank { "Hare Krishna" }}\u201D",
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // PIN Dots Row
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            for (i in 0 until config.backupPinLength) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (i < pinInput.length) KaiCyanAccent else Color.White.copy(alpha = 0.25f)
                                        )
                                )
                            }
                        }

                        // Dark Glass Numeric Keypad
                        val rows = listOf(
                            listOf('1', '2', '3'),
                            listOf('4', '5', '6'),
                            listOf('7', '8', '9')
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { digit ->
                                        OverlayKeypadKey(
                                            label = digit.toString(),
                                            onClick = {
                                                if (pinInput.length < config.backupPinLength) {
                                                    val updated = pinInput + digit
                                                    pinInput = updated
                                                    if (updated.length == config.backupPinLength) {
                                                        onTestPinUnlock(updated)
                                                        pinInput = ""
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.size(62.dp))
                                OverlayKeypadKey(
                                    label = "0",
                                    onClick = {
                                        if (pinInput.length < config.backupPinLength) {
                                            val updated = pinInput + '0'
                                            pinInput = updated
                                            if (updated.length == config.backupPinLength) {
                                                onTestPinUnlock(updated)
                                                pinInput = ""
                                            }
                                        }
                                    }
                                )
                                Surface(
                                    onClick = { pinInput = pinInput.dropLast(1) },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.14f),
                                    modifier = Modifier.size(62.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Signature & Fallback Action Bar (Voice <-> Backup PIN + Biometrics)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KaiBrandSignatureBadge(lightOnDark = true)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        pinInput = ""
                        showPinPad = !showPinPad
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("lock_overlay_toggle_method")
                ) {
                    Icon(
                        imageVector = if (showPinPad) Icons.Rounded.Mic else Icons.Rounded.Dialpad,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showPinPad) "Voice Phrase" else "Backup PIN")
                }

                if (biometricAvailability != BiometricAvailability.NO_HARDWARE) {
                    Button(
                        onClick = { triggerBiometricFallback() },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KaiBluePrimary.copy(alpha = 0.92f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("lock_overlay_biometric_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = "Unlock with fingerprint or face",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Biometrics")
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayKeypadKey(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = Modifier.size(62.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}
