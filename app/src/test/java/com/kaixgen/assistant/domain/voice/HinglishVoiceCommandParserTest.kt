package com.kaixgen.assistant.domain.voice

import com.kaixgen.assistant.domain.model.CommandRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HinglishVoiceCommandParserTest {

    private val parser = HinglishVoiceCommandParser()

    @Test
    fun parsesHinglishAndDevanagariOpenAppCommands() {
        val hinglish = parser.parse("Hey KAI, WhatsApp kholo please")
        assertEquals(VoiceIntentType.OPEN_APP, hinglish.intent)
        assertEquals("Whatsapp", hinglish.slots["app"])
        assertEquals(CommandRiskLevel.LOW, hinglish.riskLevel)
        assertFalse(hinglish.requiresConfirmation)

        val hindi = parser.parse("व्हाट्सऐप खोलो")
        assertEquals(VoiceIntentType.OPEN_APP, hindi.intent)
        assertEquals("Whatsapp", hindi.slots["app"])
    }

    @Test
    fun enforcesHighRiskConfirmationOnSendMessage() {
        val cmd = parser.parse("Rahul ko message karo main late hoon")
        assertEquals(VoiceIntentType.SEND_MESSAGE, cmd.intent)
        assertEquals("Rahul", cmd.slots["recipient"])
        assertEquals(CommandRiskLevel.HIGH, cmd.riskLevel)
        assertTrue(cmd.requiresConfirmation)
    }

    @Test
    fun parsesCallBatteryTorchVolumeAndFindPhone() {
        val call = parser.parse("Mummy ko call karo")
        assertEquals(VoiceIntentType.CALL_CONTACT, call.intent)
        assertEquals("Mummy", call.slots["contact"])
        assertTrue(call.requiresConfirmation)

        val battery = parser.parse("Kitni battery hai?")
        assertEquals(VoiceIntentType.BATTERY_STATUS, battery.intent)

        val torchOn = parser.parse("Torch chalu karo")
        assertEquals(VoiceIntentType.TOGGLE_TORCH, torchOn.intent)
        assertEquals("on", torchOn.slots["state"])

        val torchOff = parser.parse("Torch band karo")
        assertEquals(VoiceIntentType.TOGGLE_TORCH, torchOff.intent)
        assertEquals("off", torchOff.slots["state"])

        val vol = parser.parse("Volume 60 percent")
        assertEquals(VoiceIntentType.VOLUME_CONTROL, vol.intent)
        assertEquals("60", vol.slots["level"])

        val find = parser.parse("KAI, phone kahan hai?")
        assertEquals(VoiceIntentType.FIND_PHONE, find.intent)
    }

    @Test
    fun matchesCustomVoiceLockPhraseWithFuzzyTolerance() {
        val cmd = parser.parse("hare krishna", customLockPhrase = "Hare Krishna")
        assertEquals(VoiceIntentType.LOCK_PHONE, cmd.intent)
        assertEquals("custom_phrase", cmd.slots["trigger"])
    }
}
