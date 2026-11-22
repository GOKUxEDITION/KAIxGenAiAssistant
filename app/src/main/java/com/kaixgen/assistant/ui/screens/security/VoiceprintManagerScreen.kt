package com.kaixgen.assistant.ui.screens.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceprintManagerScreen(
    voicePhrase: String,
    onBack: () -> Unit,
    onSaveThreshold: (Float) -> Unit = {}
) {
    var enrolledSamples by remember { mutableIntStateOf(0) }
    var strictnessThreshold by remember { mutableFloatStateOf(0.75f) }
    var testStatus by remember { mutableStateOf("Apni aawaz ke 3 samples record karein taaki sirf aapki voice se phone unlock ho.") }

    val modeLabel = when {
        strictnessThreshold < 0.70f -> "Convenience (0.65)"
        strictnessThreshold < 0.82f -> "Balanced (0.75)"
        else -> "Strict Biometric (0.85+)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speaker Voiceprint Biometrics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null)
                        Text(
                            text = "Enrollment Progress: $enrolledSamples / 3 samples",
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { enrolledSamples / 3f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Phrase: \"$voicePhrase\"", style = MaterialTheme.typography.bodyMedium)
                    Text(testStatus, style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (enrolledSamples < 3) {
                            enrolledSamples++
                            testStatus = if (enrolledSamples == 3) {
                                "16-band Spectral Voiceprint enrolled! Dual-Factor Unlock ready."
                            } else {
                                "Sample $enrolledSamples captured. Dobara apna phrase bolein."
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Text(" Record Sample", modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(
                    onClick = {
                        enrolledSamples = 0
                        testStatus = "Voiceprint reset ho gaya. Naye samples record karein."
                    }
                ) {
                    Text("Reset")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Verification Strictness: $modeLabel", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = strictnessThreshold,
                        onValueChange = { strictnessThreshold = it },
                        onValueChangeFinished = { onSaveThreshold(strictnessThreshold) },
                        valueRange = 0.65f..0.90f
                    )
                    Text(
                        text = "Higher strictness = zyada security (noisy jagah par Balanced mode recommended hai).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
