package com.example.periodvibe.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyRecord(
    val id: Long = 0,
    val date: LocalDate,
    val cycleId: Long?,
    val isPeriod: Boolean,
    val flowLevel: FlowLevel? = null,
    val symptoms: List<Symptom> = emptyList(),
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val hasSymptoms: Boolean
        get() = symptoms.isNotEmpty()

    val hasNotes: Boolean
        get() = !notes.isNullOrBlank()

    fun updatePeriodStatus(isPeriod: Boolean): DailyRecord {
        return copy(
            isPeriod = isPeriod,
            flowLevel = if (isPeriod) flowLevel ?: FlowLevel.LIGHT else null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateFlowLevel(flowLevel: FlowLevel): DailyRecord {
        return copy(
            flowLevel = flowLevel,
            isPeriod = true,
            updatedAt = LocalDateTime.now()
        )
    }

    fun addSymptom(symptom: Symptom): DailyRecord {
        return copy(
            symptoms = if (symptom in symptoms) symptoms else symptoms + symptom,
            updatedAt = LocalDateTime.now()
        )
    }

    fun removeSymptom(symptom: Symptom): DailyRecord {
        return copy(
            symptoms = symptoms - symptom,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateNotes(notes: String?): DailyRecord {
        return copy(
            notes = notes?.takeIf { it.isNotBlank() },
            updatedAt = LocalDateTime.now()
        )
    }
}
