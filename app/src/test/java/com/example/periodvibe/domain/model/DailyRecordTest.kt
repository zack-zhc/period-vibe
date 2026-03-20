package com.example.periodvibe.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class DailyRecordTest {

    @Test
    fun `hasSymptoms returns true when symptoms list is not empty`() {
        val record = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = listOf(Symptom.FATIGUE)
        )

        assertTrue(record.hasSymptoms)
    }

    @Test
    fun `hasSymptoms returns false when symptoms list is empty`() {
        val record = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = emptyList()
        )

        assertFalse(record.hasSymptoms)
    }

    @Test
    fun `hasNotes returns true when notes is not blank`() {
        val record = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = "Feeling tired today"
        )

        assertTrue(record.hasNotes)
    }

    @Test
    fun `hasNotes returns false when notes is null`() {
        val record = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = null
        )

        assertFalse(record.hasNotes)
    }

    @Test
    fun `hasNotes returns false when notes is blank`() {
        val record = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = "   "
        )

        assertFalse(record.hasNotes)
    }

    @Test
    fun `updatePeriodStatus sets isPeriod and keeps existing flowLevel when true`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = false,
            flowLevel = FlowLevel.HEAVY
        )

        val updated = original.updatePeriodStatus(true)

        assertTrue(updated.isPeriod)
        assertEquals(FlowLevel.HEAVY, updated.flowLevel)
    }

    @Test
    fun `updatePeriodStatus sets isPeriod and sets default flowLevel when null and true`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = false,
            flowLevel = null
        )

        val updated = original.updatePeriodStatus(true)

        assertTrue(updated.isPeriod)
        assertEquals(FlowLevel.LIGHT, updated.flowLevel)
    }

    @Test
    fun `updatePeriodStatus sets isPeriod and clears flowLevel when false`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            flowLevel = FlowLevel.HEAVY
        )

        val updated = original.updatePeriodStatus(false)

        assertFalse(updated.isPeriod)
        assertNull(updated.flowLevel)
    }

    @Test
    fun `updatePeriodStatus updates updatedAt timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = false,
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val updated = original.updatePeriodStatus(true)

        assertTrue(updated.updatedAt.isAfter(pastTime))
    }

    @Test
    fun `updateFlowLevel sets flowLevel and sets isPeriod to true`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = false,
            flowLevel = null
        )

        val updated = original.updateFlowLevel(FlowLevel.MEDIUM)

        assertEquals(FlowLevel.MEDIUM, updated.flowLevel)
        assertTrue(updated.isPeriod)
    }

    @Test
    fun `updateFlowLevel updates updatedAt timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val updated = original.updateFlowLevel(FlowLevel.HEAVY)

        assertTrue(updated.updatedAt.isAfter(pastTime))
    }

    @Test
    fun `addSymptom adds new symptom when not present`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = listOf(Symptom.FATIGUE)
        )

        val updated = original.addSymptom(Symptom.HEADACHE)

        assertEquals(2, updated.symptoms.size)
        assertTrue(updated.symptoms.contains(Symptom.FATIGUE))
        assertTrue(updated.symptoms.contains(Symptom.HEADACHE))
    }

    @Test
    fun `addSymptom does not add duplicate symptom`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = listOf(Symptom.FATIGUE)
        )

        val updated = original.addSymptom(Symptom.FATIGUE)

        assertEquals(1, updated.symptoms.size)
        assertTrue(updated.symptoms.contains(Symptom.FATIGUE))
    }

    @Test
    fun `addSymptom updates updatedAt timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = emptyList(),
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val updated = original.addSymptom(Symptom.FATIGUE)

        assertTrue(updated.updatedAt.isAfter(pastTime))
    }

    @Test
    fun `removeSymptom removes existing symptom`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = listOf(Symptom.FATIGUE, Symptom.HEADACHE)
        )

        val updated = original.removeSymptom(Symptom.FATIGUE)

        assertEquals(1, updated.symptoms.size)
        assertFalse(updated.symptoms.contains(Symptom.FATIGUE))
        assertTrue(updated.symptoms.contains(Symptom.HEADACHE))
    }

    @Test
    fun `removeSymptom does nothing when symptom not present`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = listOf(Symptom.FATIGUE)
        )

        val updated = original.removeSymptom(Symptom.HEADACHE)

        assertEquals(1, updated.symptoms.size)
        assertTrue(updated.symptoms.contains(Symptom.FATIGUE))
    }

    @Test
    fun `removeSymptom updates updatedAt timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            symptoms = listOf(Symptom.FATIGUE),
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val updated = original.removeSymptom(Symptom.FATIGUE)

        assertTrue(updated.updatedAt.isAfter(pastTime))
    }

    @Test
    fun `updateNotes sets notes when not blank`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = null
        )

        val updated = original.updateNotes("New note")

        assertEquals("New note", updated.notes)
    }

    @Test
    fun `updateNotes sets null when notes is blank`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = "Existing note"
        )

        val updated = original.updateNotes("   ")

        assertNull(updated.notes)
    }

    @Test
    fun `updateNotes sets null when notes is null`() {
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = "Existing note"
        )

        val updated = original.updateNotes(null)

        assertNull(updated.notes)
    }

    @Test
    fun `updateNotes updates updatedAt timestamp`() {
        val pastTime = LocalDateTime.now().minusSeconds(1)
        val original = DailyRecord(
            id = 1,
            date = LocalDate.of(2024, 1, 1),
            cycleId = 1,
            isPeriod = true,
            notes = null,
            createdAt = pastTime,
            updatedAt = pastTime
        )

        val updated = original.updateNotes("Test note")

        assertTrue(updated.updatedAt.isAfter(pastTime))
    }
}
