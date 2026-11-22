package com.kaixgen.assistant.ui.screens.assistant

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class MemoryFactItem(val id: Long, val key: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterpreterAndMemoryScreen(
    onSpeakText: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var phraseInput by remember { mutableStateOf("") }
    var memoryKey by remember { mutableStateOf("") }
    var memoryValue by remember { mutableStateOf("") }
    val memories = remember {
        mutableStateListOf(
            MemoryFactItem(1L, "Wi-Fi Guest Name", "KAIxGen_5G"),
            MemoryFactItem(2L, "Bike Parking", "Basement Pillar B4")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interpreter & Memory Vault") },
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
                            Icon(Icons.Default.Translate, contentDescription = null)
                            Text(" Voice Interpreter (Hindi <-> English)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phraseInput,
                            onValueChange = { phraseInput = it },
                            label = { Text("Type or speak phrase to read aloud") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { onSpeakText(phraseInput, "hi-IN") }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null)
                                Text(" Speak Hindi")
                            }
                            Button(onClick = { onSpeakText(phraseInput, "en-IN") }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null)
                                Text(" Speak English")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Memory Vault (F-AI-11)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = memoryKey,
                            onValueChange = { memoryKey = it },
                            label = { Text("Label (e.g., Car Number)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = memoryValue,
                            onValueChange = { memoryValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            if (memoryKey.isNotBlank() && memoryValue.isNotBlank()) {
                                memories.add(0, MemoryFactItem(System.currentTimeMillis(), memoryKey.trim(), memoryValue.trim()))
                                memoryKey = ""
                                memoryValue = ""
                            }
                        }) {
                            Text("Save to Memory Vault")
                        }
                    }
                }
            }

            items(memories, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.key, fontWeight = FontWeight.SemiBold)
                            Text(item.value, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { memories.remove(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Memory")
                        }
                    }
                }
            }
        }
    }
}
