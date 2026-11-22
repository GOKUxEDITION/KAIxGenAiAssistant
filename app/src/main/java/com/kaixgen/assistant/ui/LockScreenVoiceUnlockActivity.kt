package com.kaixgen.assistant.ui

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.domain.voice.CustomLockCommand
import com.kaixgen.assistant.domain.voice.CustomVoiceLockUnlockManager
import com.kaixgen.assistant.security.ScreenSecurityHelper
import com.kaixgen.assistant.service.KaiAccessibilityActionService
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KAIxGenTheme

/**
 * Lock-screen-aware Activity that wakes over the locked screen (showWhenLocked + turnScreenOn),
 * verifies user-defined Custom Unlock Words (and Lock Words), and requests OS Keyguard dismissal.
 */
class LockScreenVoiceUnlockActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenSecurityHelper.enableScreenCaptureProtection(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val lockManager = CustomVoiceLockUnlockManager(this)
        val tts = (application as? KaiApplication)?.appContainer?.ttsManager

        setContent {
            KAIxGenTheme(darkTheme = true) {
                var spokenInput by remember { mutableStateOf("") }
                var statusText by remember {
                    mutableStateOf("Apna Custom Unlock Word bolein ya type karein.")
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = true) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "KAI Voice Lock Screen",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = spokenInput,
                                    onValueChange = { spokenInput = it },
                                    label = { Text("Speak or enter custom word") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            when (val cmd = lockManager.matchUtterance(spokenInput)) {
                                                is CustomLockCommand.UnlockNow -> {
                                                    statusText = "Matched '${cmd.matchedPhrase}' — Unlocking!"
                                                    tts?.speak("Phone unlock ho raha hai", languageCode = "hi-IN")
                                                    requestUnlockKeyguard()
                                                }
                                                is CustomLockCommand.LockNow -> {
                                                    statusText = "Matched '${cmd.matchedPhrase}' — Locking!"
                                                    tts?.speak("Phone lock ho raha hai", languageCode = "hi-IN")
                                                    KaiAccessibilityActionService.triggerLockScreen()
                                                }
                                                CustomLockCommand.None -> {
                                                    statusText = "Word match nahi hua. Dobara koshish karein."
                                                    tts?.speak("Voice phrase match nahi hua", languageCode = "hi-IN")
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Verify Word")
                                    }
                                    OutlinedButton(onClick = { finish() }) {
                                        Text("Close")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "FLAG_SECURE Active • Extra Voice Privacy Layer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    private fun requestUnlockKeyguard() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && km != null) {
            km.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    finish()
                }
            })
        } else {
            finish()
        }
    }
}
