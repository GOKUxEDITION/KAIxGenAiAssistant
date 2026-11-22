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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.kaixgen.assistant.security.HyperSecurityShieldEngine
import com.kaixgen.assistant.service.KaiAccessibilityActionService
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperSecurityCenterScreen(
    onOpenVoiceprint: () -> Unit,
    onOpenIntruderVault: () -> Unit,
    onOpenSafetySos: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kaiApp = context.applicationContext as? KaiApplication
    val ext = KaiTheme.extendedColors

    val shieldEngine = remember(context) { HyperSecurityShieldEngine(context) }
    val antiTheft = remember(context) {
        kaiApp?.findPhoneAlarmEngine?.let { AntiTheftGuardController(context.applicationContext, it) }
    }

    var report by remember { mutableStateOf(shieldEngine.runDeviceSecurityAudit()) }
    var decoyPinInput by remember { mutableStateOf("") }
    var panicPhraseInput by remember { mutableStateOf(shieldEngine.getPanicPhrase()) }
    var chargerArmed by remember { mutableStateOf(false) }
    var motionArmed by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("Hyper Security Shield", fontWeight = FontWeight.Bold) },
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
            // 1. Live Hyper Security Posture Ring
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = true) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.GppGood, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(" Hyper Shield Score: ${report.score}/100", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = { report = shieldEngine.runDeviceSecurityAudit() }) {
                                Text("Scan")
                            }
                        }
                        LinearProgressIndicator(
                            progress = { report.score / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        val rows = listOf(
                            "Android OS Screen Lock Active" to report.isOsLockSecure,
                            "USB Debugging (ADB) Disabled" to report.isUsbDebuggingOff,
                            "No Root / Tamper Signals" to report.isNotRooted,
                            "Real Screen-Off Accessibility Lock" to report.isAccessibilityLockReady,
                            "Decoy PIN Safe-Mode Vault" to report.hasDecoyPinConfigured,
                            "Panic Lockdown Phrase Armed" to report.hasPanicPhraseConfigured
                        )
                        rows.forEach { (label, ok) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = if (ok) ext.success else ext.warning
                                )
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // 2. Instant Panic Lockdown & Anti-Theft Guard
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Panic Lockdown & Anti-Theft Guard", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = panicPhraseInput,
                            onValueChange = { panicPhraseInput = it },
                            label = { Text("Secret Panic Phrase (e.g., emergency lock)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (shieldEngine.setPanicPhrase(panicPhraseInput)) {
                                        report = shieldEngine.runDeviceSecurityAudit()
                                        statusBanner = "Panic Phrase saved: \"$panicPhraseInput\""
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Panic Phrase")
                            }
                            Button(
                                onClick = {
                                    kaiApp?.appContainer?.ttsManager?.speak("Emergency lockdown active!", languageCode = "hi-IN")
                                    val locked = KaiAccessibilityActionService.triggerLockScreen()
                                    if (!locked) {
                                        runCatching { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Lock, contentDescription = null)
                                Text(" Panic Lock")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Charger-Unplug Siren Guard", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = chargerArmed,
                                onCheckedChange = {
                                    chargerArmed = it
                                    antiTheft?.setChargerGuardArmed(it)
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Don't Touch / Pocket-Snatch Siren", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = motionArmed,
                                onCheckedChange = {
                                    motionArmed = it
                                    antiTheft?.setMotionGuardArmed(it)
                                }
                            )
                        }
                    }
                }
            }

            // 3. Decoy PIN Setup (F-SE-08)
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Decoy PIN (Fake Safe-Mode Vault)", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Agar koi zabardasti PIN maange, to Decoy PIN daalne par harmless empty screen khulegi aur aapka asli data safe rahega.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = decoyPinInput,
                            onValueChange = { decoyPinInput = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text("4–6 Digit Decoy PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (shieldEngine.setDecoyPin(decoyPinInput)) {
                                        decoyPinInput = ""
                                        report = shieldEngine.runDeviceSecurityAudit()
                                        statusBanner = "Decoy PIN securely hashed & armed."
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Decoy PIN")
                            }
                            OutlinedButton(
                                onClick = {
                                    shieldEngine.clearDecoyPin()
                                    report = shieldEngine.runDeviceSecurityAudit()
                                    statusBanner = "Decoy PIN removed."
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear")
                            }
                        }
                        if (statusBanner.isNotBlank()) {
                            Text(statusBanner, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 4. Quick Security Shortcuts
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onOpenVoiceprint, modifier = Modifier.weight(1f)) {
                        Text("Voiceprint")
                    }
                    OutlinedButton(onClick = onOpenIntruderVault, modifier = Modifier.weight(1f)) {
                        Text("Intruders")
                    }
                    OutlinedButton(onClick = onOpenSafetySos, modifier = Modifier.weight(1f)) {
                        Text("SOS Hub")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
