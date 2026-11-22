package com.kaixgen.assistant.domain.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KaiAgentBrainTest {

    @Test
    fun multiStepHinglishCommand_decomposesIntoOrderedSteps() {
        val brain = KaiAgentBrain()
        val plan = brain.plan("Kal subah 7 baje alarm lagao aur battery kitni hai")
        assertEquals(2, plan.steps.size)
        assertEquals("SET_ALARM", plan.steps[0].resolvedIntent)
        assertEquals("BATTERY_STATUS", plan.steps[1].resolvedIntent)
    }

    @Test
    fun followUpAndCancel_updateDialogContextSafely() {
        val brain = KaiAgentBrain()
        brain.plan("WhatsApp kholo")
        assertTrue(brain.getContext().lastOpenedApp?.contains("WhatsApp") == true)

        val followUp = brain.plan("Isko Hindi mein samjhao")
        assertTrue(followUp.isFollowUp)
        assertEquals("EXPLAIN_OR_TRANSLATE", followUp.steps.first().resolvedIntent)

        val cancel = brain.plan("Ab pehle wala task cancel kar do")
        assertEquals(AgentVoiceState.CANCELLED, cancel.state)
    }
}
