package com.kaixgen.assistant.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseAndMemoryParserTest {

    private val parser = ExpenseAndMemoryParser()

    @Test
    fun parseHinglishExpense_extractsAmountAndCategory() {
        val result = parser.parse("200 rupaye chai pe")
        assertTrue(result is AdvancedIntentResult.AddExpense)
        val exp = result as AdvancedIntentResult.AddExpense
        assertEquals(200, exp.amountRupees)
        assertEquals("Food", exp.category)
    }

    @Test
    fun parseRememberFact_extractsKeyAndValue() {
        val result = parser.parse("yaad rakhna meri car ka number WB38AB1234 hai")
        assertTrue(result is AdvancedIntentResult.RememberFact)
    }

    @Test
    fun parseSosTrigger_returnsSosIntent() {
        val result = parser.parse("help help help")
        assertTrue(result is AdvancedIntentResult.TriggerSos)
    }

    @Test
    fun parseFocusTimer_extractsMinutes() {
        val result = parser.parse("25 minute focus mode")
        assertTrue(result is AdvancedIntentResult.StartFocusTimer)
        assertEquals(25, (result as AdvancedIntentResult.StartFocusTimer).minutes)
    }
}
