package com.example.periodvibe.ui.setup

import java.time.LocalDate

data class InitialSetupData(
    val lastPeriodStartDate: LocalDate? = null,
    val cycleLength: Int? = null,
    val periodLength: Int? = null
) {
    fun isValid(): Boolean {
        val cycleValid = cycleLength == null || cycleLength in 21..35
        val periodValid = periodLength == null || periodLength in 3..7
        return cycleValid && periodValid
    }

    fun hasCycleOrPeriodData(): Boolean {
        return cycleLength != null || periodLength != null
    }
}
