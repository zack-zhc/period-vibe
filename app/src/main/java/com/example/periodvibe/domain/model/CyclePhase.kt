package com.example.periodvibe.domain.model

import java.time.LocalDate
import java.time.Period

enum class CyclePhase(
    val displayName: String,
    val color: String,
    val description: String
) {
    MENSTRATION("经期", "#FF6B6B", "生理期"),
    FOLLICULAR("卵泡期", "#95E1D3", "卵泡发育期"),
    OVULATION("排卵期", "#4ECDC4", "排卵日前后"),
    LUTEAL("黄体期", "#FFB6C1", "黄体形成期"),
    FERTILE("易孕期", "#FFB6C1", "易受孕期"),
    SAFE("安全期", "#95E1D3", "不易受孕期");

    companion object {
        fun fromDate(
            date: LocalDate,
            prediction: Prediction?,
            currentCycle: Cycle?
        ): CyclePhase {
            if (currentCycle != null && currentCycle.isCurrentCycle) {
                val cycleDay = Period.between(currentCycle.startDate, date).days
                val periodLength = currentCycle.periodLength ?: 5
                val cycleLength = currentCycle.cycleLength ?: 28
                
                return when {
                    cycleDay < periodLength -> MENSTRATION
                    cycleDay < cycleLength - 14 - 3 -> FOLLICULAR
                    cycleDay < cycleLength - 14 + 3 -> OVULATION
                    else -> LUTEAL
                }
            }

            prediction?.let {
                return when {
                    date in it.fertileWindow -> FERTILE
                    date in it.ovulationWindow -> OVULATION
                    date in it.nextPeriodStart..it.nextPeriodEnd -> MENSTRATION
                    else -> SAFE
                }
            }

            return SAFE
        }
    }
}
