package com.example.periodvibe.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.periodvibe.data.local.AppDatabase
import com.example.periodvibe.data.local.dao.CycleDao
import com.example.periodvibe.data.local.dao.DailyRecordDao
import com.example.periodvibe.data.local.dao.NotificationDao
import com.example.periodvibe.data.local.dao.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE settings ADD COLUMN onboarding_version INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE cycles_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT,
                    cycle_length INTEGER,
                    period_length INTEGER,
                    average_flow_level TEXT,
                    is_completed INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """.trimIndent()
            )
            
            database.execSQL(
                """
                INSERT INTO cycles_new (id, start_date, end_date, cycle_length, period_length, average_flow_level, created_at, updated_at)
                SELECT id, start_date, end_date, cycle_length, period_length, average_flow_level, created_at, updated_at
                FROM cycles
                """.trimIndent()
            )
            
            database.execSQL("DROP TABLE cycles")
            database.execSQL("ALTER TABLE cycles_new RENAME TO cycles")
            
            database.execSQL("CREATE INDEX IF NOT EXISTS index_cycles_start_date ON cycles(start_date)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_cycles_end_date ON cycles(end_date)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE settings ADD COLUMN auto_calculate_cycle INTEGER NOT NULL DEFAULT 1"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "period_vibe_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
         .build()
    }

    @Provides
    fun provideCycleDao(database: AppDatabase): CycleDao {
        return database.cycleDao()
    }

    @Provides
    fun provideDailyRecordDao(database: AppDatabase): DailyRecordDao {
        return database.dailyRecordDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }
}
