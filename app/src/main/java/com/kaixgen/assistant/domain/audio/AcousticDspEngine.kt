package com.kaixgen.assistant.domain.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class AcousticDspEngine(
    private val sampleRateHz: Int = 16_000,
    private val vadThresholdDb: Float = -32f,
    private val clapThresholdDb: Float = -12f,
    private val minClapGapMs: Long = 160L,
    private val maxClapGapMs: Long = 750L,
    private val clapCooldownMs: Long = 3_000L
) {
    private var lastClapPeakMs: Long = 0L
    private var lastDoubleClapTriggerMs: Long = 0L
    private var noiseFloorDb: Float = -55f

    data class FrameAnalysis(
        val rmsDb: Float,
        val zeroCrossingRate: Float,
        val isSpeech: Boolean,
        val isDoubleClap: Boolean,
        val peakRatio: Float
    )

    fun analyzeFrame(pcm: ShortArray, nowMs: Long = System.currentTimeMillis()): FrameAnalysis {
        if (pcm.isEmpty()) {
            return FrameAnalysis(-90f, 0f, isSpeech = false, isDoubleClap = false, peakRatio = 0f)
        }

        var sumSquares = 0.0
        var peak = 0
        var zeroCrossings = 0

        for (i in pcm.indices) {
            val sample = pcm[i].toInt()
            val absSample = abs(sample)
            if (absSample > peak) peak = absSample
            sumSquares += sample.toDouble() * sample.toDouble()
            if (i > 0 && ((pcm[i - 1] >= 0 && pcm[i] < 0) || (pcm[i - 1] < 0 && pcm[i] >= 0))) {
                zeroCrossings++
            }
        }

        val rms = sqrt(sumSquares / pcm.size)
        val normalizedRms = (rms / 32768.0).coerceAtLeast(1e-6)
        val rmsDb = (20.0 * log10(normalizedRms)).toFloat()
        val zcr = zeroCrossings.toFloat() / pcm.size.toFloat()
        val peakRatio = if (rms > 1.0) (peak / rms).toFloat() else 0f

        if (rmsDb < noiseFloorDb + 6f) {
            noiseFloorDb = (noiseFloorDb * 0.95f) + (rmsDb * 0.05f)
        }

        val adaptiveSpeechFloor = max(vadThresholdDb, noiseFloorDb + 10f)
        val isSpeech = rmsDb > adaptiveSpeechFloor && zcr in 0.015f..0.32f

        var doubleClapDetected = false
        val isImpulsiveClap = rmsDb > max(clapThresholdDb, noiseFloorDb + 18f) && peakRatio >= 3.0f
        if (isImpulsiveClap) {
            val gap = nowMs - lastClapPeakMs
            val sinceTrigger = nowMs - lastDoubleClapTriggerMs
            if (gap in minClapGapMs..maxClapGapMs && sinceTrigger >= clapCooldownMs) {
                doubleClapDetected = true
                lastDoubleClapTriggerMs = nowMs
                lastClapPeakMs = 0L
            } else if (gap > maxClapGapMs || lastClapPeakMs == 0L) {
                lastClapPeakMs = nowMs
            }
        }

        return FrameAnalysis(
            rmsDb = rmsDb,
            zeroCrossingRate = zcr,
            isSpeech = isSpeech,
            isDoubleClap = doubleClapDetected,
            peakRatio = peakRatio
        )
    }

    fun extractVoiceprintEmbedding(pcm: ShortArray): FloatArray {
        val bandCentersHz = floatArrayOf(
            180f, 260f, 360f, 480f, 620f, 780f, 960f, 1180f,
            1420f, 1700f, 2020f, 2380f, 2780f, 3220f, 3680f, 4150f
        )
        val raw = FloatArray(bandCentersHz.size)
        if (pcm.isEmpty()) return raw

        val windowed = DoubleArray(pcm.size) { i ->
            val w = 0.5 * (1.0 - cos(2.0 * PI * i / (pcm.size - 1).coerceAtLeast(1)))
            (pcm[i] / 32768.0) * w
        }

        for (b in bandCentersHz.indices) {
            val power = goertzelPower(windowed, bandCentersHz[b], sampleRateHz)
            raw[b] = ln(power + 1e-8).toFloat()
        }

        val mean = raw.average().toFloat()
        var normSq = 0.0
        for (i in raw.indices) {
            raw[i] -= mean
            normSq += raw[i] * raw[i]
        }
        val norm = sqrt(normSq).toFloat().coerceAtLeast(1e-6f)
        for (i in raw.indices) {
            raw[i] /= norm
        }
        return raw
    }

    fun voiceprintSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.isEmpty() || a.size != b.size) return 0f
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 1e-6) (dot / denom).toFloat().coerceIn(-1f, 1f) else 0f
    }

    private fun goertzelPower(samples: DoubleArray, targetFreqHz: Float, sampleRate: Int): Double {
        val omega = 2.0 * PI * targetFreqHz / sampleRate
        val coeff = 2.0 * cos(omega)
        var q0: Double
        var q1 = 0.0
        var q2 = 0.0
        for (sample in samples) {
            q0 = coeff * q1 - q2 + sample
            q2 = q1
            q1 = q0
        }
        return (q1 * q1 + q2 * q2 - q1 * q2 * coeff).coerceAtLeast(0.0) / samples.size
    }
}
