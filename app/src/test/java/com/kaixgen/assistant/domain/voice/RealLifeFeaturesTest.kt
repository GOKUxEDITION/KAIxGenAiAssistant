package com.kaixgen.assistant.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealLifeFeaturesTest {

    @Test
    fun customLockAndUnlockWords_matchUserAddedPhrases() {
        val manager = CustomVoiceLockUnlockManager(context = null)
        assertTrue(manager.addUnlockPhrase("open sesame boss"))
        assertTrue(manager.addLockPhrase("band ho ja abhi"))

        val unlock = manager.matchUtterance("open sesame boss")
        assertTrue(unlock is CustomLockCommand.UnlockNow)

        val lock = manager.matchUtterance("band ho ja abhi")
        assertTrue(lock is CustomLockCommand.LockNow)
    }

    @Test
    fun calculator_handlesHinglishPercentageMathAndUnits() {
        val calc = VoiceUtilityCalculator()
        assertEquals("2500 ka 18% = 450", calc.evaluate("2500 ka 18 percent kitna hota hai"))
        assertEquals("Answer hai 1730", calc.evaluate("450 plus 1280"))
        assertEquals("5 km = 5000 meters", calc.evaluate("5 km in meters"))
    }
}
