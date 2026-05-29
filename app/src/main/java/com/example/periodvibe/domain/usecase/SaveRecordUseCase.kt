package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.ui.home.RecordMode
import javax.inject.Inject

class SaveRecordUseCase @Inject constructor(
    private val cycleRepository: CycleRepository
) {
    suspend operator fun invoke(
        date: java.time.LocalDate,
        mode: RecordMode,
        flowLevel: FlowLevel?
    ): Result<Unit> {
        return try {
            when (mode) {
                RecordMode.EDIT -> {
                    val existingRecord = cycleRepository.getDailyRecordByDate(date)
                        ?: throw IllegalArgumentException("EDIT mode requires existing record for date $date")
                    val updatedRecord = existingRecord.copy(
                        flowLevel = flowLevel
                    )
                    cycleRepository.updateDailyRecord(updatedRecord)
                }

                RecordMode.NEW_CYCLE, RecordMode.AUTO -> {
                    val activeCycle = cycleRepository.getActiveCycle()
                    val isPeriod = true // 这两个模式都是记录经期

                    var targetCycle: com.example.periodvibe.domain.model.Cycle? = null

                    when (mode) {
                        RecordMode.NEW_CYCLE -> {
                            targetCycle = cycleRepository.startNewCycle(date)
                        }
                        RecordMode.AUTO -> {
                            targetCycle = if (activeCycle != null) {
                                activeCycle
                            } else {
                                cycleRepository.startNewCycle(date)
                            }
                        }
                        else -> {} // 不会走到这里
                    }

                    val record = DailyRecord(
                        date = date,
                        cycleId = targetCycle?.id,
                        isPeriod = isPeriod,
                        flowLevel = flowLevel
                    )
                    cycleRepository.saveDailyRecord(record)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
