package com.example.periodvibe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.periodvibe.data.local.converter.LocalDateConverter
import com.example.periodvibe.data.local.converter.LocalDateTimeConverter
import com.example.periodvibe.data.local.dao.CycleDao
import com.example.periodvibe.data.local.dao.DailyRecordDao
import com.example.periodvibe.data.local.dao.NotificationDao
import com.example.periodvibe.data.local.dao.SettingsDao
import com.example.periodvibe.data.local.entity.CycleEntity
import com.example.periodvibe.data.local.entity.DailyRecordEntity
import com.example.periodvibe.data.local.entity.NotificationEntity
import com.example.periodvibe.data.local.entity.SettingsEntity

@Database(
    entities = [
        CycleEntity::class,
        DailyRecordEntity::class,
        NotificationEntity::class,
        SettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    LocalDateTimeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun notificationDao(): NotificationDao
    abstract fun settingsDao(): SettingsDao
}
