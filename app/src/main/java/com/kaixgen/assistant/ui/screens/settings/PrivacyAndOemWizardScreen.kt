package com.kaixgen.assistant.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAndOemWizardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val brand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    var cloudAiOptIn by remember { mutableStateOf(false) }

    val oemTip = when {
        brand.contains("Vivo", ignoreCase = true) || brand.contains("iQOO", ignoreCase = true) ->
            "Vivo FuntouchOS: Settings -> Battery -> Background Power Consumption -> KAIxGenAssistant ko 'High Background Power Usage' allow karein."
        brand.contains("Xiaomi", ignoreCase = true) || brand.contains("Redmi", ignoreCase = true) ->
            "MIUI/HyperOS: App Info -> Autostart ON karein aur Battery Saver ko 'No Restrictions' par set karein."
        brand.contains("Samsung", ignoreCase = true) ->
            "One UI: Battery -> Background usage limits -> Never sleeping apps me KAIxGenAssistant add karein."
        else ->
            "App Info -> Battery -> Unrestricted select karein taaki Wake Word & App Vault background me kill na ho."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Center & Battery Wizard") },
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
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
                            Text(" OEM Battery Wizard ($brand)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(oemTip, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            runCatching { context.startActivity(intent) }
                        }) {
                            Text("Open App Battery Settings")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null)
                            Text(" Privacy Center (F-OP-05)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Raw microphone audio kabhi disk par save nahi hota.")
                        Text("• Backup PIN 120k PBKDF2 + Android Keystore pepper se protected hai.")
                        Text("• Intruder selfies sirf app-private storage me rehti hain.")
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Opt-In Cloud AI (Gemini Fallback)", fontWeight = FontWeight.SemiBold)
                            Switch(checked = cloudAiOptIn, onCheckedChange = { cloudAiOptIn = it })
                        }
                    }
                }
            }
        }
    }
}
