package com.kaixgen.assistant.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HyperSecurityShieldTest {

    @Test
    fun decoyPin_triggersDecoyOutcomeOnlyOnExactMatch() {
        val engine = HyperSecurityShieldEngine(context = null)
        assertTrue(engine.setDecoyPin("2580"))
        assertEquals(PinCheckOutcome.DecoyTriggered, engine.checkIfDecoyPin("2580"))
        assertEquals(PinCheckOutcome.StandardFlow, engine.checkIfDecoyPin("1234"))
    }

    @Test
    fun panicPhrase_matchesCustomLockdownPhrase() {
        val engine = HyperSecurityShieldEngine(context = null)
        assertTrue(engine.setPanicPhrase("red alert now"))
        assertTrue(engine.isPanicPhraseTriggered("Hey KAI red alert now"))
        assertFalse(engine.isPanicPhraseTriggered("open whatsapp"))
    }
}
