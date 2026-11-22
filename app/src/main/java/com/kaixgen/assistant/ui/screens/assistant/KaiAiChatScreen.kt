package com.kaixgen.assistant.ui.screens.assistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.domain.ai.GeminiHybridAssistant
import com.kaixgen.assistant.ui.components.KaiAiThinkingIndicator
import com.kaixgen.assistant.ui.components.KaiEmptyStateCard
import com.kaixgen.assistant.ui.components.KaiErrorRecoveryCard
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class KaiChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val sourceLabel: String = "KAI"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaiAiChatScreen(
    onOpenVoiceMode: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kaiApp = context.applicationContext as? KaiApplication
    val ext = KaiTheme.extendedColors
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember { mutableStateListOf<KaiChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    fun sendMessage(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank() || isGenerating) return
        errorMessage = null
        messages.add(KaiChatMessage(System.currentTimeMillis(), isUser = true, text = clean, sourceLabel = "You"))
        inputText = ""
        isGenerating = true

        activeJob = scope.launch {
            val parser = kaiApp?.appContainer?.commandParser
            val executor = kaiApp?.appContainer?.actionExecutor
            val hybrid = kaiApp?.geminiHybridAssistant

            val parsed = parser?.parse(clean)
            if (parsed != null && parsed.intent.name != "CLARIFY" && executor != null) {
                val exec = executor.execute(parsed, confirmed = false)
                val reply = when (exec) {
                    is com.kaixgen.assistant.domain.voice.ActionExecutionResult.Executed -> exec.displayMessage
                    is com.kaixgen.assistant.domain.voice.ActionExecutionResult.NeedsConfirmation -> exec.promptMessage
                    is com.kaixgen.assistant.domain.voice.ActionExecutionResult.Failure -> exec.errorMessage
                }
                messages.add(KaiChatMessage(System.currentTimeMillis() + 1, isUser = false, text = reply, sourceLabel = "KAI On-Device"))
                isGenerating = false
                return@launch
            }

            if (hybrid != null) {
                when (val res = hybrid.askHinglishAssistant(clean)) {
                    is GeminiHybridAssistant.HybridAiResult.Reply -> {
                        messages.add(KaiChatMessage(System.currentTimeMillis() + 1, isUser = false, text = res.spokenText, sourceLabel = "KAI Hybrid AI"))
                    }
                    is GeminiHybridAssistant.HybridAiResult.Unavailable -> {
                        errorMessage = res.reason
                    }
                }
            } else {
                errorMessage = "Assistant engine unavailable."
            }
            isGenerating = false
        }
    }

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = {
                    Column {
                        Text("KAI AI Chat", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Offline NLU + Opt-In Gemini Hybrid",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { messages.clear(); errorMessage = null }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear Chat")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        KaiEmptyStateCard(
                            title = "Ask KAIxGenAssistant Anything",
                            subtitle = "Type a Hinglish command, coding question, or tap the mic for hands-free voice control.",
                            icon = Icons.Rounded.AutoAwesome,
                            actionLabel = "Try: Battery status",
                            onAction = { sendMessage("battery kitni hai") }
                        )
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    ChatBubbleRow(
                        message = msg,
                        onCopy = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("KAI Response", msg.text))
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg.text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share KAI Response"))
                        },
                        onRetry = {
                            val lastUser = messages.lastOrNull { it.isUser }?.text
                            if (!lastUser.isNullOrBlank()) sendMessage(lastUser)
                        }
                    )
                }

                if (isGenerating) {
                    item {
                        KaiAiThinkingIndicator()
                    }
                }

                errorMessage?.let { err ->
                    item {
                        KaiErrorRecoveryCard(
                            title = "Could not complete request",
                            message = err,
                            onRetry = {
                                val lastUser = messages.lastOrNull { it.isUser }?.text
                                errorMessage = null
                                if (!lastUser.isNullOrBlank()) sendMessage(lastUser)
                            }
                        )
                    }
                }
            }

            // Advanced Expandable AI Input Panel (Screen 08)
            Surface(
                color = ext.secondaryBackground,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onOpenVoiceMode,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ext.elevatedSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message KAIxGenAssistant...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = ext.divider,
                            focusedContainerColor = ext.elevatedSurface,
                            unfocusedContainerColor = ext.elevatedSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (isGenerating) {
                                activeJob?.cancel()
                                isGenerating = false
                            } else {
                                sendMessage(inputText)
                            }
                        },
                        enabled = isGenerating || inputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGenerating || inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else ext.elevatedSurface
                            )
                    ) {
                        Icon(
                            imageVector = if (isGenerating) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.Send,
                            contentDescription = if (isGenerating) "Stop" else "Send",
                            tint = if (isGenerating || inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                            else ext.mutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleRow(
    message: KaiChatMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit
) {
    val ext = KaiTheme.extendedColors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (message.isUser) {
            Surface(
                shape = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            KaiGlassPanel(modifier = Modifier.fillMaxWidth(0.94f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val isCodeLike = message.text.contains("```")
                    Text(
                        text = message.text,
                        style = if (isCodeLike) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                        else MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = ext.mutedText, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRetry, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Retry", tint = ext.mutedText, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onShare, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = ext.mutedText, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
