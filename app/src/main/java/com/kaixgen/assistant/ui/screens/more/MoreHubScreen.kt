package com.kaixgen.assistant.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.domain.model.KaiFeatureCategory
import com.kaixgen.assistant.domain.model.KaiFeatureItem
import com.kaixgen.assistant.domain.model.KaiFeatureRegistry
import com.kaixgen.assistant.navigation.KaiRoutes
import com.kaixgen.assistant.navigation.KaiRoutesV2
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.MoreHubUiState

@Composable
fun MoreHubScreen(
    uiState: MoreHubUiState,
    isWakeWordActive: Boolean,
    onRefreshDeviceStatus: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenDrivingMode: () -> Unit,
    onCloseDrivingMode: () -> Unit,
    onLaunchMaps: () -> Unit,
    onRunVoiceAction: (String) -> Unit,
    onLaunchTimer: (Int) -> Unit,
    onAddTask: (String) -> Unit,
    onToggleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDismissBanner: () -> Unit,
    onNavigateToVoiceBuddy: () -> Unit,
    onNavigateToCommandHelp: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSecurityHealth: () -> Unit = {},
    onNavigateToVoiceprint: () -> Unit = {},
    onNavigateToSafetyHub: () -> Unit = {},
    onNavigateToFullDriving: () -> Unit = {},
    onNavigateToInterpreterMemory: () -> Unit = {},
    onNavigateToProductivityHub: () -> Unit = {},
    onNavigateToPrivacyOem: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onNavigateToAiToolsHub: () -> Unit = {},
    onNavigateToAboutKai: () -> Unit = {},
    onNavigateToRealLifeHub: () -> Unit = {},
    onNavigateToSuperUtilities: () -> Unit = {},
    onNavigateToSmartMoney: () -> Unit = {},
    onNavigateToProfileAccount: () -> Unit = {},
    onNavigateToSubscriptionPro: () -> Unit = {},
    onNavigateToHyperSecurity: () -> Unit = {},
    modifier: Modifier = Modifier,
    onNavigateToStudentMode: () -> Unit = {},
    onNavigateToSeniorMode: () -> Unit = {},
) {
    val ext = KaiTheme.extendedColors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<KaiFeatureCategory?>(null) }

    val filtered = remember(searchQuery, selectedCategory) {
        KaiFeatureRegistry.search(searchQuery, selectedCategory)
    }
    val grouped = remember(filtered) {
        filtered.groupBy { it.category }
    }

    fun openFeature(item: KaiFeatureItem) {
        when (item.route) {
            KaiRoutesV2.AI_CHAT -> onNavigateToAiChat()
            KaiRoutesV2.STUDENT_MODE -> onNavigateToStudentMode()
            KaiRoutesV2.SENIOR_MODE -> onNavigateToSeniorMode()
            KaiRoutesV2.AI_TOOLS_HUB -> onNavigateToAiToolsHub()
            KaiRoutesV2.INTERPRETER_MEMORY -> onNavigateToInterpreterMemory()
            KaiRoutesV2.HYPER_SECURITY_CENTER -> onNavigateToHyperSecurity()
            KaiRoutesV2.REAL_LIFE_POWER_HUB -> onNavigateToRealLifeHub()
            KaiRoutesV2.VOICEPRINT_MANAGER -> onNavigateToVoiceprint()
            KaiRoutesV2.SECURITY_HEALTH_INTRUDER -> onNavigateToSecurityHealth()
            KaiRoutesV2.SAFETY_HUB -> onNavigateToSafetyHub()
            KaiRoutesV2.DRIVING_MODE -> onNavigateToFullDriving()
            KaiRoutesV2.SUPER_UTILITIES_HUB -> onNavigateToSuperUtilities()
            KaiRoutesV2.SMART_MONEY_TOOLS -> onNavigateToSmartMoney()
            KaiRoutesV2.PRODUCTIVITY_HUB -> onNavigateToProductivityHub()
            KaiRoutesV2.SUBSCRIPTION_PRO -> onNavigateToSubscriptionPro()
            KaiRoutesV2.PROFILE_ACCOUNT -> onNavigateToProfileAccount()
            KaiRoutesV2.PRIVACY_OEM_WIZARD -> onNavigateToPrivacyOem()
            KaiRoutes.COMMAND_HELP_ALIASES -> onNavigateToCommandHelp()
            KaiRoutes.DIAGNOSTICS -> onNavigateToDiagnostics()
            KaiRoutes.SETTINGS -> onNavigateToSettings()
            KaiRoutesV2.ABOUT_KAI -> onNavigateToAboutKai()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KaiBrandTopBar(
            isListeningActive = isWakeWordActive,
            onSettingsClick = onNavigateToSettings
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Clean Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search features, tools, settings...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = ext.divider
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Compact Quick Device Utility Strip (replaces bulky device card)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("more_device_utilities_card"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactQuickActionPill(
                        icon = Icons.Rounded.BatteryStd,
                        label = "${uiState.batteryPercent}%",
                        active = false,
                        onClick = onRefreshDeviceStatus,
                        modifier = Modifier.weight(1f)
                    )
                    CompactQuickActionPill(
                        icon = Icons.Rounded.FlashlightOn,
                        label = if (uiState.isTorchOn) "Torch On" else "Torch",
                        active = uiState.isTorchOn,
                        onClick = onToggleFlashlight,
                        modifier = Modifier.weight(1f).testTag("more_torch_button")
                    )
                    CompactQuickActionPill(
                        icon = Icons.Rounded.Timer,
                        label = "5m Timer",
                        active = false,
                        onClick = { onLaunchTimer(5) },
                        modifier = Modifier.weight(1f).testTag("more_timer_button")
                    )
                }
            }

            // Category Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                    KaiFeatureCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
            }

            // iOS-Style Grouped Inset Lists (Zero Card Bloat!)
            grouped.forEach { (category, itemsInCat) ->
                item(key = category.name) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = category.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.mutedText,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                itemsInCat.forEachIndexed { idx, feature ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag(feature.testTag)
                                            .clickable { openFeature(feature) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ext.micContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = feature.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = feature.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (feature.isPro) {
                                                    Text(
                                                        text = "PRO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = feature.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = ext.mutedText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    if (idx < itemsInCat.lastIndex) {
                                        HorizontalDivider(
                                            color = ext.divider,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(start = 60.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactQuickActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
