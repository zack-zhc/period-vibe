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
            val existingRecord = cycleRepository.getDailyRecordByDate(date)
            val activeCycle = cycleRepository.getActiveCycle()

            val isPeriod = when (mode) {
                RecordMode.NEW_CYCLE -> true
                RecordMode.AUTO -> true
            }

            var targetCycle: com.example.periodvibe.domain.model.Cycle? = null

            when (mode) {
                RecordMode.NEW_CYCLE -> {
                    targetCycle = cycleRepository.startNewCycle(date)
                }
                RecordMode.AUTO -> {
                    if (activeCycle != null) {
                        targetCycle = activeCycle
                    } else {
                        targetCycle = cycleRepository.startNewCycle(date)
                    }
                }
            }

            val record = if (existingRecord != null) {
                existingRecord.copy(
                    isPeriod = isPeriod,
                    flowLevel = flowLevel,
                    cycleId = targetCycle?.id ?: existingRecord.cycleId
                )
            } else {
                DailyRecord(
                    date = date,
                    cycleId = targetCycle?.id,
                    isPeriod = isPeriod,
                    flowLevel = flowLevel
                )
            }

            if (existingRecord != null) {
                cycleRepository.updateDailyRecord(record)
            } else {
                cycleRepository.saveDailyRecord(record)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
