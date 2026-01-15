package com.example.periodvibe.ui.setup

import java.time.LocalDate

data class InitialSetupData(
    val lastPeriodStartDate: LocalDate? = null,
    val cycleLength: Int = 28,
    val periodLength: Int = 5
) {
    fun isValid(): Boolean {
        return cycleLength in 21..35 &&
                periodLength in 3..7
    }
}
