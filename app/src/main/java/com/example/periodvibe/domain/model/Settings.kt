package com.example.periodvibe.domain.model

import java.time.LocalTime

data class Settings(
    val autoCalculateCycle: Boolean = true,
    val cycleLengthDefault: Int = 28,
    val periodLengthDefault: Int = 5,
    val cycleLengthRange: IntRange = 21..35,
    val periodLengthRange: IntRange = 3..7,
    val notificationEnabled: Boolean = true,
    val notificationDaysBefore: Int = 3,
    val notificationTime: LocalTime = LocalTime.of(9, 0),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appLockEnabled: Boolean = false,
    val privacyModeEnabled: Boolean = false,
    val language: String = "zh",
    val onboardingVersion: Int = 0
) {
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    fun updateNotificationSettings(
        enabled: Boolean,
        daysBefore: Int,
        time: LocalTime
    ): Settings {
        return copy(
            notificationEnabled = enabled,
            notificationDaysBefore = daysBefore,
            notificationTime = time
        )
    }

    fun updateThemeMode(mode: ThemeMode): Settings {
        return copy(themeMode = mode)
    }

    fun updateCycleParameters(
        cycleLength: Int,
        periodLength: Int
    ): Settings {
        return copy(
            cycleLengthDefault = cycleLength,
            periodLengthDefault = periodLength
        )
    }
}
