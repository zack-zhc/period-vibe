package com.example.periodvibe.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

data class Cycle(
    val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val cycleLength: Int?,
    val periodLength: Int?,
    val averageFlowLevel: FlowLevel? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val isCurrentCycle: Boolean
        get() = !isCompleted

    val duration: Int
        get() = if (endDate != null) {
            Period.between(startDate, endDate).days
        } else {
            Period.between(startDate, LocalDate.now()).days
        }

    fun complete(endDate: LocalDate): Cycle {
        return copy(
            endDate = endDate,
            cycleLength = Period.between(startDate, endDate).days,
            isCompleted = true,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updatePeriodLength(newLength: Int): Cycle {
        return copy(periodLength = newLength, updatedAt = LocalDateTime.now())
    }
}
