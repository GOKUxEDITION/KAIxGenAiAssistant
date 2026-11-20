package com.kaixgen.assistant.security

import com.kaixgen.assistant.data.local.db.PinRecordDao
import com.kaixgen.assistant.data.local.db.PinRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

sealed interface PinVerificationResult {
    data object Success : PinVerificationResult
    data object NotConfigured : PinVerificationResult
    data class WrongPin(val failedCount: Int, val attemptsBeforeCooldown: Int) : PinVerificationResult
    data class Cooldown(val remainingSeconds: Long) : PinVerificationResult
}

/**
 * Hardened on-device PIN Vault implementing PRD 05 §4.4 & PRD 06 §3.1:
 * - Random 16-byte per-PIN salt
 * - Keystore-bound HMAC pepper
 * - PBKDF2WithHmacSHA256 key stretching
 * - Constant-time digest comparison
 * - Persisted exponential lockout cooldown (5 failures -> 30s, doubling up to 15 min)
 */
class PinVault(
    private val pinRecordDao: PinRecordDao,
    private val pepperProvider: DevicePepperProvider = KeystorePepperProvider(),
    private val clockMs: () -> Long = { System.currentTimeMillis() }
) {
    private val secureRandom = SecureRandom()

    suspend fun setPin(pin: String) = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val hash = derivePinHash(pin.toCharArray(), salt, PBKDF2_ITERATIONS)
        val record = PinRecordEntity(
            id = 1,
            hashHex = hash.toHex(),
            saltHex = salt.toHex(),
            iterations = PBKDF2_ITERATIONS,
            pinLength = pin.length,
            failedCount = 0,
            cooldownUntilMs = 0L,
            changedAt = clockMs()
        )
        pinRecordDao.upsert(record)
    }

    suspend fun verifyPin(pin: String): PinVerificationResult = withContext(Dispatchers.Default) {
        val record = pinRecordDao.getPinRecordOnce() ?: return@withContext PinVerificationResult.NotConfigured
        val now = clockMs()

        if (record.cooldownUntilMs > now) {
            val remainingSec = ((record.cooldownUntilMs - now) / 1000L).coerceAtLeast(1L)
            return@withContext PinVerificationResult.Cooldown(remainingSec)
        }

        val salt = record.saltHex.hexToBytes()
        val expectedHash = record.hashHex.hexToBytes()
        val candidateHash = derivePinHash(pin.toCharArray(), salt, record.iterations)

        val matches = MessageDigest.isEqual(expectedHash, candidateHash)
        if (matches) {
            pinRecordDao.updateAttemptState(failedCount = 0, cooldownUntilMs = 0L)
            PinVerificationResult.Success
        } else {
            val nextFailures = record.failedCount + 1
            if (nextFailures >= MAX_ATTEMPTS_BEFORE_COOLDOWN) {
                val multiplier = 1L shl (nextFailures - MAX_ATTEMPTS_BEFORE_COOLDOWN).coerceAtMost(5)
                val cooldownSeconds = (BASE_COOLDOWN_SECONDS * multiplier).coerceAtMost(MAX_COOLDOWN_SECONDS)
                val cooldownUntil = now + cooldownSeconds * 1000L
                pinRecordDao.updateAttemptState(failedCount = nextFailures, cooldownUntilMs = cooldownUntil)
                PinVerificationResult.Cooldown(cooldownSeconds)
            } else {
                pinRecordDao.updateAttemptState(failedCount = nextFailures, cooldownUntilMs = 0L)
                PinVerificationResult.WrongPin(
                    failedCount = nextFailures,
                    attemptsBeforeCooldown = MAX_ATTEMPTS_BEFORE_COOLDOWN - nextFailures
                )
            }
        }
    }

    private fun derivePinHash(pinChars: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val peppered = pepperProvider.applyPepper(pinChars, salt)
        pinChars.fill('\u0000')
        val spec = PBEKeySpec(peppered, salt, iterations, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded
        } finally {
            peppered.fill('\u0000')
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        val len = length
        val out = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            out[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return out
    }

    companion object {
        private const val SALT_LENGTH_BYTES = 16
        private const val KEY_LENGTH_BITS = 256
        private const val PBKDF2_ITERATIONS = 120_000
        private const val MAX_ATTEMPTS_BEFORE_COOLDOWN = 5
        private const val BASE_COOLDOWN_SECONDS = 30L
        private const val MAX_COOLDOWN_SECONDS = 900L // 15 minutes max (PRD 03 §3.3)
    }
}
