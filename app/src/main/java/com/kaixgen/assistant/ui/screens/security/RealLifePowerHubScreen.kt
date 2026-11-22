package com.kaixgen.assistant.ui.screens.security

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.domain.hardware.AntiTheftGuardController
import com.kaixgen.assistant.domain.voice.CustomVoiceLockUnlockManager
import com.kaixgen.assistant.domain.voice.VoiceUtilityCalculator
import com.kaixgen.assistant.service.KaiAccessibilityActionService
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLifePowerHubScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kaiApp = context.applicationContext as? KaiApplication
    val ext = KaiTheme.extendedColors

    val lockManager = remember { CustomVoiceLockUnlockManager(context) }
    val calculator = remember { VoiceUtilityCalculator() }
    val antiTheft = remember {
        kaiApp?.findPhoneAlarmEngine?.let { AntiTheftGuardController(context.applicationContext, it) }
    }

    val unlockWords = remember { mutableStateListOf<String>().apply { addAll(lockManager.getUnlockPhrases()) } }
    val lockWords = remember { mutableStateListOf<String>().apply { addAll(lockManager.getLockPhrases()) } }

    var newUnlockInput by remember { mutableStateOf("") }
    var newLockInput by remember { mutableStateOf("") }
    var chargerGuardOn by remember { mutableStateOf(false) }
    var motionGuardOn by remember { mutableStateOf(false) }
    var calcQuery by remember { mutableStateOf("") }
    var calcAnswer by remember { mutableStateOf("Try: '2500 ka 18 percent' ya '5 km in meters'") }
    var lockStatusMsg by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("Voice Lock Words & Anti-Theft", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Custom Unlock Words
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = true) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Custom Unlock Words", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Koi bhi custom word add karein — phone lock hone par ye word bolte hi KAI unlock trigger karega.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newUnlockInput,
                                onValueChange = { newUnlockInput = it },
                                placeholder = { Text("e.g., Khul ja sim sim") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (lockManager.addUnlockPhrase(newUnlockInput)) {
                                        unlockWords.clear()
                                        unlockWords.addAll(lockManager.getUnlockPhrases())
                                        newUnlockInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add Unlock Word")
                            }
                        }
                        unlockWords.forEach { word ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• \"$word\"", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = {
                                    lockManager.removeUnlockPhrase(word)
                                    unlockWords.clear()
                                    unlockWords.addAll(lockManager.getUnlockPhrases())
                                }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }

            // 2. Custom Lock Words + Instant Real Screen Lock
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Custom Lock Words (Real Screen Off)", fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newLockInput,
                                onValueChange = { newLockInput = it },
                                placeholder = { Text("e.g., So ja phone") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (lockManager.addLockPhrase(newLockInput)) {
                                        lockWords.clear()
                                        lockWords.addAll(lockManager.getLockPhrases())
                                        newLockInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add Lock Word")
                            }
                        }
                        lockWords.forEach { word ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• \"$word\"", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = {
                                    lockManager.removeLockPhrase(word)
                                    lockWords.clear()
                                    lockWords.addAll(lockManager.getLockPhrases())
                                }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Remove")
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = {
                                val locked = KaiAccessibilityActionService.triggerLockScreen()
                                lockStatusMsg = if (locked) "Screen locked!" else "Real screen lock ke liye Accessibility Service enable karein."
                            }) {
                                Text("Test Lock Screen")
                            }
                            OutlinedButton(onClick = {
                                runCatching { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                            }) {
                                Text("Enable Service")
                            }
                        }
                        if (lockStatusMsg.isNotBlank()) {
                            Text(lockStatusMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 3. Anti-Theft Charger & Pocket Snatch Guard
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Anti-Theft Charger & Pocket Guard", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Charger-Unplug Siren", fontWeight = FontWeight.SemiBold)
                                Text("Koi charger nikale to turant loud alarm + strobe", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = chargerGuardOn,
                                onCheckedChange = { enabled ->
                                    chargerGuardOn = enabled
                                    antiTheft?.setChargerGuardArmed(enabled)
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pocket / Snatch Motion Guard", fontWeight = FontWeight.SemiBold)
                                Text("Sudden phone snatch detect hone par siren ON", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = motionGuardOn,
                                onCheckedChange = { enabled ->
                                    motionGuardOn = enabled
                                    antiTheft?.setMotionGuardArmed(enabled)
                                }
                            )
                        }
                    }
                }
            }

            // 4. Offline Hinglish Quick Calculator & Unit Converter
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Offline Hinglish Calculator & Units", fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = calcQuery,
                            onValueChange = { calcQuery = it },
                            placeholder = { Text("2500 ka 18 percent / 5 km in meters") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = {
                            val res = calculator.evaluate(calcQuery)
                            calcAnswer = res ?: "Samajh nahi aaya — example: '2500 ka 18 percent' ya '450 plus 1280'"
                            kaiApp?.appContainer?.ttsManager?.speak(calcAnswer, languageCode = "hi-IN")
                        }) {
                            Text("Calculate & Speak")
                        }
                        Text(calcAnswer, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
