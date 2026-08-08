package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.utils.DateUtils
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
            val recordsByCycle = records.groupBy { it.cycleId }

            val cycleWithRecords = sortedCycles.mapIndexed { index, cycle ->
                val cycleRecords = recordsByCycle[cycle.id] ?: emptyList()
                // 动态计算周期长度：下一个周期的开始日期 - 当前周期的开始日期
                val calculatedCycleLength = if (index < sortedCycles.size - 1) {
                    val nextCycle = sortedCycles[index + 1]
                    DateUtils.daysBetween(cycle.startDate, nextCycle.startDate)
                } else {
                    null
                }
                CycleWithRecords(
                    cycle = cycle,
                    records = cycleRecords.sortedBy { it.date },
                    calculatedCycleLength = calculatedCycleLength
                )
            }.sortedByDescending { it.cycle.startDate }

            // 计算统计指标
            val validCycles = cycleWithRecords.mapNotNull { it.cycleLengthDays }
            val avgCycleLength = if (validCycles.isNotEmpty()) validCycles.average().toInt() else null
            val longestCycle = validCycles.maxOrNull()
            val shortestCycle = validCycles.minOrNull()

            val allPeriodDays = cycleWithRecords.map { it.periodDaysCount }.filter { it > 0 }
            val avgPeriodLength = if (allPeriodDays.isNotEmpty()) allPeriodDays.average().toInt() else null

            HistoryData(
                cycles = cycleWithRecords,
                totalCycles = cycles.size,
                hasData = cycles.isNotEmpty(),
                avgCycleLength = avgCycleLength,
                longestCycle = longestCycle,
                shortestCycle = shortestCycle,
                avgPeriodLength = avgPeriodLength
            )
        }
    }

    suspend fun deleteCycle(cycleId: Long) {
        val cycle = cycleRepository.getCycleById(cycleId) ?: return
        cycleRepository.deleteCycle(cycle)
    }

    suspend fun deleteDailyRecord(recordId: Long) {
        val record = cycleRepository.getDailyRecordById(recordId) ?: return
        cycleRepository.deleteDailyRecord(record)
    }

    suspend fun deleteCycles(cycleIds: List<Long>) {
        val cycles = cycleIds.mapNotNull { cycleRepository.getCycleById(it) }
        if (cycles.isNotEmpty()) {
            // 批量删除在单个事务中执行，避免中途失败残留部分数据
            cycleRepository.deleteCycles(cycles)
        }
    }

    suspend fun updateDailyRecord(record: DailyRecord) {
        // 编辑时日期可能被修改，需按新日期重新归属周期，避免记录出现在错误的周期中
        cycleRepository.updateDailyRecord(cycleRepository.reassignCycleForDate(record))
    }
}

data class HistoryData(
    val cycles: List<CycleWithRecords>,
    val totalCycles: Int,
    val hasData: Boolean,
    val avgCycleLength: Int? = null,
    val longestCycle: Int? = null,
    val shortestCycle: Int? = null,
    val avgPeriodLength: Int? = null
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

    val dateRangeFormatted: String
        get() {
            val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
            val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", Locale.CHINA)

            val periodRecords = records.filter { it.isPeriod }.sortedBy { it.date }
            val startDisplay = periodRecords.firstOrNull()?.date ?: cycle.startDate
            val endDisplay = if (cycle.endDate != null) {
                cycle.endDate
            } else {
                periodRecords.lastOrNull()?.date ?: cycle.startDate
            }

            val startMonthDay = startDisplay.format(monthDayFormatter)
            val endMonthDay = endDisplay.format(monthDayFormatter)
            val year = startDisplay.format(yearFormatter)

            return "${startMonthDay}-${endMonthDay}，${year}"
        }

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
            ?.groupingBy { it }
            ?.eachCount()
            ?.maxByOrNull { it.value }
            ?.key

    val year: Int
        get() = cycle.startDate.year

    val dateRangeWithoutYear: String
        get() {
            val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
            val yearFormatter = DateTimeFormatter.ofPattern("yyyy年", Locale.CHINA)
            val periodRecords = records.filter { it.isPeriod }.sortedBy { it.date }
            val startDisplay = periodRecords.firstOrNull()?.date ?: cycle.startDate
            val endDisplay = if (cycle.endDate != null) {
                cycle.endDate
            } else {
                periodRecords.lastOrNull()?.date ?: cycle.startDate
            }
            val startMonthDay = startDisplay.format(monthDayFormatter)
            val endMonthDay = endDisplay.format(monthDayFormatter)
            // 跨年周期补充结束年份，避免 "12月30日 - 1月3日" 歧义
            val endWithYear = if (startDisplay.year != endDisplay.year) {
                "${endMonthDay}（${endDisplay.format(yearFormatter)}）"
            } else {
                endMonthDay
            }
            return "${startMonthDay} - ${endWithYear}"
        }
}
