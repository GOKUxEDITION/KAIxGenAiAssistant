package com.kaixgen.assistant.domain.voice

import kotlin.math.pow
import kotlin.math.roundToLong

object SmartMoneyCalculator {

    data class EmiResult(
        val monthlyEmi: Long,
        val totalInterest: Long,
        val totalPayable: Long
    )

    data class GstResult(
        val baseAmount: Double,
        val gstAmount: Double,
        val totalAmount: Double
    )

    fun calculateEmi(principal: Double, annualRatePercent: Double, months: Int): EmiResult {
        if (principal <= 0.0 || months <= 0) return EmiResult(0L, 0L, 0L)
        if (annualRatePercent <= 0.0) {
            val emi = (principal / months).roundToLong()
            return EmiResult(emi, 0L, principal.roundToLong())
        }
        val r = (annualRatePercent / 12.0) / 100.0
        val factor = (1.0 + r).pow(months.toDouble())
        val emi = (principal * r * factor) / (factor - 1.0)
        val total = emi * months
        val interest = (total - principal).coerceAtLeast(0.0)
        return EmiResult(
            monthlyEmi = emi.roundToLong(),
            totalInterest = interest.roundToLong(),
            totalPayable = total.roundToLong()
        )
    }

    fun addGst(amount: Double, gstPercent: Double): GstResult {
        val gst = amount * (gstPercent / 100.0)
        return GstResult(amount, gst, amount + gst)
    }

    fun splitBill(totalBill: Double, peopleCount: Int, tipPercent: Double = 0.0): Double {
        if (totalBill <= 0.0 || peopleCount <= 0) return 0.0
        val withTip = totalBill * (1.0 + tipPercent / 100.0)
        return withTip / peopleCount
    }

    fun calculateDiscount(mrp: Double, discountPercent: Double): Pair<Double, Double> {
        val savings = mrp * (discountPercent.coerceIn(0.0, 100.0) / 100.0)
        return Pair((mrp - savings).coerceAtLeast(0.0), savings)
    }
}
