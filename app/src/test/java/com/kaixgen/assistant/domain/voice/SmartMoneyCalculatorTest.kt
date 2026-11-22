package com.kaixgen.assistant.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartMoneyCalculatorTest {

    @Test
    fun emiCalculation_computesExpectedMonthlyAndInterest() {
        val res = SmartMoneyCalculator.calculateEmi(100_000.0, 12.0, 12)
        assertTrue(res.monthlyEmi in 8880L..8890L)
        assertTrue(res.totalInterest > 6000L)
    }

    @Test
    fun gstAndBillSplit_computeAccurately() {
        val gst = SmartMoneyCalculator.addGst(1000.0, 18.0)
        assertEquals(180.0, gst.gstAmount, 0.01)
        assertEquals(1180.0, gst.totalAmount, 0.01)

        val split = SmartMoneyCalculator.splitBill(1200.0, 4)
        assertEquals(300.0, split, 0.01)

        val (finalPrice, savings) = SmartMoneyCalculator.calculateDiscount(2000.0, 25.0)
        assertEquals(1500.0, finalPrice, 0.01)
        assertEquals(500.0, savings, 0.01)
    }
}
