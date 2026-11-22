package com.kaixgen.assistant.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.BuildConfig
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutKaiScreen(
    onBack: () -> Unit
) {
    val ext = KaiTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("About KAIxGenAssistant", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = true) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "KAIxGenAssistant",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Har Smartphone Ka Smart Assistant — Offline-first Indian Hinglish/Hindi Voice Assistant, Dual-Factor Voice Biometric Lock, Per-App Voice Vault, Personal Safety SOS & Acoustic Find-My-Phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Developer & Brand Identity", fontWeight = FontWeight.Bold)
                    Text("Developer: Dipak Choudhary (GOKUxEDITION)", style = MaterialTheme.typography.bodyMedium)
                    Text("Organization: KAIxGenShopLabs / KAIxGenShopTM", style = MaterialTheme.typography.bodyMedium)
                    Text("Architecture: Kotlin, Jetpack Compose Material 3, Room v2, Keystore AES-256-GCM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("© 2026 KAIxGenShopLabs. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = ext.mutedText)
                }
            }
        }
    }
}
