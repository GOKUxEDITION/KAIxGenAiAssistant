package com.kaixgen.assistant.ui.screens.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    var statusNotice by remember { mutableStateOf("") }

    val benefits = listOf(
        "Unlimited Per-App Voice Vault & Custom Routines",
        "Strict Speaker Voiceprint & Intruder Selfie Vault",
        "Double-Clap Find Phone & Anti-Theft Siren Guards",
        "Unlimited Hybrid AI Chat & Hands-Free Driving Pro"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("KAI Pro", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Current Plan: ${activeTier.displayName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Upgrade to KAI Pro",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Core voice commands, Backup PIN and Emergency SOS are always free.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        benefits.forEach { b ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(b, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val plans = listOf(
                            Triple(KaiPlanTier.PRO_YEARLY, "Best Value • Save 37%", "Billed annually"),
                            Triple(KaiPlanTier.PRO_MONTHLY, "Flexible monthly", "Cancel anytime"),
                            Triple(KaiPlanTier.PRO_LIFETIME, "One-time purchase", "Own KAI Pro forever")
                        )
                        plans.forEachIndexed { idx, (tier, tag, sub) ->
                            val isSel = selectedTier == tier
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTier = tier }
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSel, onClick = { selectedTier = tier })
                                Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                                    Text(tier.displayName, fontWeight = FontWeight.SemiBold)
                                    Text("$tag • $sub", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = tier.priceLabelInr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (idx < plans.lastIndex) {
                                HorizontalDivider(color = ext.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        manager.activateTier(selectedTier)
                        statusNotice = "${selectedTier.displayName} is now active."
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Continue — ${selectedTier.priceLabelInr}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { statusNotice = "Checking previous purchases..." }) {
                        Text("Restore Purchases")
                    }
                    if (activeTier != KaiPlanTier.FREE) {
                        TextButton(onClick = {
                            manager.resetToFree()
                            statusNotice = "Switched to KAI Free."
                        }) {
                            Text("Switch to Free")
                        }
                    }
                }
                if (statusNotice.isNotBlank()) {
                    KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = statusNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}
