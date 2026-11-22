package com.kaixgen.assistant.domain.voice

import java.util.Locale
import kotlin.math.roundToInt

/**
 * PRD F-PR-12 & F-UT-01 Offline Hinglish Quick Calculator & Unit Converter:
 * - "2500 ka 18 percent kitna hota hai" -> "2500 ka 18% = 450"
 * - "450 plus 1280" / "1200 minus 350" / "25 into 12" / "1000 divide 4"
 * - "5 km in meters" / "2 kg in grams" / "37 c in f"
 */
class VoiceUtilityCalculator {

    private val percentRegex = Regex(
        """(\d+(?:\.\d+)?)\s*(?:ka|of)\s*(\d+(?:\.\d+)?)\s*(?:%|percent|pratishat)""",
        RegexOption.IGNORE_CASE
    )
    private val mathRegex = Regex(
        """(\d+(?:\.\d+)?)\s*(\+|plus|jod|-|minus|ghata|\*|x|into|guna|/|divide|bhag)\s*(\d+(?:\.\d+)?)""",
        RegexOption.IGNORE_CASE
    )
    private val unitRegex = Regex(
        """(\d+(?:\.\d+)?)\s*(km|m|kg|g|c|celsius|f|fahrenheit)\s*(?:in|to|me|mein)\s*(km|m|meters|kg|g|grams|c|celsius|f|fahrenheit)""",
        RegexOption.IGNORE_CASE
    )

    fun evaluate(query: String): String? {
        val text = query.trim()

        percentRegex.find(text)?.let { m ->
            val base = m.groupValues[1].toDoubleOrNull() ?: return null
            val pct = m.groupValues[2].toDoubleOrNull() ?: return null
            val ans = (base * pct) / 100.0
            return "${formatNum(base)} ka ${formatNum(pct)}% = ${formatNum(ans)}"
        }

        unitRegex.find(text)?.let { m ->
            val v = m.groupValues[1].toDoubleOrNull() ?: return null
            val from = m.groupValues[2].lowercase(Locale.ROOT)
            val to = m.groupValues[3].lowercase(Locale.ROOT)
            val converted = when {
                from == "km" && to.startsWith("m") -> "${formatNum(v)} km = ${formatNum(v * 1000.0)} meters"
                from == "m" && to == "km" -> "${formatNum(v)} m = ${formatNum(v / 1000.0)} km"
                from == "kg" && to.startsWith("g") -> "${formatNum(v)} kg = ${formatNum(v * 1000.0)} grams"
                from == "g" && to == "kg" -> "${formatNum(v)} g = ${formatNum(v / 1000.0)} kg"
                from.startsWith("c") && to.startsWith("f") -> "${formatNum(v)}°C = ${formatNum(v * 9.0 / 5.0 + 32.0)}°F"
                from.startsWith("f") && to.startsWith("c") -> "${formatNum(v)}°F = ${formatNum((v - 32.0) * 5.0 / 9.0)}°C"
                else -> null
            }
            if (converted != null) return converted
        }

        mathRegex.find(text)?.let { m ->
            val a = m.groupValues[1].toDoubleOrNull() ?: return null
            val op = m.groupValues[2].lowercase(Locale.ROOT)
            val b = m.groupValues[3].toDoubleOrNull() ?: return null
            val res = when (op) {
                "+", "plus", "jod" -> a + b
                "-", "minus", "ghata" -> a - b
                "*", "x", "into", "guna" -> a * b
                "/", "divide", "bhag" -> if (b == 0.0) return "Zero se divide nahi ho sakta." else a / b
                else -> return null
            }
            return "Answer hai ${formatNum(res)}"
        }

        return null
    }

    private fun formatNum(v: Double): String {
        return if (v % 1.0 == 0.0) v.roundToInt().toString()
        else String.format(Locale.US, "%.2f", v)
    }
}
