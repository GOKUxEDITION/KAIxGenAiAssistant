package com.kaixgen.assistant.security

import android.app.KeyguardManager
import android.content.Context
import android.provider.Settings
import com.kaixgen.assistant.service.KaiAccessibilityActionService
import java.security.MessageDigest
import java.util.Locale

data class SecurityThreatReport(
    val score: Int,
    val isOsLockSecure: Boolean,
    val isUsbDebuggingOff: Boolean,
    val isNotRooted: Boolean,
    val isAccessibilityLockReady: Boolean,
    val hasDecoyPinConfigured: Boolean,
    val hasPanicPhraseConfigured: Boolean
)

sealed interface PinCheckOutcome {
    data object DecoyTriggered : PinCheckOutcome
    data object StandardFlow : PinCheckOutcome
}

/**
 * Hyper-Security Shield Engine (PRD F-SE-08 Decoy PIN, F-SE-17 Panic Phrase, PRD 06 §3.3 Hardening):
 * - Scans real Android security posture (Keyguard, ADB/USB Debugging, Root heuristic, Accessibility Lock)
 * - Stores SHA-256 salted Decoy PIN separately from primary PinVault
 * - Matches Panic Lock Phrase for instant screen lock + strict lockdown
 */
class HyperSecurityShieldEngine(private val context: Context? = null) {

    private val prefs = context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val antiReplay = AntiReplayChallengeEngine()

    private var decoyPinHash: String? = prefs?.getString(KEY_DECOY_HASH, null)
    private var panicPhrase: String = prefs?.getString(KEY_PANIC_PHRASE, DEFAULT_PANIC_PHRASE) ?: DEFAULT_PANIC_PHRASE

    fun setDecoyPin(pin: String): Boolean {
        val clean = pin.trim()
        if (clean.length !in 4..6 || !clean.all { it.isDigit() }) return false
        val hashed = sha256("kai_decoy_v1:$clean")
        decoyPinHash = hashed
        prefs?.edit()?.putString(KEY_DECOY_HASH, hashed)?.apply()
        return true
    }

    fun clearDecoyPin() {
        decoyPinHash = null
        prefs?.edit()?.remove(KEY_DECOY_HASH)?.apply()
    }

    fun checkIfDecoyPin(enteredPin: String): PinCheckOutcome {
        val stored = decoyPinHash ?: return PinCheckOutcome.StandardFlow
        val candidate = sha256("kai_decoy_v1:${enteredPin.trim()}")
        return if (MessageDigest.isEqual(stored.toByteArray(), candidate.toByteArray())) {
            PinCheckOutcome.DecoyTriggered
        } else {
            PinCheckOutcome.StandardFlow
        }
    }

    fun setPanicPhrase(phrase: String): Boolean {
        val clean = phrase.trim()
        if (clean.length < 3) return false
        panicPhrase = clean
        prefs?.edit()?.putString(KEY_PANIC_PHRASE, clean)?.apply()
        return true
    }

    fun getPanicPhrase(): String = panicPhrase

    fun isPanicPhraseTriggered(spokenText: String): Boolean {
        val normSpoken = spokenText.lowercase(Locale.ROOT).trim()
        val normTarget = panicPhrase.lowercase(Locale.ROOT).trim()
        return normTarget.length >= 3 && normSpoken.contains(normTarget)
    }

    fun runDeviceSecurityAudit(): SecurityThreatReport {
        val ctx = context?.applicationContext
        val osSecure = runCatching {
            val km = ctx?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.isDeviceSecure == true
        }.getOrDefault(false)

        val adbOff = runCatching {
            if (ctx == null) true
            else Settings.Global.getInt(ctx.contentResolver, Settings.Global.ADB_ENABLED, 0) == 0
        }.getOrDefault(true)

        val notRooted = !antiReplay.isDeviceRootedHeuristic()
        val a11yReady = KaiAccessibilityActionService.isConnected()
        val hasDecoy = !decoyPinHash.isNullOrBlank()
        val hasPanic = panicPhrase.length >= 3

        val checks = listOf(osSecure, adbOff, notRooted, a11yReady, hasDecoy, hasPanic)
        val score = (checks.count { it } * 100) / checks.size

        return SecurityThreatReport(
            score = score,
            isOsLockSecure = osSecure,
            isUsbDebuggingOff = adbOff,
            isNotRooted = notRooted,
            isAccessibilityLockReady = a11yReady,
            hasDecoyPinConfigured = hasDecoy,
            hasPanicPhraseConfigured = hasPanic
        )
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "kai_hyper_security_prefs"
        private const val KEY_DECOY_HASH = "decoy_pin_sha256"
        private const val KEY_PANIC_PHRASE = "panic_lock_phrase"
        const val DEFAULT_PANIC_PHRASE = "emergency lock"
    }
}
