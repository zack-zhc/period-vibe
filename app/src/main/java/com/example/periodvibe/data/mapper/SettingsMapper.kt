package com.example.periodvibe.data.mapper

import com.example.periodvibe.data.local.entity.SettingsEntity
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.domain.model.Settings.ThemeMode
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsMapper @Inject constructor() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun toDomain(entity: SettingsEntity): Settings {
        return Settings(
            cycleLengthDefault = entity.cycleLengthDefault,
            periodLengthDefault = entity.periodLengthDefault,
            cycleLengthRange = entity.cycleLengthMin..entity.cycleLengthMax,
            periodLengthRange = entity.periodLengthMin..entity.periodLengthMax,
            notificationEnabled = entity.notificationEnabled,
            notificationDaysBefore = entity.notificationDaysBefore,
            notificationTime = LocalTime.parse(entity.notificationTime, timeFormatter),
            themeMode = ThemeMode.valueOf(entity.themeMode),
            appLockEnabled = entity.appLockEnabled,
            privacyModeEnabled = entity.privacyModeEnabled,
            language = entity.language,
            onboardingVersion = entity.onboardingVersion
        )
    }

    fun toEntity(domain: Settings): SettingsEntity {
        return SettingsEntity(
            id = 1,
            cycleLengthDefault = domain.cycleLengthDefault,
            periodLengthDefault = domain.periodLengthDefault,
            cycleLengthMin = domain.cycleLengthRange.first,
            cycleLengthMax = domain.cycleLengthRange.last,
            periodLengthMin = domain.periodLengthRange.first,
            periodLengthMax = domain.periodLengthRange.last,
            notificationEnabled = domain.notificationEnabled,
            notificationDaysBefore = domain.notificationDaysBefore,
            notificationTime = domain.notificationTime.format(timeFormatter),
            themeMode = domain.themeMode.name,
            appLockEnabled = domain.appLockEnabled,
            privacyModeEnabled = domain.privacyModeEnabled,
            language = domain.language,
            onboardingVersion = domain.onboardingVersion,
            updatedAt = System.currentTimeMillis()
        )
    }
}
