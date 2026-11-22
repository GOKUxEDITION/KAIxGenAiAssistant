package com.kaixgen.assistant.ui.screens.productivity

import android.content.Intent
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.domain.voice.SmartMoneyCalculator
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMoneyToolsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tts = (context.applicationContext as? KaiApplication)?.appContainer?.ttsManager
    val ext = KaiTheme.extendedColors

    var principalText by remember { mutableStateOf("100000") }
    var rateText by remember { mutableStateOf("10.5") }
    var monthsText by remember { mutableStateOf("12") }
    val emiRes = SmartMoneyCalculator.calculateEmi(
        principalText.toDoubleOrNull() ?: 0.0,
        rateText.toDoubleOrNull() ?: 0.0,
        monthsText.toIntOrNull() ?: 0
    )

    var gstBaseText by remember { mutableStateOf("1000") }
    var gstRate by remember { mutableDoubleStateOf(18.0) }
    val gstRes = SmartMoneyCalculator.addGst(gstBaseText.toDoubleOrNull() ?: 0.0, gstRate)

    var billText by remember { mutableStateOf("1200") }
    var friendsText by remember { mutableStateOf("4") }
    val perPerson = SmartMoneyCalculator.splitBill(
        billText.toDoubleOrNull() ?: 0.0,
        friendsText.toIntOrNull() ?: 1
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("Smart Money: EMI, GST & Split", fontWeight = FontWeight.Bold) },
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
                        Text("Instant Loan EMI Calculator", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = principalText,
                            onValueChange = { principalText = it },
                            label = { Text("Loan Amount (₹)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it },
                                label = { Text("Rate %/yr") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = monthsText,
                                onValueChange = { monthsText = it },
                                label = { Text("Months") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "Monthly EMI: ₹${emiRes.monthlyEmi}  •  Interest: ₹${emiRes.totalInterest}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = {
                            tts?.speak(
                                "Aapki monthly EMI ${emiRes.monthlyEmi} rupaye hogi aur total interest ${emiRes.totalInterest} rupaye.",
                                languageCode = "hi-IN"
                            )
                        }) {
                            Text("Speak EMI in Hindi")
                        }
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1-Tap GST Calculator", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = gstBaseText,
                            onValueChange = { gstBaseText = it },
                            label = { Text("Base Amount (₹)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5.0, 12.0, 18.0, 28.0).forEach { r ->
                                FilterChip(
                                    selected = gstRate == r,
                                    onClick = { gstRate = r },
                                    label = { Text("${r.toInt()}%") }
                                )
                            }
                        }
                        Text(
                            text = String.format(
                                Locale.US,
                                "GST: ₹%.2f  •  Final Total: ₹%.2f",
                                gstRes.gstAmount,
                                gstRes.totalAmount
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Friends Bill Split & Share", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = billText,
                                onValueChange = { billText = it },
                                label = { Text("Total Bill (₹)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = friendsText,
                                onValueChange = { friendsText = it },
                                label = { Text("Friends") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        val splitText = String.format(Locale.US, "Per Person: ₹%.2f", perPerson)
                        Text(splitText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Bill Split (KAIxGenAssistant): Total ₹$billText / $friendsText friends = $splitText")
                            }
                            context.startActivity(Intent.createChooser(share, "Share Bill Split"))
                        }) {
                            Text("Share Split on WhatsApp")
                        }
                    }
                }
            }
        }
    }
}
