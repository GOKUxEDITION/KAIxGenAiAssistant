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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme

private data class AiToolCardItem(
    val category: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiToolsHubScreen(
    onOpenAiChat: () -> Unit,
    onOpenInterpreterMemory: () -> Unit,
    onOpenProductivity: () -> Unit,
    onOpenDrivingMode: () -> Unit,
    onOpenSecurityHealth: () -> Unit,
    onOpenVoiceprint: () -> Unit,
    onOpenSafetyHub: () -> Unit,
    onOpenPrivacyWizard: () -> Unit,
    onBack: () -> Unit
) {
    val ext = KaiTheme.extendedColors
    val tools = listOf(
        AiToolCardItem("AI & Writing", "KAI AI Chat", "Offline Hinglish NLU + Opt-In Gemini Hybrid Q&A", Icons.AutoMirrored.Rounded.Chat, onOpenAiChat),
        AiToolCardItem("Language & Memory", "Interpreter & Memory Vault", "Hindi/English speech interpreter + saved facts", Icons.Rounded.Translate, onOpenInterpreterMemory),
        AiToolCardItem("Productivity", "Expenses & Focus Pomodoro", "Voice expense tracking & 25-min focus timer", Icons.Rounded.Timer, onOpenProductivity),
        AiToolCardItem("Commute", "Driving Mode Cockpit", "Giant hands-free controls & parked-car memory", Icons.Rounded.DirectionsCar, onOpenDrivingMode),
        AiToolCardItem("Security", "Security Health & Intruders", "0–100 security score & private intruder selfie vault", Icons.Rounded.Security, onOpenSecurityHealth),
        AiToolCardItem("Biometrics", "Speaker Voiceprint Manager", "3-sample enrollment & dual-factor strictness", Icons.Rounded.GraphicEq, onOpenVoiceprint),
        AiToolCardItem("Personal Safety", "SOS, Fake Call & Medical Card", "Emergency siren, SOS share & fake call escape", Icons.Rounded.Shield, onOpenSafetyHub),
        AiToolCardItem("Device & Privacy", "Privacy Center & OEM Wizard", "Vivo/Xiaomi/Samsung battery survival & opt-in controls", Icons.Rounded.PrivacyTip, onOpenPrivacyWizard)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("KAI AI Tools Hub", fontWeight = FontWeight.Bold) },
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
            items(tools) { tool ->
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tool.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tool.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = tool.onClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Open ${tool.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
