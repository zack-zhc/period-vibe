package com.example.periodvibe.domain.model

import java.time.LocalDate
import java.time.Period

data class Prediction(
    val nextPeriodStart: LocalDate,
    val nextPeriodEnd: LocalDate,
    val ovulationDate: LocalDate,
    val ovulationWindow: ClosedRange<LocalDate>,
    val fertileWindow: ClosedRange<LocalDate>,
    val confidence: Float,
    val predictedCycleLength: Int,
    val predictedPeriodLength: Int
) {
    val daysUntilPeriod: Int
        get() = Period.between(LocalDate.now(), nextPeriodStart).days

    val daysUntilOvulation: Int
        get() = Period.between(LocalDate.now(), ovulationDate).days

    val isInPeriodWindow: Boolean
        get() = LocalDate.now() in nextPeriodStart..nextPeriodEnd

    val isInOvulationWindow: Boolean
        get() = LocalDate.now() in ovulationWindow

    val isInFertileWindow: Boolean
        get() = LocalDate.now() in fertileWindow

    val confidenceLevel: ConfidenceLevel
        get() = when {
            confidence >= 0.8f -> ConfidenceLevel.HIGH
            confidence >= 0.5f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

    enum class ConfidenceLevel {
        HIGH, MEDIUM, LOW
    }
}
