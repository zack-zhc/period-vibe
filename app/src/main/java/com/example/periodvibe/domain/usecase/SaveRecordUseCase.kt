package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.RecordMode
import java.time.LocalDate
import javax.inject.Inject

class SaveRecordUseCase @Inject constructor(
    private val cycleRepository: CycleRepository
) {
    /**
     * @param existingRecord 编辑模式下的原记录（按 id 更新，日期变更时重新归属周期）
     */
    suspend operator fun invoke(
        date: LocalDate,
        mode: RecordMode,
        flowLevel: FlowLevel?,
        existingRecord: DailyRecord? = null
    ): Result<Unit> {
        return try {
            when (mode) {
                RecordMode.EDIT -> {
                    val record = existingRecord ?: cycleRepository.getDailyRecordByDate(date)
                        ?: throw IllegalArgumentException("EDIT mode requires existing record for date $date")
                    // 日期可能被修改：按原记录 id 更新，并按新日期重新归属周期
                    val updated = cycleRepository.reassignCycleForDate(
                        record.copy(date = date, flowLevel = flowLevel)
                    )
                    cycleRepository.updateDailyRecord(updated)
                }

                RecordMode.NEW_CYCLE -> {
                    val newCycle = cycleRepository.startNewCycle(date)
                    val existing = cycleRepository.getDailyRecordByDate(date)
                    if (existing != null) {
                        // 该日期已有记录：保留 id 更新并归属到新周期，避免 REPLACE 换 id
                        val updated = cycleRepository.reassignCycleForDate(
                            existing.copy(flowLevel = flowLevel)
                        )
                        cycleRepository.updateDailyRecord(updated)
                    } else {
                        cycleRepository.saveDailyRecord(
                            DailyRecord(
                                date = date,
                                cycleId = newCycle.id,
                                isPeriod = true,
                                flowLevel = flowLevel
                            )
                        )
                    }
                }

                RecordMode.AUTO -> {
                    val existing = cycleRepository.getDailyRecordByDate(date)
                    if (existing != null) {
                        // 该日期已有记录：按 id 更新，避免 REPLACE 换 id / 周期漂移
                        val updated = cycleRepository.reassignCycleForDate(
                            existing.copy(flowLevel = flowLevel)
                        )
                        cycleRepository.updateDailyRecord(updated)
                    } else {
                        // 按日期归属周期：补记到所在的历史/活动周期，都没有才新建
                        val targetCycle = cycleRepository.getCycleForDate(date)
                            ?: cycleRepository.getActiveCycle()
                            ?: cycleRepository.startNewCycle(date)
                        cycleRepository.saveDailyRecord(
                            DailyRecord(
                                date = date,
                                cycleId = targetCycle?.id,
                                isPeriod = true,
                                flowLevel = flowLevel
                            )
                        )
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
