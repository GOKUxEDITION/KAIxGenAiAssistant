package com.kaixgen.assistant.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityHardeningTest {

    @Test
    fun antiReplayChallenge_verifiesWithinWindow_andExpiresAfterTtl() {
        var now = 10_000L
        val engine = AntiReplayChallengeEngine(clockMs = { now }, challengeTtlMs = 12_000L)
        val token = engine.issueChallenge()

        assertEquals(
            AntiReplayChallengeEngine.VerificationOutcome.Verified,
            engine.verifyResponse(token, "Hey KAI unlock ${token.word}")
        )
        assertEquals(
            AntiReplayChallengeEngine.VerificationOutcome.Mismatch,
            engine.verifyResponse(token, "wrong phrase")
        )

        now += 15_000L
        assertEquals(
            AntiReplayChallengeEngine.VerificationOutcome.Expired,
            engine.verifyResponse(token, token.word)
        )
    }

    @Test
    fun piiScrubber_redactsNumericCodes() {
        val raw = "Session token 482910 verified"
        val scrubbed = ScreenSecurityHelper.scrubPii(raw)
        assertFalse(scrubbed.contains("482910"))
        assertTrue(scrubbed.contains("[REDACTED_NUM]"))
    }
}
