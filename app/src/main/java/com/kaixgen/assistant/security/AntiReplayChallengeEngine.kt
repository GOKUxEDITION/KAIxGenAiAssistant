package com.kaixgen.assistant.security

import java.io.File
import java.security.SecureRandom
import java.util.Locale

class AntiReplayChallengeEngine(
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    private val challengeTtlMs: Long = 12_000L
) {
    private val secureRandom = SecureRandom()

    data class ChallengeToken(
        val word: String,
        val issuedAtMs: Long,
        val expiresAtMs: Long
    )

    sealed interface VerificationOutcome {
        data object Verified : VerificationOutcome
        data object Expired : VerificationOutcome
        data object Mismatch : VerificationOutcome
    }

    fun issueChallenge(): ChallengeToken {
        val now = clockMs()
        val word = CHALLENGE_WORDS[secureRandom.nextInt(CHALLENGE_WORDS.size)]
        return ChallengeToken(
            word = word,
            issuedAtMs = now,
            expiresAtMs = now + challengeTtlMs
        )
    }

    fun verifyResponse(token: ChallengeToken, spokenTranscript: String): VerificationOutcome {
        if (clockMs() > token.expiresAtMs) return VerificationOutcome.Expired
        val normalized = spokenTranscript.lowercase(Locale.ROOT).trim()
        return if (normalized.contains(token.word.lowercase(Locale.ROOT))) {
            VerificationOutcome.Verified
        } else {
            VerificationOutcome.Mismatch
        }
    }

    fun isDeviceRootedHeuristic(): Boolean {
        val suPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su"
        )
        return suPaths.any { path -> runCatching { File(path).exists() }.getOrDefault(false) }
    }

    companion object {
        private val CHALLENGE_WORDS = listOf(
            "surya", "kamal", "neela", "shakti", "sagar", "tara",
            "lotus", "river", "amber", "spark", "horizon", "silver"
        )
    }
}
