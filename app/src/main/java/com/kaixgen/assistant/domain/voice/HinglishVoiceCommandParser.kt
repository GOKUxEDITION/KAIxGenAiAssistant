package com.kaixgen.assistant.domain.voice

import com.kaixgen.assistant.domain.model.CommandRiskLevel
import java.util.Locale
import kotlin.math.min

/**
 * Deterministic Fast-Path On-Device Hinglish / Hindi / English Voice Command Parser.
 * Meets PRD 05 §8 budget (< 50ms) and implements PRD 11 Command Catalog + PRD 15 §5 Risk Policy.
 */
class HinglishVoiceCommandParser {

    fun parse(
        rawUtterance: String,
        customLockPhrase: String = "",
        aliasResolver: (String) -> String? = { null }
    ): ParsedVoiceCommand {
        val startNs = System.nanoTime()
        val normalized = normalizeUtterance(rawUtterance)

        val result = matchIntent(
            rawText = rawUtterance.trim(),
            normalized = normalized,
            customLockPhrase = normalizeUtterance(customLockPhrase),
            aliasResolver = aliasResolver
        )
        val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
        return result.copy(latencyMs = elapsedMs)
    }

    /**
     * Normalizes Hindi Devanagari scripts, strips wake prefixes ("Hey KAI", "Hey Buddy")
     * and polite Hinglish fillers ("please", "zara", "yaar", "na") so grammar matching stays fast.
     */
    fun normalizeUtterance(input: String): String {
        var text = input.trim().lowercase(Locale.ROOT)
        DEVANAGARI_MAP.forEach { (hindi, latin) ->
            if (text.contains(hindi)) {
                text = text.replace(hindi, latin)
            }
        }
        text = text
            .replace("\u201C", "")
            .replace("\u201D", "")
            .replace("\"", "")
            .replace(Regex("^(hey\\s+kai|ok\\s+kai|hey\\s+buddy|ok\\s+buddy|kai|buddy)[,\\s]*"), "")
            .replace(Regex("\\b(please|plz|zara|jara|yaar|bhai|ji|na)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return text
    }

    private fun matchIntent(
        rawText: String,
        normalized: String,
        customLockPhrase: String,
        aliasResolver: (String) -> String?
    ): ParsedVoiceCommand {
        if (normalized.isBlank()) {
            return buildCommand(rawText, normalized, VoiceIntentType.CLARIFY, emptyMap(), 0.25f)
        }

        // 1. User-defined Lock Phrase match (phonetic / fuzzy tolerance)
        if (customLockPhrase.length >= 3 &&
            (normalized == customLockPhrase ||
                normalized.contains(customLockPhrase) ||
                similarity(normalized, customLockPhrase) >= 0.82f)
        ) {
            return buildCommand(
                rawText,
                normalized,
                VoiceIntentType.LOCK_PHONE,
                mapOf("trigger" to "custom_phrase"),
                0.96f
            )
        }

        // 2. Meta / Confirmation / Cancel / Help
        if (normalized in setOf("cancel", "rehne do", "mat karo", "chhodo")) {
            return buildCommand(rawText, normalized, VoiceIntentType.CANCEL, emptyMap(), 0.98f)
        }
        if (normalized in setOf("yes", "haan", "ha", "confirm", "send", "kar do", "bhej do")) {
            return buildCommand(rawText, normalized, VoiceIntentType.CONFIRM, emptyMap(), 0.98f)
        }
        if (normalized.contains("what can you do") || normalized.contains("kya kar sakte") || normalized == "help") {
            return buildCommand(rawText, normalized, VoiceIntentType.HELP, emptyMap(), 0.96f)
        }

        // 3. Find Phone & Stop Ringing (PRD 11 §4)
        if (normalized.contains("where are you") ||
            normalized.contains("phone kahan") ||
            normalized.contains("find my phone") ||
            normalized.contains("find phone") ||
            normalized.contains("mera phone dhundo")
        ) {
            return buildCommand(rawText, normalized, VoiceIntentType.FIND_PHONE, emptyMap(), 0.97f)
        }
        if (normalized in setOf("stop", "bas", "ruk jao", "band karo", "stop ringing")) {
            return buildCommand(rawText, normalized, VoiceIntentType.STOP_RING, emptyMap(), 0.94f)
        }

        // 4. Routines (PRD 11 §8)
        when {
            normalized.contains("good morning") || normalized.contains("suprabhat") ->
                return buildCommand(rawText, normalized, VoiceIntentType.RUN_ROUTINE, mapOf("routine" to "Good Morning"), 0.95f)
            normalized.contains("driving mode") || normalized.contains("drive mode") ->
                return buildCommand(rawText, normalized, VoiceIntentType.RUN_ROUTINE, mapOf("routine" to "Driving Mode"), 0.95f)
            normalized.contains("sleep mode") || normalized.contains("good night") ->
                return buildCommand(rawText, normalized, VoiceIntentType.RUN_ROUTINE, mapOf("routine" to "Sleep Mode"), 0.95f)
        }

        // 5. Device Controls: Torch / Flashlight (PRD 11 §5)
        if (normalized.contains("torch") || normalized.contains("flashlight") || normalized.contains("flash light")) {
            val turnOff = normalized.contains("off") || normalized.contains("band") || normalized.contains("close")
            val state = if (turnOff) "off" else "on"
            return buildCommand(rawText, normalized, VoiceIntentType.TOGGLE_TORCH, mapOf("state" to state), 0.96f)
        }

        // 6. Battery & Time/Date (PRD 11 §5 & §6)
        if (normalized.contains("battery") || normalized.contains("charge kitna")) {
            return buildCommand(rawText, normalized, VoiceIntentType.BATTERY_STATUS, emptyMap(), 0.96f)
        }
        if (normalized.contains("what's the time") ||
            normalized.contains("what time") ||
            normalized.contains("kitne baje") ||
            normalized.contains("samay kya") ||
            normalized.contains("time kya") ||
            normalized.contains("aaj ki tarikh") ||
            normalized.contains("what is the date")
        ) {
            return buildCommand(rawText, normalized, VoiceIntentType.TIME_DATE, emptyMap(), 0.96f)
        }

        // 7. Volume Control (PRD 11 §3)
        if (normalized.contains("volume") || normalized.contains("awaaz") || normalized.contains("awaz") || normalized.contains("sound")) {
            val pctMatch = Regex("(\\d{1,3})\\s*(percent|%)?").find(normalized)
            val slots = when {
                pctMatch != null -> mapOf("level" to pctMatch.groupValues[1].toInt().coerceIn(0, 100).toString())
                normalized.contains("mute") || normalized.contains("silent") || normalized.contains("band") -> mapOf("action" to "mute")
                normalized.contains("down") || normalized.contains("kam") || normalized.contains("low") || normalized.contains("decrease") -> mapOf("action" to "down")
                else -> mapOf("action" to "up")
            }
            return buildCommand(rawText, normalized, VoiceIntentType.VOLUME_CONTROL, slots, 0.94f)
        }

        // 8. Alarm & Timer (PRD 11 §6)
        if (normalized.contains("timer")) {
            val mins = Regex("(\\d+)\\s*(min|minute|minutes|sec|second)").find(normalized)
            val amount = mins?.groupValues?.getOrNull(1) ?: "5"
            val unit = if (mins?.groupValues?.getOrNull(2)?.startsWith("sec") == true) "seconds" else "minutes"
            return buildCommand(
                rawText,
                normalized,
                VoiceIntentType.SET_TIMER,
                mapOf("duration" to amount, "unit" to unit),
                0.93f
            )
        }
        if (normalized.contains("alarm") || normalized.contains("wake me")) {
            val timeMatch = Regex("(\\d{1,2})(:(\\d{2}))?").find(normalized)
            val hour = timeMatch?.groupValues?.getOrNull(1) ?: "7"
            val minute = timeMatch?.groupValues?.getOrNull(3)?.ifBlank { "00" } ?: "00"
            return buildCommand(
                rawText,
                normalized,
                VoiceIntentType.SET_ALARM,
                mapOf("hour" to hour, "minute" to minute),
                0.92f
            )
        }

        // 9. Lock Phone / Lock Specific App (PRD 11 §4)
        if (normalized.startsWith("lock ") || normalized.endsWith(" lock karo") || normalized.endsWith(" lock kar do")) {
            val target = normalized
                .removePrefix("lock ")
                .removeSuffix(" lock karo")
                .removeSuffix(" lock kar do")
                .trim()
            return if (target.isBlank() || target in setOf("phone", "screen", "my phone", "device")) {
                buildCommand(rawText, normalized, VoiceIntentType.LOCK_PHONE, mapOf("trigger" to "voice_command"), 0.93f)
            } else {
                val resolvedApp = aliasResolver(target) ?: target.replaceFirstChar { it.uppercaseChar() }
                buildCommand(rawText, normalized, VoiceIntentType.LOCK_APP, mapOf("app" to resolvedApp), 0.92f)
            }
        }

        // 10. Messaging (HIGH RISK - always confirms per PRD 15 §5)
        if (normalized.contains("message") || normalized.contains("msg") ||
            (normalized.contains("whatsapp") && (normalized.contains("bolo") || normalized.contains("bhej") || normalized.contains("send")))
        ) {
            val app = if (normalized.contains("sms")) "SMS" else "WhatsApp"
            val recipientMatch = Regex("^(?:message|msg|send message to)\\s+([a-z]+)").find(normalized)
                ?: Regex("^([a-z]+)\\s+ko\\s+(?:whatsapp|message|msg)").find(normalized)
            val recipient = recipientMatch?.groupValues?.getOrNull(1)?.replaceFirstChar { it.uppercaseChar() } ?: "Contact"
            val body = normalized
                .replace(Regex("^(?:message|msg|send message to)\\s+[a-z]+\\s*"), "")
                .replace(Regex("^[a-z]+\\s+ko\\s+(?:whatsapp|message|msg)\\s*(?:karo|bhejo|kar do)?\\s*"), "")
                .trim()
                .ifBlank { "I will reach shortly" }
            return buildCommand(
                rawText,
                normalized,
                VoiceIntentType.SEND_MESSAGE,
                mapOf("app" to app, "recipient" to recipient, "text" to body),
                0.90f
            )
        }

        // 11. Calls: Answer / Reject / Call Number / Call Contact (PRD 11 §2)
        if (normalized.contains("answer") || normalized.contains("utha lo") || normalized.contains("pick up")) {
            return buildCommand(rawText, normalized, VoiceIntentType.ANSWER_CALL, emptyMap(), 0.94f)
        }
        if (normalized.contains("reject") || normalized.contains("kaat do") || normalized.contains("cut call")) {
            return buildCommand(rawText, normalized, VoiceIntentType.REJECT_CALL, emptyMap(), 0.94f)
        }
        if (normalized.contains("call") || normalized.contains("phone lagao") || normalized.contains("dial")) {
            val digits = normalized.filter { it.isDigit() }
            if (digits.length >= 6) {
                return buildCommand(rawText, normalized, VoiceIntentType.CALL_NUMBER, mapOf("number" to digits), 0.95f)
            }
            val contact = normalized
                .replace(Regex("\\b(call|dial|phone|lagao|karo|kar do|ko|to|pe)\\b"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .replaceFirstChar { it.uppercaseChar() }
                .ifBlank { "Contact" }
            return buildCommand(rawText, normalized, VoiceIntentType.CALL_CONTACT, mapOf("contact" to contact), 0.91f)
        }

        // 12. Media Playback (PRD 11 §3)
        if (normalized.contains("play") || normalized.contains("bajao") || normalized.contains("gaane") || normalized.contains("song") || normalized.contains("music")) {
            val query = normalized
                .replace(Regex("\\b(play|bajao|chalao|sunao|ke|ka|ki|songs?|gaane|music)\\b"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .ifBlank { "Trending Hindi Songs" }
            return buildCommand(rawText, normalized, VoiceIntentType.PLAY_MEDIA, mapOf("query" to query), 0.93f)
        }
        if (normalized.contains("next song") || normalized.contains("agla gaana") || normalized.contains("pause")) {
            val cmd = if (normalized.contains("pause")) "pause" else "next"
            return buildCommand(rawText, normalized, VoiceIntentType.MEDIA_CONTROL, mapOf("command" to cmd), 0.92f)
        }

        // 13. Open Settings Panel (PRD 11 §1)
        if (normalized.contains("settings") && (normalized.contains("wifi") || normalized.contains("wi-fi") || normalized.contains("bluetooth") || normalized.contains("open") || normalized.contains("kholo"))) {
            val page = when {
                normalized.contains("wifi") || normalized.contains("wi-fi") -> "wifi"
                normalized.contains("bluetooth") -> "bluetooth"
                else -> "main"
            }
            return buildCommand(rawText, normalized, VoiceIntentType.OPEN_SETTINGS, mapOf("page" to page), 0.95f)
        }

        // 14. Web Search (PRD 11 §1)
        if (normalized.startsWith("search") || normalized.endsWith("search karo") || normalized.contains("google pe")) {
            val query = normalized
                .replace(Regex("\\b(search|for|karo|kar do|google|pe|on)\\b"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (query.isNotBlank()) {
                return buildCommand(rawText, normalized, VoiceIntentType.SEARCH_WEB, mapOf("query" to query), 0.92f)
            }
        }

        // 15. Open App (PRD 11 §1: "Open WhatsApp", "WhatsApp kholo", "Instagram chalao")
        if (normalized.startsWith("open ") ||
            normalized.startsWith("launch ") ||
            normalized.endsWith(" kholo") ||
            normalized.endsWith(" open karo") ||
            normalized.endsWith(" chalao")
        ) {
            val rawApp = normalized
                .removePrefix("open ")
                .removePrefix("launch ")
                .removeSuffix(" open karo")
                .removeSuffix(" kholo")
                .removeSuffix(" chalao")
                .trim()
            val resolved = aliasResolver(rawApp) ?: rawApp.replaceFirstChar { it.uppercaseChar() }
            return buildCommand(rawText, normalized, VoiceIntentType.OPEN_APP, mapOf("app" to resolved), 0.96f)
        }

        // 16. Fallback Clarify
        return buildCommand(rawText, normalized, VoiceIntentType.CLARIFY, emptyMap(), 0.45f)
    }

    private fun buildCommand(
        rawText: String,
        normalized: String,
        intent: VoiceIntentType,
        slots: Map<String, String>,
        confidence: Float
    ): ParsedVoiceCommand {
        val risk = when (intent) {
            VoiceIntentType.SEND_MESSAGE,
            VoiceIntentType.CALL_NUMBER -> CommandRiskLevel.HIGH

            VoiceIntentType.CALL_CONTACT,
            VoiceIntentType.LOCK_PHONE,
            VoiceIntentType.LOCK_APP -> CommandRiskLevel.MEDIUM

            else -> CommandRiskLevel.LOW
        }
        val needsConfirm = when (risk) {
            CommandRiskLevel.HIGH -> true
            CommandRiskLevel.MEDIUM -> intent == VoiceIntentType.CALL_CONTACT || confidence < 0.85f
            CommandRiskLevel.LOW -> false
        }
        return ParsedVoiceCommand(
            rawText = rawText,
            normalizedText = normalized,
            intent = intent,
            slots = slots,
            confidence = confidence,
            riskLevel = risk,
            requiresConfirmation = needsConfirm,
            latencyMs = 1L
        )
    }

    fun similarity(a: String, b: String): Float {
        if (a.isEmpty() || b.isEmpty()) return 0f
        if (a == b) return 1f
        val dist = levenshteinDistance(a, b)
        val maxLen = maxOf(a.length, b.length)
        return (1f - dist.toFloat() / maxLen.toFloat()).coerceIn(0f, 1f)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val prev = IntArray(s2.length + 1) { it }
        val curr = IntArray(s2.length + 1)
        for (i in 1..s1.length) {
            curr[0] = i
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                curr[j] = min(min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
            }
            System.arraycopy(curr, 0, prev, 0, prev.size)
        }
        return prev[s2.length]
    }

    companion object {
        private val DEVANAGARI_MAP = listOf(
            "व्हाट्सऐप" to "whatsapp",
            "वॉट्सएप" to "whatsapp",
            "इंस्टाग्राम" to "instagram",
            "यूट्यूब" to "youtube",
            "कैमरा" to "camera",
            "सेटिंग्स" to "settings",
            "खोलो" to "kholo",
            "कॉल" to "call",
            "मम्मी" to "mummy",
            "पापा" to "papa",
            "गाने" to "gaane",
            "बजाओ" to "bajao",
            "टॉर्च" to "torch",
            "बैटरी" to "battery",
            "फ़ोन कहाँ" to "phone kahan",
            "फोन कहां" to "phone kahan",
            "आवाज़" to "awaaz",
            "कम करो" to "kam karo",
            "बढ़ाओ" to "up",
            "बंद करो" to "band karo",
            "चालू करो" to "on",
            "लॉक करो" to "lock karo",
            "मैसेज" to "message",
            "अलार्म" to "alarm",
            "टाइमर" to "timer",
            "कितने बजे" to "kitne baje",
            "हाँ" to "haan",
            "हां" to "haan"
        )
    }
}
