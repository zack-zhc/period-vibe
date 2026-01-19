package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.Prediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetCalendarDataUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(yearMonth: YearMonth): Flow<CalendarData> {
        return combine(
            cycleRepository.getAllCycles(),
            cycleRepository.getAllDailyRecords(),
            settingsRepository.getSettings()
        ) { cycles, records, settings ->
            val monthStart = yearMonth.atDay(1)
            val monthEnd = yearMonth.atEndOfMonth()

            val calendarDays = generateCalendarDays(yearMonth, cycles, records, settings)
            val prediction = createPrediction(cycles, settings)

            CalendarData(
                yearMonth = yearMonth,
                days = calendarDays,
                prediction = prediction,
                hasData = cycles.isNotEmpty()
            )
        }
    }

    private fun generateCalendarDays(
        yearMonth: YearMonth,
        cycles: List<Cycle>,
        records: List<DailyRecord>,
        settings: com.example.periodvibe.domain.model.Settings?
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        val firstDayOfWeek = monthStart.dayOfWeek.value % 7

        val prediction = createPrediction(cycles, settings)
        val currentCycle = cycles.firstOrNull { it.isCurrentCycle }

        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDay.Empty)
        }

        for (day in 1..monthEnd.dayOfMonth) {
            val date = monthStart.withDayOfMonth(day)
            val record = records.find { it.date == date }
            val phase = CyclePhase.fromDate(date, prediction, currentCycle)
            val isToday = date == LocalDate.now()

            val dayType = determineDayType(date, record, cycles, prediction)
            val isPredictedPeriod = prediction != null && date in prediction.nextPeriodStart..prediction.nextPeriodEnd
            val isPredictedOvulation = prediction != null && date in prediction.ovulationWindow
            val isPredictedFertile = prediction != null && date in prediction.fertileWindow

            days.add(
                CalendarDay.Data(
                    date = date,
                    dayOfMonth = day,
                    record = record,
                    phase = phase,
                    dayType = dayType,
                    isToday = isToday,
                    isPredictedPeriod = isPredictedPeriod,
                    isPredictedOvulation = isPredictedOvulation,
                    isPredictedFertile = isPredictedFertile
                )
            )
        }

        val remainingDays = 7 - (days.size % 7)
        if (remainingDays < 7) {
            for (i in 0 until remainingDays) {
                days.add(CalendarDay.Empty)
            }
        }

        return days
    }

    private fun determineDayType(
        date: LocalDate,
        record: DailyRecord?,
        cycles: List<Cycle>,
        prediction: Prediction?
    ): CalendarDayType {
        if (record != null && record.isPeriod) {
            return CalendarDayType.PERIOD
        }

        if (prediction != null && date in prediction.nextPeriodStart..prediction.nextPeriodEnd) {
            return CalendarDayType.PREDICTED_PERIOD
        }

        if (prediction != null && date in prediction.ovulationWindow) {
            return CalendarDayType.OVULATION
        }

        if (prediction != null && date in prediction.fertileWindow) {
            return CalendarDayType.FERTILE
        }

        return CalendarDayType.NORMAL
    }

    private fun createPrediction(
        cycles: List<Cycle>,
        settings: com.example.periodvibe.domain.model.Settings?
    ): Prediction? {
        if (cycles.isEmpty()) return null

        val latestCycle = cycles.firstOrNull() ?: return null
        
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

        val predictedNextPeriodStart = latestCycle.startDate!!.plusDays(cycleLength.toLong())

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

    data class CalendarData(
        val yearMonth: YearMonth,
        val days: List<CalendarDay>,
        val prediction: Prediction?,
        val hasData: Boolean
    )
}

sealed class CalendarDay {
    data object Empty : CalendarDay()

    data class Data(
        val date: LocalDate,
        val dayOfMonth: Int,
        val record: com.example.periodvibe.domain.model.DailyRecord?,
        val phase: com.example.periodvibe.domain.model.CyclePhase,
        val dayType: CalendarDayType,
        val isToday: Boolean,
        val isPredictedPeriod: Boolean,
        val isPredictedOvulation: Boolean,
        val isPredictedFertile: Boolean
    ) : CalendarDay()
}

enum class CalendarDayType {
    NORMAL,
    PERIOD,
    PREDICTED_PERIOD,
    OVULATION,
    FERTILE
}
