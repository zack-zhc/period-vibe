package com.example.periodvibe.domain.model

import java.time.LocalDate
import java.time.Period

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(startDate <= endDate) { "Start date must be before or equal to end date" }
    }

    val duration: Int
        get() = Period.between(startDate, endDate).days + 1

    val dates: List<LocalDate>
        get() = generateSequence(startDate) { it.plusDays(1) }
            .take(duration)
            .toList()

    fun contains(date: LocalDate): Boolean {
        return date in startDate..endDate
    }

    fun overlaps(other: DateRange): Boolean {
        return startDate <= other.endDate && endDate >= other.startDate
    }

    fun expand(days: Int): DateRange {
        return DateRange(
            startDate.minusDays(days.toLong()),
            endDate.plusDays(days.toLong())
        )
    }

    companion object {
        fun fromMonth(year: Int, month: Int): DateRange {
            return DateRange(
                startDate = LocalDate.of(year, month, 1),
                endDate = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .minusDays(1)
            )
        }
    }
}
