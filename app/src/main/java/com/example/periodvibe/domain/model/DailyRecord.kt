package com.example.periodvibe.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyRecord(
    val id: Long = 0,
    val date: LocalDate,
    val cycleId: Long?,
    val isPeriod: Boolean,
    val flowLevel: FlowLevel? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
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
}
