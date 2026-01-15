package com.example.periodvibe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.periodvibe.data.local.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycles ORDER BY start_date DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles WHERE id = :id")
    suspend fun getCycleById(id: Long): CycleEntity?

    @Query("SELECT * FROM cycles WHERE is_completed = 0 ORDER BY start_date DESC LIMIT 1")
    suspend fun getActiveCycle(): CycleEntity?

    @Query("SELECT * FROM cycles ORDER BY start_date DESC LIMIT 1")
    suspend fun getLatestCycle(): CycleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    @Delete
    suspend fun deleteCycle(cycle: CycleEntity)

    @Query("DELETE FROM cycles")
    suspend fun deleteAllCycles()
}
