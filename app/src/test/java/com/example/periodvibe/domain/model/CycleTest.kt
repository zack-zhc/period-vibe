package com.example.periodvibe.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class CycleTest {

    @Test
    fun `isCurrentCycle returns true when cycle is not completed`() {
        val cycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false
        )

        assertTrue(cycle.isCurrentCycle)
    }

    @Test
    fun `isCurrentCycle returns false when cycle is completed`() {
        val cycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 5),
            cycleLength = 28,
            periodLength = 5,
            isCompleted = true
        )

        assertFalse(cycle.isCurrentCycle)
    }

    @Test
    fun `duration returns correct days with endDate including +1 day`() {
        val cycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 5),
            cycleLength = null,
            periodLength = null,
            isCompleted = true
        )

        // Jan 1 to Jan 5 is 5 days inclusive (1,2,3,4,5)
        assertEquals(5, cycle.duration)
    }

    @Test
    fun `duration returns 1 day when start and end are same day`() {
        val cycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 1),
            cycleLength = null,
            periodLength = null,
            isCompleted = true
        )

        assertEquals(1, cycle.duration)
    }

    @Test
    fun `duration returns correct days without endDate`() {
        val today = LocalDate.now()
        val startDate = today.minusDays(5)

        val cycle = Cycle(
            id = 1,
            startDate = startDate,
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false
        )

        // Should be 6 days (today inclusive)
        assertEquals(6, cycle.duration)
    }

    @Test
    fun `complete sets endDate and marks as completed`() {
        val originalCycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = null,
            cycleLength = 28,
            periodLength = null,
            isCompleted = false
        )

        val endDate = LocalDate.of(2024, 1, 10)
        val completedCycle = originalCycle.complete(endDate)

        assertEquals(endDate, completedCycle.endDate)
        assertTrue(completedCycle.isCompleted)
        assertNull(completedCycle.cycleLength) // cycleLength should be nulled
    }

    @Test
    fun `complete updates updatedAt timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val originalCycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false,
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val completedCycle = originalCycle.complete(LocalDate.of(2024, 1, 10))

        assertTrue(completedCycle.updatedAt.isAfter(pastTime))
    }

    @Test
    fun `updatePeriodLength updates periodLength and timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val originalCycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false,
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val updatedCycle = originalCycle.updatePeriodLength(5)

        assertEquals(5, updatedCycle.periodLength)
        assertTrue(updatedCycle.updatedAt.isAfter(pastTime))
    }

    @Test
    fun `updatePeriodLength can set to zero`() {
        val originalCycle = Cycle(
            id = 1,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = null,
            cycleLength = null,
            periodLength = 5,
            isCompleted = false
        )

        val updatedCycle = originalCycle.updatePeriodLength(0)

        assertEquals(0, updatedCycle.periodLength)
    }
}
