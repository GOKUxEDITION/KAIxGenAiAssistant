package com.kaixgen.assistant.ui.screens.safety

import android.content.Intent
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyHubScreen(
    onTriggerSiren: () -> Unit,
    onStopSiren: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var trustedContact by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var fakeCallerName by remember { mutableStateOf("Papa") }
    var showFakeCallDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Safety & SOS Hub") },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Emergency SOS Trigger (F-FP-07)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Bolein 'Help Help Help' ya neeche button dabayein — loud siren + SOS alert share hoga.")
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    onTriggerSiren()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "EMERGENCY SOS from KAIxGenAssistant! Please call me immediately. Trusted contact: $trustedContact"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share SOS Alert"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Text(" Trigger SOS", modifier = Modifier.padding(start = 6.dp))
                            }
                            OutlinedButton(onClick = onStopSiren) {
                                Text("Stop Siren")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fake Incoming Call (F-FP-09)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = fakeCallerName,
                            onValueChange = { fakeCallerName = it },
                            label = { Text("Fake Caller Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showFakeCallDialog = true }) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Text(" Start Fake Call", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Icon(Icons.Default.LocalHospital, contentDescription = null)
                            Text(" Emergency Medical Card (F-FP-11)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = trustedContact,
                            onValueChange = { trustedContact = it },
                            label = { Text("Trusted Family Contact Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showFakeCallDialog) {
        AlertDialog(
            onDismissRequest = { showFakeCallDialog = false },
            title = { Text("Incoming Call...") },
            text = { Text("$fakeCallerName is calling you\nMobile • India") },
            confirmButton = {
                TextButton(onClick = { showFakeCallDialog = false }) { Text("Answer") }
            },
            dismissButton = {
                TextButton(onClick = { showFakeCallDialog = false }) { Text("Decline") }
            }
        )
    }
}
