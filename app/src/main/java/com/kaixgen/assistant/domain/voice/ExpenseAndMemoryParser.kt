package com.kaixgen.assistant.domain.voice

import java.util.Locale

sealed interface AdvancedIntentResult {
    data class AddExpense(val amountRupees: Int, val category: String, val note: String) : AdvancedIntentResult
    data class RememberFact(val key: String, val value: String) : AdvancedIntentResult
    data class RecallFact(val queryKey: String) : AdvancedIntentResult
    data class TriggerSos(val phrase: String) : AdvancedIntentResult
    data class StartDrivingMode(val destination: String?) : AdvancedIntentResult
    data class StartFocusTimer(val minutes: Int) : AdvancedIntentResult
    data object None : AdvancedIntentResult
}

class ExpenseAndMemoryParser {

    private val expenseRegex = Regex(
        """(?:rs\.?|inr|₹)?\s*(\d{1,6})\s*(?:rupaye|rupees|rs|₹)?\s*(?:on|for|pe|par|ka|ki|ke|mein|me)?\s*(.+)""",
        RegexOption.IGNORE_CASE
    )
    private val focusRegex = Regex(
        """(\d{1,3})\s*(?:min|minute|minutes)\s*(?:focus|pomodoro|study)""",
        RegexOption.IGNORE_CASE
    )

    fun parse(rawUtterance: String): AdvancedIntentResult {
        val text = rawUtterance.trim()
        val lower = text.lowercase(Locale.ROOT)

        if (lower.contains("help help") || lower.contains("emergency sos") ||
            lower.contains("bachao") || lower.contains("sos bhejo")
        ) {
            return AdvancedIntentResult.TriggerSos(text)
        }

        if (lower.contains("driving mode") || lower.contains("drive mode") || lower.contains("ghar chalo")) {
            val dest = if (lower.contains("ghar chalo")) "Home" else null
            return AdvancedIntentResult.StartDrivingMode(dest)
        }

        focusRegex.find(lower)?.let { match ->
            val mins = match.groupValues[1].toIntOrNull()?.coerceIn(5, 120) ?: 25
            return AdvancedIntentResult.StartFocusTimer(mins)
        }

        val rememberPrefixes = listOf("yaad rakhna ", "remember that ", "remember ", "save memory ")
        for (prefix in rememberPrefixes) {
            if (lower.startsWith(prefix)) {
                val body = text.substring(prefix.length).trim()
                val splitIdx = body.indexOf(" is ").takeIf { it > 0 }
                    ?: body.indexOf(" hai").takeIf { it > 0 }
                return if (splitIdx != null && splitIdx > 0) {
                    val key = body.substring(0, splitIdx).trim()
                    val value = body.substring(splitIdx).removePrefix(" is ").removeSuffix(" hai").trim()
                        .ifBlank { body }
                    AdvancedIntentResult.RememberFact(key = key, value = value)
                } else {
                    AdvancedIntentResult.RememberFact(key = "Note", value = body)
                }
            }
        }

        if ((lower.contains("kya hai") || lower.startsWith("what is my ") || lower.startsWith("recall ")) &&
            (lower.contains("mera ") || lower.contains("meri ") || lower.contains("my ") || lower.startsWith("recall "))
        ) {
            val cleaned = lower
                .removePrefix("what is ")
                .removePrefix("recall ")
                .replace("kya hai", "")
                .replace("?", "")
                .trim()
            if (cleaned.isNotBlank()) {
                return AdvancedIntentResult.RecallFact(cleaned)
            }
        }

        if (lower.contains("rupaye") || lower.contains("rupees") || lower.contains("spent ") ||
            lower.contains("kharch") || lower.contains("₹")
        ) {
            val cleaned = lower.removePrefix("spent ").removePrefix("add expense ").trim()
            expenseRegex.find(cleaned)?.let { match ->
                val amount = match.groupValues[1].toIntOrNull() ?: return AdvancedIntentResult.None
                val note = match.groupValues[2].replace("kharch", "").trim().ifBlank { "General" }
                val category = inferCategory(note)
                return AdvancedIntentResult.AddExpense(amount, category, note)
            }
        }

        return AdvancedIntentResult.None
    }

    private fun inferCategory(note: String): String {
        val n = note.lowercase(Locale.ROOT)
        return when {
            listOf("chai", "coffee", "food", "khana", "lunch", "dinner", "snack").any { n.contains(it) } -> "Food"
            listOf("petrol", "diesel", "auto", "cab", "uber", "ola", "bus", "train").any { n.contains(it) } -> "Transport"
            listOf("bill", "recharge", "wifi", "electricity", "rent").any { n.contains(it) } -> "Bills"
            listOf("medicine", "doctor", "pharmacy", "dawai").any { n.contains(it) } -> "Health"
            listOf("grocery", "sabzi", "milk", "doodh", "ration").any { n.contains(it) } -> "Groceries"
            else -> "Other"
        }
    }
}
