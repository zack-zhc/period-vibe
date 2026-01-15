package com.example.periodvibe.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "cycle_length_default")
    val cycleLengthDefault: Int,

    @ColumnInfo(name = "period_length_default")
    val periodLengthDefault: Int,

    @ColumnInfo(name = "cycle_length_min")
    val cycleLengthMin: Int,

    @ColumnInfo(name = "cycle_length_max")
    val cycleLengthMax: Int,

    @ColumnInfo(name = "period_length_min")
    val periodLengthMin: Int,

    @ColumnInfo(name = "period_length_max")
    val periodLengthMax: Int,

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean,

    @ColumnInfo(name = "notification_days_before")
    val notificationDaysBefore: Int,

    @ColumnInfo(name = "notification_time")
    val notificationTime: String,

    @ColumnInfo(name = "theme_mode")
    val themeMode: String,

    @ColumnInfo(name = "app_lock_enabled")
    val appLockEnabled: Boolean,

    @ColumnInfo(name = "privacy_mode_enabled")
    val privacyModeEnabled: Boolean,

    @ColumnInfo(name = "language")
    val language: String,

    @ColumnInfo(name = "onboarding_version")
    val onboardingVersion: Int,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
