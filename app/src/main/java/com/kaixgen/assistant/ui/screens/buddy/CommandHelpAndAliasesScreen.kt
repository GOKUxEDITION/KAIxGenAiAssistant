package com.kaixgen.assistant.ui.screens.buddy

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.domain.model.VoiceAliasItem
import com.kaixgen.assistant.ui.components.KaiCircularIconBadge
import com.kaixgen.assistant.ui.components.KaiElevatedCard
import com.kaixgen.assistant.ui.components.KaiSubScreenTopBar
import com.kaixgen.assistant.ui.theme.KaiTheme

private data class CommandCategoryGuide(
    val title: String,
    val examples: List<Pair<String, String>>
)

private val COMMAND_CATALOG = listOf(
    CommandCategoryGuide(
        title = "Apps & Navigation",
        examples = listOf(
            "KAI, Open WhatsApp" to "WhatsApp kholo / व्हाट्सऐप खोलो",
            "Open Wi-Fi settings" to "Wi-Fi settings kholo",
            "Search for biryani near me" to "Biryani near me search karo"
        )
    ),
    CommandCategoryGuide(
        title = "Calls & Messaging",
        examples = listOf(
            "Call Mummy" to "Mummy ko call karo",
            "Message Rahul I'm late" to "Rahul ko message karo main late hoon"
        )
    ),
    CommandCategoryGuide(
        title = "Device Controls & Status",
        examples = listOf(
            "Turn on flashlight" to "Torch chalu karo / Torch band karo",
            "Volume 60 percent" to "Awaaz kam karo",
            "How much battery?" to "Kitni battery hai?",
            "What's the time?" to "Kitne baje hain?"
        )
    ),
    CommandCategoryGuide(
        title = "Security & Find Phone",
        examples = listOf(
            "Lock Instagram" to "Instagram lock karo",
            "Where are you?" to "KAI, phone kahan hai?",
            "Good morning" to "Sleep mode / Driving mode"
        )
    )
)

/**
 * Command Catalog ("What can I say?") + Room-backed Custom Hinglish Alias Manager (PRD S2-08 & S5-02).
 */
@Composable
fun CommandHelpAndAliasesScreen(
    aliases: List<VoiceAliasItem>,
    onBackClick: () -> Unit,
    onTryCommand: (String) -> Unit,
    onAddAlias: (String, String, String) -> Unit,
    onDeleteAlias: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddAliasDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KaiSubScreenTopBar(
            onBackClick = onBackClick,
            title = "Voice Guide & Aliases"
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("What Can I Say?") },
                modifier = Modifier.testTag("help_tab_commands")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Aliases (${aliases.size})") },
                modifier = Modifier.testTag("help_tab_aliases")
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(COMMAND_CATALOG) { group ->
                    KaiElevatedCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            group.examples.forEach { (english, hinglish) ->
                                Surface(
                                    onClick = { onTryCommand(english) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "\u201C$english\u201D",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = hinglish,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = "Try command",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Button(
                        onClick = { showAddAliasDialog = true },
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_alias_button")
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Hinglish / Hindi Alias", fontWeight = FontWeight.Bold)
                    }
                }

                items(aliases, key = { it.id }) { alias ->
                    KaiElevatedCard(cornerRadius = 18.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KaiCircularIconBadge(
                                icon = Icons.Rounded.Translate,
                                containerColor = extendedColors.micContainer,
                                iconTint = MaterialTheme.colorScheme.primary,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "\u201C${alias.spoken}\u201D \u2192 ${alias.target}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${alias.kind} alias \u2022 Stored locally in Room",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onDeleteAlias(alias.id) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete alias",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAliasDialog) {
        var spoken by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAliasDialog = false },
            title = { Text("Add Custom Alias") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = spoken,
                        onValueChange = { spoken = it },
                        label = { Text("When I say") },
                        placeholder = { Text("e.g. insta / chacha / map") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Target App") },
                        placeholder = { Text("e.g. Instagram / Google Maps") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddAlias("APP", spoken, target)
                        showAddAliasDialog = false
                    },
                    enabled = spoken.isNotBlank() && target.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAliasDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
