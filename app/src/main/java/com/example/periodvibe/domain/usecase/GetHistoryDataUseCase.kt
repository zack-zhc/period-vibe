package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class GetHistoryDataUseCase @Inject constructor(
    private val cycleRepository: CycleRepository
) {

    operator fun invoke(): Flow<HistoryData> {
        return combine(
            cycleRepository.getAllCycles(),
            cycleRepository.getAllDailyRecords()
        ) { cycles, records ->
            val sortedCycles = cycles.sortedBy { it.startDate }

            val cycleWithRecords = sortedCycles.mapIndexed { index, cycle ->
                val cycleRecords = records.filter { it.cycleId == cycle.id }
                // 动态计算周期长度：下一个周期的开始日期 - 当前周期的开始日期
                val calculatedCycleLength = if (index < sortedCycles.size - 1) {
                    val nextCycle = sortedCycles[index + 1]
                    Period.between(cycle.startDate, nextCycle.startDate).days
                } else {
                    null
                }
                CycleWithRecords(
                    cycle = cycle,
                    records = cycleRecords.sortedBy { it.date },
                    calculatedCycleLength = calculatedCycleLength
                )
            }.sortedByDescending { it.cycle.startDate }

            val unassociatedRecords = records.filter { it.cycleId == null }
            val hasUnassociatedRecords = unassociatedRecords.isNotEmpty()

            HistoryData(
                cycles = cycleWithRecords,
                totalCycles = cycles.size,
                hasData = cycles.isNotEmpty() || hasUnassociatedRecords,
                unassociatedRecords = unassociatedRecords.sortedBy { it.date }
            )
        }
    }

    suspend fun getCycleDetails(cycleId: Long): CycleDetails? {
        val cycle = cycleRepository.getCycleById(cycleId) ?: return null
        val records = cycleRepository.getDailyRecordsByCycleId(cycleId)
        val recordsList = mutableListOf<DailyRecord>()
        records.collect { recordsList.addAll(it) }

        val periodDays = recordsList.count { it.isPeriod }
        val averageFlowLevel = recordsList
            .mapNotNull { it.flowLevel }
            .takeIf { it.isNotEmpty() }
            ?.map { it.value }
            ?.average()
            ?.let { FlowLevel.fromValue(it.toInt()) }

        val commonSymptoms = recordsList
            .flatMap { it.symptoms }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        return CycleDetails(
            cycle = cycle,
            records = recordsList.sortedBy { it.date },
            periodDays = periodDays,
            averageFlowLevel = averageFlowLevel,
            commonSymptoms = commonSymptoms
        )
    }

    suspend fun deleteCycle(cycleId: Long) {
        val cycle = cycleRepository.getCycleById(cycleId) ?: return
        cycleRepository.deleteCycle(cycle)
    }

    suspend fun deleteDailyRecord(recordId: Long) {
        val record = cycleRepository.getDailyRecordById(recordId) ?: return
        cycleRepository.deleteDailyRecord(record)
    }

    suspend fun updateDailyRecord(record: DailyRecord) {
        cycleRepository.updateDailyRecord(record)
    }
}

data class HistoryData(
    val cycles: List<CycleWithRecords>,
    val totalCycles: Int,
    val hasData: Boolean,
    val unassociatedRecords: List<DailyRecord> = emptyList()
)

data class CycleWithRecords(
    val cycle: Cycle,
    val records: List<DailyRecord>,
    val calculatedCycleLength: Int? = null
) {
    val startDateFormatted: String
        get() = cycle.startDate.format(
            DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA)
        )

    val durationDays: Int
        get() = cycle.duration

    val cycleLengthDays: Int?
        get() = calculatedCycleLength ?: cycle.cycleLength

    val periodDaysCount: Int
        get() = records.count { it.isPeriod }

    val averageFlowLevel: FlowLevel?
        get() = records
            .mapNotNull { it.flowLevel }
            .takeIf { it.isNotEmpty() }
            ?.map { it.value }
            ?.average()
            ?.let { FlowLevel.fromValue(it.toInt()) }
}

data class CycleDetails(
    val cycle: Cycle,
    val records: List<DailyRecord>,
    val periodDays: Int,
    val averageFlowLevel: FlowLevel?,
    val commonSymptoms: List<com.example.periodvibe.domain.model.Symptom>
)
