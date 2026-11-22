package com.kaixgen.assistant.ui.screens.productivity

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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.domain.voice.AdvancedIntentResult
import com.kaixgen.assistant.domain.voice.ExpenseAndMemoryParser
import kotlinx.coroutines.delay

data class ExpenseEntry(val id: Long, val amount: Int, val category: String, val note: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityHubScreen(
    onBack: () -> Unit
) {
    val parser = remember { ExpenseAndMemoryParser() }
    var expenseInput by remember { mutableStateOf("") }
    val expenses = remember { mutableStateListOf<ExpenseEntry>() }

    var secondsLeft by remember { mutableIntStateOf(25 * 60) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning, secondsLeft) {
        if (timerRunning && secondsLeft > 0) {
            delay(1_000L)
            secondsLeft--
        } else if (secondsLeft == 0) {
            timerRunning = false
        }
    }

    val totalSpent = expenses.sumOf { it.amount }
    val mins = secondsLeft / 60
    val secs = secondsLeft % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productivity: Expenses & Focus") },
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
                        Row {
                            Icon(Icons.Default.Timer, contentDescription = null)
                            Text(" Focus Pomodoro Timer (F-PR-10)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { timerRunning = !timerRunning }) {
                                Text(if (timerRunning) "Pause" else "Start Focus")
                            }
                            OutlinedButton(onClick = {
                                timerRunning = false
                                secondsLeft = 25 * 60
                            }) {
                                Text("Reset 25m")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Voice Expense Tracker (Total: ₹$totalSpent)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = expenseInput,
                            onValueChange = { expenseInput = it },
                            label = { Text("e.g., 200 rupaye chai pe") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val parsed = parser.parse(expenseInput)
                            if (parsed is AdvancedIntentResult.AddExpense) {
                                expenses.add(
                                    0,
                                    ExpenseEntry(
                                        id = System.currentTimeMillis(),
                                        amount = parsed.amountRupees,
                                        category = parsed.category,
                                        note = parsed.note
                                    )
                                )
                                expenseInput = ""
                            }
                        }) {
                            Text("Add Expense")
                        }
                    }
                }
            }

            items(expenses, key = { it.id }) { exp ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(exp.note, fontWeight = FontWeight.SemiBold)
                            Text(exp.category, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹${exp.amount}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
