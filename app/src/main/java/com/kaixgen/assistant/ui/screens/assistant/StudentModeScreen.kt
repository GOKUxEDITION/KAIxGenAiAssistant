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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme
import kotlinx.coroutines.delay

private data class StudyFlashcard(val subject: String, val question: String, val explanation: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentModeScreen(
    onOpenAiTutorChat: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tts = (context.applicationContext as? KaiApplication)?.appContainer?.ttsManager
    val ext = KaiTheme.extendedColors

    val flashcards = remember {
        listOf(
            StudyFlashcard("Physics", "Newton's Second Law kya kehta hai?", "Force = Mass × Acceleration (F = m·a). Jitna zyada mass ya acceleration, utna zyada force."),
            StudyFlashcard("Computer Science", "Big-O O(log n) ka matlab kya hai?", "Har step par search space aadha ho jata hai, jaise Binary Search me."),
            StudyFlashcard("Mathematics", "Pythagoras Theorem kya hai?", "Right-angled triangle me: Hypotenuse² = Base² + Height² (a² + b² = c²)."),
            StudyFlashcard("Biology", "Mitochondria ko powerhouse kyun kehte hain?", "Kyunki ye cellular respiration ke through ATP energy banata hai.")
        )
    }

    var cardIdx by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, secondsLeft) {
        if (running && secondsLeft > 0) {
            delay(1_000L)
            secondsLeft--
        } else if (secondsLeft == 0 && running) {
            running = false
            tts?.speak("Study session complete! 5 minute ka break lein.", languageCode = "hi-IN")
        }
    }

    val currentCard = flashcards[cardIdx]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ext.secondaryBackground),
                title = { Text("Student Study Mode", fontWeight = FontWeight.Bold) },
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
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Revision Flashcard • ${currentCard.subject}", fontWeight = FontWeight.Bold)
                        }
                        Text(currentCard.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (showAnswer) {
                            Text(currentCard.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { showAnswer = !showAnswer }) {
                                Text(if (showAnswer) "Hide Answer" else "Show Explanation")
                            }
                            OutlinedButton(onClick = {
                                showAnswer = false
                                cardIdx = (cardIdx + 1) % flashcards.size
                            }) {
                                Text("Next Card")
                            }
                        }
                    }
                }
            }

            item {
                KaiGlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Deep Study Focus Timer", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { running = !running }) {
                                Text(if (running) "Pause" else "Start 25m")
                            }
                            OutlinedButton(onClick = { running = false; secondsLeft = 45 * 60 }) {
                                Text("45m Exam")
                            }
                            OutlinedButton(onClick = { running = false; secondsLeft = 25 * 60 }) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onOpenAiTutorChat,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("Ask KAI AI Tutor (Concept Explanation)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
