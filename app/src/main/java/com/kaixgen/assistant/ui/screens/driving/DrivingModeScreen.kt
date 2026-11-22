package com.kaixgen.assistant.ui.screens.driving

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingModeScreen(
    onSpeakCommand: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var parkedNote by remember { mutableStateOf("Abhi koi parking spot save nahi hai.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KAI Driving Mode") },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Home"))
                        runCatching { context.startActivity(mapIntent) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Text(" Maps / Ghar", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL)
                        runCatching { context.startActivity(dialIntent) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Text(" Call", fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val musicIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MUSIC)
                        }
                        runCatching { context.startActivity(musicIntent) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Text(" Music", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
                        runCatching { context.startActivity(smsIntent) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                ) {
                    Icon(Icons.Default.Message, contentDescription = null)
                    Text(" Messages", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onSpeakCommand,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Text(" Tap to Speak to KAI", style = MaterialTheme.typography.titleMedium)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null)
                        Text(" Parked-Car Memory (F-DR-06)", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(parkedNote, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        val time = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date())
                        parkedNote = "Car yahin park ki gayi hai — Saved at $time"
                    }) {
                        Text("Save Parking Spot Now")
                    }
                }
            }
        }
    }
}
