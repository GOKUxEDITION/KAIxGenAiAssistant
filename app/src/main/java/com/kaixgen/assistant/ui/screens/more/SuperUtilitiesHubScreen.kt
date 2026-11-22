package com.kaixgen.assistant.ui.screens.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.domain.hardware.BatteryVoiceAlertMonitor
import com.kaixgen.assistant.domain.routine.DailyHabitReminderHelper
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperUtilitiesHubScreen(
    onOpenMoneyTools: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kaiApp = context.applicationContext as? KaiApplication
    val ext = KaiTheme.extendedColors

    var phoneDigits by remember { mutableStateOf("") }
    var waMessage by remember { mutableStateOf("Hello!") }
    var batteryVoiceOn by remember { mutableStateOf(false) }
    val batteryMonitor = remember {
        kaiApp?.appContainer?.ttsManager?.let { BatteryVoiceAlertMonitor(context.applicationContext, it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("KAI Super Utilities Hub", fontWeight = FontWeight.Bold) },
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Direct WhatsApp (Bina Number Save Kiye)", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { phoneDigits = it.filter { ch -> ch.isDigit() }.take(12) },
                            label = { Text("10-digit Mobile Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = waMessage,
                            onValueChange = { waMessage = it },
                            label = { Text("Message") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val normalized = if (phoneDigits.length == 10) "91$phoneDigits" else phoneDigits
                                val uri = Uri.Builder()
                                    .scheme("https")
                                    .authority("wa.me")
                                    .appendPath(normalized)
                                    .appendQueryParameter("text", waMessage)
                                    .build()
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            },
                            enabled = phoneDigits.length >= 10
                        ) {
                            Text("Open Direct WhatsApp Chat")
                        }
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smart Money: EMI, GST & Bill Split", fontWeight = FontWeight.Bold)
                            Text("Loan EMI, 1-tap GST & friends bill split", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = onOpenMoneyTools) {
                            Text("Open")
                        }
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Battery Full / Low Hindi Voice Alert", fontWeight = FontWeight.Bold)
                            Text("100% full aur 15% low par Hindi me alert bolega", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = batteryVoiceOn,
                            onCheckedChange = { enabled ->
                                batteryVoiceOn = enabled
                                if (enabled) batteryMonitor?.start() else batteryMonitor?.stop()
                            }
                        )
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1-Tap Daily Health Alarms (No Medical Advice)", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { DailyHabitReminderHelper.scheduleDailyAlarm(context, "Drink Water (KAI)", 10, 0) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Water 10 AM")
                            }
                            OutlinedButton(
                                onClick = { DailyHabitReminderHelper.scheduleDailyAlarm(context, "Morning Medicine (KAI)", 8, 30) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Medicine 8:30")
                            }
                        }
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("India Emergency Helplines (1-Tap Dialer)", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { DailyHabitReminderHelper.openEmergencyDialer(context, "112") }, modifier = Modifier.weight(1f)) {
                                Text("112 SOS")
                            }
                            OutlinedButton(onClick = { DailyHabitReminderHelper.openEmergencyDialer(context, "108") }, modifier = Modifier.weight(1f)) {
                                Text("108 Amb.")
                            }
                            OutlinedButton(onClick = { DailyHabitReminderHelper.openEmergencyDialer(context, "1930") }, modifier = Modifier.weight(1f)) {
                                Text("1930 Cyber")
                            }
                        }
                    }
                }
            }
        }
    }
}
