package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import javax.inject.Inject

class EndCycleUseCase @Inject constructor(
    private val cycleRepository: CycleRepository
) {
    suspend operator fun invoke(endDate: java.time.LocalDate): Result<Unit> {
        return try {
            cycleRepository.endCurrentCycle(endDate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}