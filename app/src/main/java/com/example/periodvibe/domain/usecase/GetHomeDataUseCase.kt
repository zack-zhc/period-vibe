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

            val (cycleLength, phaseInfo, ovulationDate) = if (cycles.isEmpty()) {
                Triple(28, null, null)
            } else {
                val currentCycle = cycles.firstOrNull { it.isCurrentCycle }
                if (currentCycle != null) {
                    val today = LocalDate.now()
                    val dayInCycle = Period.between(currentCycle.startDate, today).days + 1
                    val phaseInfo = getPhaseInfo(currentCycle, dayInCycle)
                    val cycleLength = currentCycle.cycleLength ?: 28
                    val ovulationDate = currentCycle.startDate.plusDays((cycleLength - 14).toLong())
                    Triple(cycleLength, phaseInfo, ovulationDate)
                } else {
                    val latestCycle = cycles.firstOrNull()
                    if (latestCycle != null) {
                        val prediction = createPrediction(latestCycle, settings)
                        val today = LocalDate.now()
                        val dayInCycle = Period.between(latestCycle.startDate!!, today).days + 1
                        val phaseInfo = getPhaseInfoForPrediction(prediction, dayInCycle)
                        Triple(prediction.predictedCycleLength, phaseInfo, prediction.ovulationDate)
                    } else {
                        Triple(28, null, null)
                    }
                }
            }

            HomeData(
                cycleInfo = cycleInfo,
                totalCycles = cycles.size,
                hasData = cycles.isNotEmpty(),
                cycleLength = cycleLength,
                currentPhaseStartDay = phaseInfo?.startDay ?: 1,
                currentPhaseEndDay = phaseInfo?.endDay ?: 28,
                nextPhaseName = phaseInfo?.nextPhaseName ?: "月经期",
                daysUntilNextPhase = phaseInfo?.let { it.endDay - (cycleInfo?.dayInCycle ?: 1) + 1 } ?: 0,
                ovulationDate = ovulationDate
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

        val cycleLength = if (settings?.autoCalculateCycle == true) {
            latestCycle.cycleLength ?: settings?.cycleLengthDefault ?: 28
        } else {
            settings?.cycleLengthDefault ?: 28
        }

        val periodLength = if (settings?.autoCalculateCycle == true) {
            latestCycle.periodLength ?: settings?.periodLengthDefault ?: 5
        } else {
            settings?.periodLengthDefault ?: 5
        }

        // 使用最新周期的结束日期作为基准来预测下一个周期（如果没有结束日期则用开始日期）
        val referenceDate = latestCycle.endDate ?: latestCycle.startDate
        val predictedNextPeriodStart = referenceDate.plusDays(cycleLength.toLong())
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

    private fun createPrediction(latestCycle: Cycle, settings: com.example.periodvibe.domain.model.Settings?): Prediction {
        val cycleLength = if (settings?.autoCalculateCycle == true) {
            latestCycle.cycleLength ?: settings?.cycleLengthDefault ?: 28
        } else {
            settings?.cycleLengthDefault ?: 28
        }

        val periodLength = if (settings?.autoCalculateCycle == true) {
            latestCycle.periodLength ?: settings?.periodLengthDefault ?: 5
        } else {
            settings?.periodLengthDefault ?: 5
        }

        val referenceDate = latestCycle.endDate ?: latestCycle.startDate
        val predictedNextPeriodStart = referenceDate.plusDays(cycleLength.toLong())

        return Prediction(
            nextPeriodStart = predictedNextPeriodStart,
            nextPeriodEnd = predictedNextPeriodStart.plusDays(periodLength.toLong()),
            ovulationDate = predictedNextPeriodStart.minusDays(14),
            ovulationWindow = predictedNextPeriodStart.minusDays(17)..predictedNextPeriodStart.minusDays(11),
            fertileWindow = predictedNextPeriodStart.minusDays(19)..predictedNextPeriodStart.minusDays(9),
            confidence = if (latestCycle.cycleLength != null && latestCycle.periodLength != null) 0.7f else 0.5f,
            predictedCycleLength = cycleLength,
            predictedPeriodLength = periodLength
        )
    }

    private fun determinePhase(cycle: Cycle, dayInCycle: Int): CyclePhase {
        val periodLength = cycle.periodLength ?: 5
        val cycleLength = cycle.cycleLength ?: 28

        return when {
            dayInCycle <= periodLength -> CyclePhase.MENSTRATION
            dayInCycle <= cycleLength - 14 - 3 -> CyclePhase.FOLLICULAR
            dayInCycle <= cycleLength - 14 + 3 -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }
    }

    private data class PhaseInfo(
        val phase: CyclePhase,
        val startDay: Int,
        val endDay: Int,
        val nextPhaseName: String
    )

    private fun getPhaseInfo(cycle: Cycle, dayInCycle: Int): PhaseInfo {
        val periodLength = cycle.periodLength ?: 5
        val cycleLength = cycle.cycleLength ?: 28

        return when {
            dayInCycle <= periodLength -> PhaseInfo(
                phase = CyclePhase.MENSTRATION,
                startDay = 1,
                endDay = periodLength,
                nextPhaseName = "卵泡期"
            )
            dayInCycle <= cycleLength - 14 - 3 -> PhaseInfo(
                phase = CyclePhase.FOLLICULAR,
                startDay = periodLength + 1,
                endDay = cycleLength - 14 - 3,
                nextPhaseName = "排卵期"
            )
            dayInCycle <= cycleLength - 14 + 3 -> PhaseInfo(
                phase = CyclePhase.OVULATION,
                startDay = cycleLength - 14 - 2,
                endDay = cycleLength - 14 + 3,
                nextPhaseName = "黄体期"
            )
            else -> PhaseInfo(
                phase = CyclePhase.LUTEAL,
                startDay = cycleLength - 14 + 4,
                endDay = cycleLength,
                nextPhaseName = "月经期"
            )
        }
    }

    private fun getPhaseInfoForPrediction(prediction: Prediction, dayInCycle: Int): PhaseInfo {
        val cycleLength = prediction.predictedCycleLength
        val periodLength = prediction.predictedPeriodLength
        val today = LocalDate.now()

        return when {
            today in prediction.nextPeriodStart..prediction.nextPeriodEnd -> PhaseInfo(
                phase = CyclePhase.MENSTRATION,
                startDay = 1,
                endDay = periodLength,
                nextPhaseName = "卵泡期"
            )
            today in prediction.fertileWindow && today !in prediction.ovulationWindow -> PhaseInfo(
                phase = CyclePhase.FERTILE,
                startDay = periodLength + 1,
                endDay = cycleLength - 14 - 4,
                nextPhaseName = "排卵期"
            )
            today in prediction.ovulationWindow -> PhaseInfo(
                phase = CyclePhase.OVULATION,
                startDay = cycleLength - 14 - 3,
                endDay = cycleLength - 14 + 3,
                nextPhaseName = "黄体期"
            )
            else -> PhaseInfo(
                phase = CyclePhase.LUTEAL,
                startDay = cycleLength - 14 + 4,
                endDay = cycleLength,
                nextPhaseName = "月经期"
            )
        }
    }

    data class HomeData(
        val cycleInfo: CycleInfo?,
        val totalCycles: Int,
        val hasData: Boolean,
        val cycleLength: Int,
        val currentPhaseStartDay: Int,
        val currentPhaseEndDay: Int,
        val nextPhaseName: String,
        val daysUntilNextPhase: Int,
        val ovulationDate: LocalDate?
    )
}
