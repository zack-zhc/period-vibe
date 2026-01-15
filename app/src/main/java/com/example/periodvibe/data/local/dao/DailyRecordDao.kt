package com.example.periodvibe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.periodvibe.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllDailyRecords(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE id = :id")
    suspend fun getDailyRecordById(id: Long): DailyRecordEntity?

    @Query("SELECT * FROM daily_records WHERE date = :date")
    suspend fun getDailyRecordByDate(date: String): DailyRecordEntity?

    @Query("SELECT * FROM daily_records WHERE cycle_id = :cycleId")
    fun getDailyRecordsByCycleId(cycleId: Long): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE is_period = 1 ORDER BY date DESC")
    fun getPeriodRecords(): Flow<List<DailyRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecordEntity): Long

    @Update
    suspend fun updateDailyRecord(record: DailyRecordEntity)

    @Delete
    suspend fun deleteDailyRecord(record: DailyRecordEntity)

    @Query("DELETE FROM daily_records")
    suspend fun deleteAllDailyRecords()
}
