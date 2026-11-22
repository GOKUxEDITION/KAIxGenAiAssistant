package com.kaixgen.assistant.ui.screens.security

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SecurityCheckItem(
    val title: String,
    val detail: String,
    val isPassed: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityHealthAndIntruderScreen(
    hasBackupPin: Boolean,
    isVoiceLockEnabled: Boolean,
    isVoiceprintEnrolled: Boolean,
    lockedAppCount: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val intruderDir = remember { File(context.filesDir, "intruders") }
    val intruderFiles = remember {
        mutableStateListOf<File>().apply {
            intruderDir.listFiles()?.sortedByDescending { it.lastModified() }?.let { addAll(it) }
        }
    }

    val checks = listOf(
        SecurityCheckItem("Backup PIN (PBKDF2 + Keystore)", "Required fallback for voice unlock", hasBackupPin),
        SecurityCheckItem("Voice Lock Protection", "Master voice guard active", isVoiceLockEnabled),
        SecurityCheckItem("Speaker Voiceprint Biometrics", "Dual-factor vocal embedding enrolled", isVoiceprintEnrolled),
        SecurityCheckItem("Per-App Voice Vault", "$lockedAppCount apps protected", lockedAppCount > 0)
    )
    val score = (checks.count { it.isPassed } * 100) / checks.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Health & Intruders") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                            Text(
                                text = "Security Health Score: $score/100",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { score / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            items(checks) { check ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (check.isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (check.isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(check.title, fontWeight = FontWeight.SemiBold)
                            Text(check.detail, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Intruder Selfie Vault (${intruderFiles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (intruderFiles.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraFront, contentDescription = null)
                            Text(
                                text = "Koi intruder attempt record nahi hua hai. 3 galat PIN/Voice attempts par front-camera selfie privately save hogi.",
                                modifier = Modifier.padding(start = 12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(intruderFiles, key = { it.absolutePath }) { file ->
                    val stamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(file.lastModified()))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(file.name, fontWeight = FontWeight.SemiBold)
                                Text("Captured: $stamp", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                if (file.delete()) intruderFiles.remove(file)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Intruder Photo")
                            }
                        }
                    }
                }
            }
        }
    }
}
