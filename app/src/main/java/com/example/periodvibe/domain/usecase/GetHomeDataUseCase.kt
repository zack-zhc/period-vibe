package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.CycleInfo
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.model.Prediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val settingsRepository: com.example.periodvibe.data.repository.SettingsRepository
) {

    operator fun invoke(): Flow<HomeData> {
        return kotlinx.coroutines.flow.combine(
            cycleRepository.getAllCycles(),
            settingsRepository.getSettings()
        ) { cycles, settings ->
            val cycleInfo = if (cycles.isEmpty()) {
                null
            } else {
                val currentCycle = cycles.firstOrNull { it.isCurrentCycle }

                if (currentCycle != null) {
                    createCurrentCycleInfo(currentCycle)
                } else {
                    val latestCycle = cycles.firstOrNull()
                    if (latestCycle != null) {
                        createPredictionBasedInfo(latestCycle, settings)
                    } else {
                        null
                    }
                }
            }

            HomeData(
                cycleInfo = cycleInfo,
                totalCycles = cycles.size,
                hasData = cycles.isNotEmpty()
            )
        }
    }

    private fun createCurrentCycleInfo(cycle: Cycle): CycleInfo {
        val today = LocalDate.now()
        val dayInCycle = Period.between(cycle.startDate, today).days + 1
        val phase = determinePhase(cycle, dayInCycle)
        val cycleLength = cycle.cycleLength ?: 28
        val daysUntilNextPeriod = cycleLength - dayInCycle

        return CycleInfo(
            cycle = cycle,
            phase = phase,
            dayInCycle = dayInCycle,
            daysUntilNextPeriod = if (daysUntilNextPeriod > 0) daysUntilNextPeriod else null,
            prediction = null
        )
    }

    private fun createPredictionBasedInfo(latestCycle: Cycle, settings: com.example.periodvibe.domain.model.Settings?): CycleInfo {
        val today = LocalDate.now()
        
        val cycleLength = latestCycle.cycleLength ?: settings?.cycleLengthDefault ?: 28
        val periodLength = latestCycle.periodLength ?: settings?.periodLengthDefault ?: 5
        
        val predictedNextPeriodStart = latestCycle.startDate!!.plusDays(cycleLength.toLong())
        val daysUntilNextPeriod = Period.between(today, predictedNextPeriodStart).days

        val prediction = Prediction(
            nextPeriodStart = predictedNextPeriodStart,
            nextPeriodEnd = predictedNextPeriodStart.plusDays(periodLength.toLong()),
            ovulationDate = predictedNextPeriodStart.minusDays(14),
            ovulationWindow = predictedNextPeriodStart.minusDays(17)..predictedNextPeriodStart.minusDays(11),
            fertileWindow = predictedNextPeriodStart.minusDays(19)..predictedNextPeriodStart.minusDays(9),
            confidence = if (latestCycle.cycleLength != null && latestCycle.periodLength != null) 0.7f else 0.5f,
            predictedCycleLength = cycleLength,
            predictedPeriodLength = periodLength
        )

        val phase = CyclePhase.fromDate(today, prediction, null)
        val dayInCycle = Period.between(latestCycle.startDate!!, today).days + 1

        return CycleInfo(
            cycle = latestCycle,
            phase = phase,
            dayInCycle = dayInCycle,
            daysUntilNextPeriod = if (daysUntilNextPeriod > 0) daysUntilNextPeriod else null,
            prediction = prediction
        )
    }

    private fun determinePhase(cycle: Cycle, dayInCycle: Int): CyclePhase {
        val periodLength = cycle.periodLength ?: 5
        val cycleLength = cycle.cycleLength ?: 28
        
        return when {
            dayInCycle <= periodLength -> CyclePhase.MENSTRATION
            dayInCycle <= cycleLength - 14 - 3 -> CyclePhase.FOLLICULAR
            dayInCycle <= cycleLength - 14 + 3 -> CyclePhase.OVULATION
            dayInCycle <= cycleLength - 14 + 6 -> CyclePhase.FERTILE
            else -> CyclePhase.LUTEAL
        }
    }

    data class HomeData(
        val cycleInfo: CycleInfo?,
        val totalCycles: Int,
        val hasData: Boolean
    )
}