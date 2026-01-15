package com.example.periodvibe.domain.model

data class CycleInfo(
    val cycle: Cycle,
    val phase: CyclePhase,
    val dayInCycle: Int,
    val daysUntilNextPeriod: Int?,
    val prediction: Prediction?
) {
    val isPeriod: Boolean
        get() = phase == CyclePhase.MENSTRATION

    val isOvulation: Boolean
        get() = phase == CyclePhase.OVULATION

    val isFertile: Boolean
        get() = phase == CyclePhase.FERTILE
}
