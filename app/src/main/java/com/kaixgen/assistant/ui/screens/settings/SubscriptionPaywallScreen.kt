package com.kaixgen.assistant.ui.screens.settings

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
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaixgen.assistant.domain.billing.KaiPlanTier
import com.kaixgen.assistant.domain.billing.KaiSubscriptionManager
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPaywallScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ext = KaiTheme.extendedColors
    val manager = remember(context) { KaiSubscriptionManager(context) }
    val activeTier by manager.currentTier.collectAsStateWithLifecycle()

    var selectedTier by remember { mutableStateOf(KaiPlanTier.PRO_YEARLY) }
    var feedbackMsg by remember { mutableStateOf("") }

    val comparisonRows = listOf(
        Triple("Hinglish Voice, Calculator, Direct WA", "Free", "Free"),
        Triple("SOS Emergency, 112, Fake Call & PIN", "Free", "Free"),
        Triple("Per-App Voice Vault", "3 Apps", "Unlimited"),
        Triple("Custom Multi-Step Routines", "2 Routines", "Unlimited"),
        Triple("Hybrid Gemini AI Chat", "20 / day", "Unlimited"),
        Triple("Strict Voiceprint + Intruder Selfie", "—", "Pro"),
        Triple("Double-Clap + Anti-Theft Guard", "—", "Pro"),
        Triple("Driving Mode Pro + Battery Alerts", "—", "Pro")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("KAI Pro Subscription", fontWeight = FontWeight.Bold) },
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
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Active Plan: ${activeTier.displayName}", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Core voice commands, Backup PIN aur Emergency SOS hamesha FREE rahenge. Power automation & security upgrades ke liye KAI Pro unlock karein.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Free vs Pro Comparison Card
            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Feature", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                            Text("Free", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                            Text("Pro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(0.8f))
                        }
                        comparisonRows.forEach { (name, freeVal, proVal) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.8f))
                                Text(freeVal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.7f))
                                Text(proVal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
                            }
                        }
                    }
                }
            }

            // Plan Selector Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple(KaiPlanTier.PRO_MONTHLY, "Flexible monthly billing", "Cancel anytime"),
                        Triple(KaiPlanTier.PRO_YEARLY, "Best Value • Save 37%", "Most popular in India"),
                        Triple(KaiPlanTier.PRO_LIFETIME, "Pay once, own forever", "No recurring charges")
                    ).forEach { (tier, sub, badge) ->
                        val isSel = selectedTier == tier
                        KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = isSel) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSel, onClick = { selectedTier = tier })
                                Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                                    Text("${tier.displayName} — ${tier.priceLabelInr}", fontWeight = FontWeight.Bold)
                                    Text("$sub • $badge", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        manager.activateTier(selectedTier)
                        feedbackMsg = "${selectedTier.displayName} preview activated (SKU: ${selectedTier.skuId}). Connect Google Play Billing in release build for live UPI/Card checkout."
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Activate ${selectedTier.displayName} (${selectedTier.priceLabelInr})", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { feedbackMsg = "Play Billing restore check ready for SKU ${selectedTier.skuId}." },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restore")
                    }
                    OutlinedButton(
                        onClick = {
                            manager.resetToFree()
                            feedbackMsg = "Switched back to KAI Free tier."
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Switch to Free")
                    }
                }
                if (feedbackMsg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(feedbackMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
