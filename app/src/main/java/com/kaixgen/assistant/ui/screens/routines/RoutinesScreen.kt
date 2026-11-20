package com.kaixgen.assistant.ui.screens.routines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.domain.model.RoutineItem
import com.kaixgen.assistant.ui.components.KaiBrandSignatureBadge
import com.kaixgen.assistant.ui.components.KaiBrandTopBar
import com.kaixgen.assistant.ui.components.KaiCircularIconBadge
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiTwoToneHeadline
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.viewmodel.RoutinesUiState

/**
 * Voice Routines Hub & Builder Screen (PRD S8-01 / S8-02 / S8-03 / S8-09).
 * Persists custom voice routines in Room DB and executes actions live via KaiActionExecutor.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutinesScreen(
    uiState: RoutinesUiState,
    isWakeWordActive: Boolean,
    onToggleRoutine: (String, Boolean) -> Unit,
    onRunRoutineNow: (RoutineItem) -> Unit,
    onOpenCreateSheet: () -> Unit,
    onCloseCreateSheet: () -> Unit,
    onCreateRoutine: (String, String, List<String>) -> Unit,
    onDeleteRoutine: (String) -> Unit,
    onDismissBanner: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors

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
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        KaiTwoToneHeadline(
                            prefix = "Voice ",
                            highlighted = "Routines"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "One spoken phrase runs multiple actions hands-free.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenCreateSheet,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("create_routine_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "New routine",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                val templates = listOf(
                    Triple("Morning Brief", "good morning", listOf("Announce Time & Battery", "Open WhatsApp")),
                    Triple("Night Guard", "good night", listOf("Arm Voice Lock", "Lower Media Volume")),
                    Triple("Pooja Quiet", "pooja time", listOf("Lower Media Volume", "Arm Voice Lock")),
                    Triple("Torch Alert", "emergency flash", listOf("Turn On Flashlight", "Ring Find Phone"))
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quick Templates (One-Tap Add)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(templates) { (name, phrase, actions) ->
                            FilterChip(
                                selected = false,
                                onClick = { onCreateRoutine(name, phrase, actions) },
                                label = { Text("+ $name") },
                                modifier = Modifier.testTag("routine_template_${phrase.replace(" ", "_")}")
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = uiState.lastRunBanner != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = extendedColors.lockContainer,
                        border = BorderStroke(1.dp, extendedColors.success.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = extendedColors.success,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.lastRunBanner.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissBanner,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(uiState.routines, key = { it.id }) { routine ->
                val isCustom = routine.id.startsWith("custom_")
                KaiElevatedCard(
                    modifier = Modifier.testTag("routine_card_${routine.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KaiCircularIconBadge(
                                icon = if (isCustom) Icons.Rounded.AutoAwesome else Icons.Rounded.Bolt,
                                containerColor = extendedColors.micContainer,
                                iconTint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = routine.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Say \u201CHey KAI, ${routine.triggerPhrase}\u201D",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Switch(
                                checked = routine.enabled,
                                onCheckedChange = { onToggleRoutine(routine.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Text(
                            text = routine.actionsSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onRunRoutineNow(routine) },
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("run_routine_${routine.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run Now", style = MaterialTheme.typography.labelMedium)
                            }

                            if (isCustom) {
                                IconButton(
                                    onClick = { onDeleteRoutine(routine.id) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete routine",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KaiBrandSignatureBadge()
                }
            }
        }
    }

    if (uiState.showCreateSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var name by remember { mutableStateOf("") }
        var trigger by remember { mutableStateOf("") }
        val availableActions = listOf(
            "Announce Time & Battery",
            "Arm Voice Lock",
            "Lower Media Volume",
            "Turn On Flashlight",
            "Open WhatsApp",
            "Ring Find Phone"
        )
        val pickedActions = remember { mutableStateListOf("Announce Time & Battery", "Arm Voice Lock") }

        ModalBottomSheet(
            onDismissRequest = onCloseCreateSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Create Voice Routine",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    placeholder = { Text("e.g. Pooja Time / Movie Night") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_name_input")
                )

                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Voice Trigger Phrase") },
                    placeholder = { Text("e.g. movie night") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_trigger_input")
                )

                Text(
                    text = "Select Actions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableActions.forEach { action ->
                        val selected = action in pickedActions
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) pickedActions.remove(action) else pickedActions.add(action)
                            },
                            label = { Text(action, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Button(
                    onClick = { onCreateRoutine(name, trigger, pickedActions.toList()) },
                    enabled = name.isNotBlank() && trigger.isNotBlank() && pickedActions.isNotEmpty(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_routine_button")
                ) {
                    Text("Save Routine", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
