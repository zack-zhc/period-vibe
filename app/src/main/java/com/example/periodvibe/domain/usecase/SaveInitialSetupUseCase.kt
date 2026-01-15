package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.Cycle
import java.time.LocalDate
import javax.inject.Inject

class SaveInitialSetupUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(
        lastPeriodStartDate: LocalDate?,
        cycleLength: Int,
        periodLength: Int
    ): Cycle? {
        settingsRepository.saveInitialSettings(
            cycleLength = cycleLength,
            periodLength = periodLength
        )

        return if (lastPeriodStartDate != null) {
            cycleRepository.createInitialCycle(
                startDate = lastPeriodStartDate,
                cycleLength = null,
                periodLength = null
            )
        } else {
            null
        }
    }
}
