package com.kaixgen.assistant.domain.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class AcousticDspEngineTest {

    private val engine = AcousticDspEngine()

    @Test
    fun silenceFrame_isNotSpeechOrClap() {
        val silence = ShortArray(1600) { 0 }
        val result = engine.analyzeFrame(silence, nowMs = 1_000L)
        assertFalse(result.isSpeech)
        assertFalse(result.isDoubleClap)
    }

    @Test
    fun sameSpeakerTone_hasHighVoiceprintSimilarity() {
        val sampleA = generateHarmonicVoice(f0 = 180.0)
        val sampleB = generateHarmonicVoice(f0 = 182.0)
        val embA = engine.extractVoiceprintEmbedding(sampleA)
        val embB = engine.extractVoiceprintEmbedding(sampleB)
        val sim = engine.voiceprintSimilarity(embA, embB)
        assertTrue("Expected high similarity >= 0.85, got $sim", sim >= 0.85f)
    }

    @Test
    fun doubleClapImpulses_triggerDoubleClapDetection() {
        val impulse = ShortArray(1600) { idx ->
            if (idx in 200..349) 30_000 else 20
        }
        val first = engine.analyzeFrame(impulse, nowMs = 5_000L)
        assertFalse(first.isDoubleClap)
        val second = engine.analyzeFrame(impulse, nowMs = 5_350L)
        assertTrue("Expected double clap trigger on second impulse", second.isDoubleClap)
    }

    private fun generateHarmonicVoice(f0: Double): ShortArray {
        val sr = 16_000.0
        return ShortArray(3200) { i ->
            val t = i / sr
            val s = 0.5 * sin(2 * PI * f0 * t) +
                0.3 * sin(2 * PI * (f0 * 2.0) * t) +
                0.2 * sin(2 * PI * (f0 * 3.0) * t)
            (s * 18_000).toInt().toShort()
        }
    }
}
