package com.kaixgen.assistant.ui.screens.safety

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.domain.routine.DailyHabitReminderHelper
import com.kaixgen.assistant.ui.theme.KaiTheme

/**
 * Phase 9 Dignified Senior Citizen Mode:
 * Extra-large text, high-contrast oversized buttons (76dp+), zero clutter, voice-first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorModeScreen(
    onOpenVoiceBuddy: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kaiApp = context.applicationContext as? KaiApplication
    val ext = KaiTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("Easy Senior Mode", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = onOpenVoiceBuddy,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(84.dp)
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null)
                    Text("  Bol Kar Kaam Karein (KAI)", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Button(
                    onClick = { DailyHabitReminderHelper.openEmergencyDialer(context, "112") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(84.dp)
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null)
                    Text("  Emergency Helpline (112)", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Button(
                    onClick = { DailyHabitReminderHelper.scheduleDailyAlarm(context, "Dawai Ka Samay (KAI)", 8, 30) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(78.dp)
                ) {
                    Icon(Icons.Rounded.Alarm, contentDescription = null)
                    Text("  Dawai Reminder Set Karein", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Button(
                    onClick = {
                        val engine = kaiApp?.findPhoneAlarmEngine
                        if (engine?.isRinging?.value == true) {
                            engine.stopAlarm()
                        } else {
                            engine?.startAlarm(enableFlashlight = true, enableVibrate = true)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(78.dp)
                ) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null)
                    Text("  Phone Ring / Stop Alarm", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
