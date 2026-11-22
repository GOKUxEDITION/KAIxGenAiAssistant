package com.kaixgen.assistant.domain.routine

import com.kaixgen.assistant.domain.voice.HinglishVoiceCommandParser
import com.kaixgen.assistant.domain.voice.KaiActionExecutor
import com.kaixgen.assistant.domain.voice.KaiTtsManager
import kotlinx.coroutines.delay

class DynamicRoutineEngine(
    private val actionExecutor: KaiActionExecutor,
    private val parser: HinglishVoiceCommandParser,
    private val ttsManager: KaiTtsManager
) {
    data class RoutineStepResult(
        val stepCommand: String,
        val success: Boolean,
        val message: String
    )

    data class RoutineRunSummary(
        val routineName: String,
        val completedSteps: Int,
        val totalSteps: Int,
        val stepResults: List<RoutineStepResult>
    )

    suspend fun runSteps(
        routineName: String,
        stepCommands: List<String>,
        speakSummary: Boolean = true,
        interStepDelayMs: Long = 350L
    ): RoutineRunSummary {
        val results = mutableListOf<RoutineStepResult>()
        for (rawStep in stepCommands) {
            val trimmed = rawStep.trim()
            if (trimmed.isBlank()) continue
            val parsed = parser.parse(trimmed)
            val exec = actionExecutor.execute(parsed, confirmed = true)
            results += RoutineStepResult(
                stepCommand = trimmed,
                success = exec.isSuccess,
                message = exec.spokenFeedback
            )
            delay(interStepDelayMs)
        }

        val completed = results.count { it.success }
        if (speakSummary) {
            ttsManager.speak(
                "$routineName routine complete ho gaya. $completed me se ${results.size} steps run hue.",
                languageCode = "hi-IN"
            )
        }
        return RoutineRunSummary(
            routineName = routineName,
            completedSteps = completed,
            totalSteps = results.size,
            stepResults = results
        )
    }
}
