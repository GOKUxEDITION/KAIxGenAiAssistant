package com.kaixgen.assistant.security

import com.kaixgen.assistant.data.local.db.PinRecordDao
import com.kaixgen.assistant.data.local.db.PinRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinVaultTest {

    private class FakePinRecordDao : PinRecordDao {
        private val flow = MutableStateFlow<PinRecordEntity?>(null)
        override fun observePinRecord(): Flow<PinRecordEntity?> = flow
        override suspend fun getPinRecordOnce(): PinRecordEntity? = flow.value
        override suspend fun upsert(record: PinRecordEntity) {
            flow.value = record
        }
        override suspend fun updateAttemptState(failedCount: Int, cooldownUntilMs: Long) {
            flow.value = flow.value?.copy(failedCount = failedCount, cooldownUntilMs = cooldownUntilMs)
        }
        override suspend fun clearAll() {
            flow.value = null
        }
    }

    private val testPepper = object : DevicePepperProvider {
        override fun applyPepper(rawPin: CharArray, salt: ByteArray): CharArray {
            return ("test_pepper:" + String(rawPin)).toCharArray()
        }
    }

    @Test
    fun verifiesValidPinAndTriggersExponentialCooldownAfterFiveFailures() = runTest {
        var fakeNow = 100_000L
        val dao = FakePinRecordDao()
        val vault = PinVault(pinRecordDao = dao, pepperProvider = testPepper, clockMs = { fakeNow })

        vault.setPin("4829")
        assertEquals(PinVerificationResult.Success, vault.verifyPin("4829"))

        // 4 failed attempts decrement remaining attempts before cooldown
        for (attempt in 1..4) {
            val res = vault.verifyPin("0000")
            assertTrue(res is PinVerificationResult.WrongPin)
            assertEquals(5 - attempt, (res as PinVerificationResult.WrongPin).attemptsBeforeCooldown)
        }

        // 5th failure activates 30s cooldown
        val fifth = vault.verifyPin("0000")
        assertTrue(fifth is PinVerificationResult.Cooldown)
        assertEquals(30L, (fifth as PinVerificationResult.Cooldown).remainingSeconds)

        // Even correct PIN is blocked during active cooldown
        val blocked = vault.verifyPin("4829")
        assertTrue(blocked is PinVerificationResult.Cooldown)

        // After cooldown expires, correct PIN succeeds and resets failure count
        fakeNow += 31_000L
        assertEquals(PinVerificationResult.Success, vault.verifyPin("4829"))
        assertEquals(0, dao.getPinRecordOnce()?.failedCount)
    }
}
