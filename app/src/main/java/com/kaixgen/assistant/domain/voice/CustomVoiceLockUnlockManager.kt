package com.kaixgen.assistant.domain.voice

import android.content.Context
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

sealed interface CustomLockCommand {
    data class LockNow(val matchedPhrase: String) : CustomLockCommand
    data class UnlockNow(val matchedPhrase: String) : CustomLockCommand
    data object None : CustomLockCommand
}

/**
 * Manages user-defined custom Lock Words and Unlock Words (Hinglish/Hindi/English)
 * with fuzzy matching so users can add any custom phrase to lock or unlock their phone.
 */
class CustomVoiceLockUnlockManager(context: Context? = null) {

    private val prefs = context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var lockPhrases: MutableList<String> = loadList(KEY_LOCK_PHRASES, DEFAULT_LOCK_PHRASES)
    private var unlockPhrases: MutableList<String> = loadList(KEY_UNLOCK_PHRASES, DEFAULT_UNLOCK_PHRASES)

    fun getLockPhrases(): List<String> = lockPhrases.toList()
    fun getUnlockPhrases(): List<String> = unlockPhrases.toList()

    fun addLockPhrase(phrase: String): Boolean {
        val clean = normalize(phrase)
        if (clean.length < 2 || lockPhrases.any { normalize(it) == clean }) return false
        lockPhrases.add(phrase.trim())
        saveList(KEY_LOCK_PHRASES, lockPhrases)
        return true
    }

    fun removeLockPhrase(phrase: String) {
        lockPhrases.removeAll { normalize(it) == normalize(phrase) }
        if (lockPhrases.isEmpty()) lockPhrases.addAll(DEFAULT_LOCK_PHRASES)
        saveList(KEY_LOCK_PHRASES, lockPhrases)
    }

    fun addUnlockPhrase(phrase: String): Boolean {
        val clean = normalize(phrase)
        if (clean.length < 2 || unlockPhrases.any { normalize(it) == clean }) return false
        unlockPhrases.add(phrase.trim())
        saveList(KEY_UNLOCK_PHRASES, unlockPhrases)
        return true
    }

    fun removeUnlockPhrase(phrase: String) {
        unlockPhrases.removeAll { normalize(it) == normalize(phrase) }
        if (unlockPhrases.isEmpty()) unlockPhrases.addAll(DEFAULT_UNLOCK_PHRASES)
        saveList(KEY_UNLOCK_PHRASES, unlockPhrases)
    }

    fun matchUtterance(spokenText: String, threshold: Float = 0.80f): CustomLockCommand {
        val clean = normalize(spokenText)
        if (clean.isBlank()) return CustomLockCommand.None

        // Check Unlock phrases first when matching unlock words
        for (phrase in unlockPhrases) {
            val target = normalize(phrase)
            if (clean == target || clean.contains(target) || similarity(clean, target) >= threshold) {
                return CustomLockCommand.UnlockNow(phrase)
            }
        }

        for (phrase in lockPhrases) {
            val target = normalize(phrase)
            if (clean == target || clean.contains(target) || similarity(clean, target) >= threshold) {
                return CustomLockCommand.LockNow(phrase)
            }
        }

        return CustomLockCommand.None
    }

    private fun normalize(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\u0900-\\u097F\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun similarity(a: String, b: String): Float {
        if (a.isEmpty() && b.isEmpty()) return 1f
        val maxLen = max(a.length, b.length)
        if (maxLen == 0) return 1f
        val dist = levenshtein(a, b)
        return 1f - (dist.toFloat() / maxLen.toFloat())
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return dp[b.length]
    }

    private fun loadList(key: String, defaults: List<String>): MutableList<String> {
        val stored = prefs?.getStringSet(key, null)
        return if (stored.isNullOrEmpty()) defaults.toMutableList() else stored.toMutableList()
    }

    private fun saveList(key: String, list: List<String>) {
        prefs?.edit()?.putStringSet(key, list.toSet())?.apply()
    }

    companion object {
        private const val PREFS_NAME = "kai_custom_voice_lock_prefs"
        private const val KEY_LOCK_PHRASES = "custom_lock_phrases"
        private const val KEY_UNLOCK_PHRASES = "custom_unlock_phrases"
        val DEFAULT_LOCK_PHRASES = listOf("kai lock", "phone lock karo", "so ja phone")
        val DEFAULT_UNLOCK_PHRASES = listOf("khul ja sim sim", "kai unlock", "phone kholo")
    }
}
