package com.example.periodvibe.domain.model

import kotlin.math.pow

data class CycleStatistics(
    val totalCycles: Int,
    val averageCycleLength: Double,
    val averagePeriodLength: Double,
    val shortestCycle: Int,
    val longestCycle: Int,
    val shortestPeriod: Int,
    val longestPeriod: Int,
    val regularityScore: RegularityScore,
    val totalRecordDays: Int,
    val cycleLengths: List<Int>,
    val periodLengths: List<Int>
) {
    val cycleLengthVariance: Double
        get() = calculateVariance(cycleLengths)

    val periodLengthVariance: Double
        get() = calculateVariance(periodLengths)

    val cycleLengthStandardDeviation: Double
        get() = kotlin.math.sqrt(cycleLengthVariance)

    val periodLengthStandardDeviation: Double
        get() = kotlin.math.sqrt(periodLengthVariance)

    private fun calculateVariance(values: List<Int>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        return values.map { (it - mean).pow(2.0) }.average()
    }

    enum class RegularityScore(val label: String, val color: String) {
        VERY_REGULAR("非常规律", "#4CAF50"),
        REGULAR("规律", "#8BC34A"),
        SOMEWHAT_REGULAR("不太规律", "#FFC107"),
        IRREGULAR("不规律", "#FF9800"),
        VERY_IRREGULAR("很不规律", "#F44336");

        companion object {
            fun fromCoefficientOfVariation(cv: Double): RegularityScore {
                return when {
                    cv < 0.10 -> VERY_REGULAR
                    cv < 0.20 -> REGULAR
                    cv < 0.30 -> SOMEWHAT_REGULAR
                    cv < 0.40 -> IRREGULAR
                    else -> VERY_IRREGULAR
                }
            }
        }
    }
}
