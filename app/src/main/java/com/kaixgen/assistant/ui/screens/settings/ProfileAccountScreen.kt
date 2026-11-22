package com.kaixgen.assistant.ui.screens.settings

import android.content.Context
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

/**
 * PRD F-AB-01 & Screen 04/15:
 * Guest-first offline local profile (no forced login friction) with honest optional
 * cloud-account status (never simulates fake login without Firebase Auth configured).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAccountScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tts = (context.applicationContext as? KaiApplication)?.appContainer?.ttsManager
    val ext = KaiTheme.extendedColors
    val prefs = remember(context) {
        context.getSharedPreferences("kai_profile_prefs", Context.MODE_PRIVATE)
    }

    var displayName by remember { mutableStateOf(prefs.getString("display_name", "") ?: "") }
    var statusMessage by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("Profile & Account", fontWeight = FontWeight.Bold) },
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
                KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = true) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Offline Guest Profile (Default)", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "KAIxGenAssistant bina kisi forced login ke 100% offline chalta hai. Apna naam save karein taaki KAI aapko naam se greet kare.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Your Name (Optional)") },
                            placeholder = { Text("e.g., Dipak") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val clean = displayName.trim()
                                    prefs.edit().putString("display_name", clean).apply()
                                    statusMessage = if (clean.isNotBlank()) "Profile saved! Namaste $clean." else "Guest profile reset."
                                    if (clean.isNotBlank()) {
                                        tts?.speak("Namaste $clean! Aapka KAI profile save ho gaya hai.", languageCode = "hi-IN")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Profile")
                            }
                            OutlinedButton(
                                onClick = {
                                    displayName = ""
                                    prefs.edit().remove("display_name").apply()
                                    statusMessage = "Local profile cleared."
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear")
                            }
                        }
                        if (statusMessage.isNotBlank()) {
                            Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = ext.mutedText)
                            Text(" Optional Cloud Sign-In (Firebase Auth)", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Status: Local-Only Mode Active. Cloud backup aur cross-device sync ke liye Firebase Auth (`google-services.json`) connect karna optional hai — bina login ke sabhi core features 100% free aur unlocked hain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No fake login • Zero forced account wall • Privacy-first by design",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
