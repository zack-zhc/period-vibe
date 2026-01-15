package com.example.periodvibe.data.repository

import com.example.periodvibe.data.local.dao.CycleDao
import com.example.periodvibe.data.local.dao.DailyRecordDao
import com.example.periodvibe.data.mapper.CycleMapper
import com.example.periodvibe.data.mapper.DailyRecordMapper
import com.example.periodvibe.domain.model.Cycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val cycleDao: CycleDao,
    private val dailyRecordDao: DailyRecordDao,
    private val cycleMapper: CycleMapper,
    private val dailyRecordMapper: DailyRecordMapper
) {

    fun getAllCycles(): Flow<List<Cycle>> {
        return cycleDao.getAllCycles().map { entities ->
            entities.map { cycleMapper.toDomain(it) }
        }
    }

    suspend fun getLatestCycle(): Cycle? {
        val entity = cycleDao.getLatestCycle()
        return entity?.let { cycleMapper.toDomain(it) }
    }

    suspend fun getActiveCycle(): Cycle? {
        val entity = cycleDao.getActiveCycle()
        return entity?.let { cycleMapper.toDomain(it) }
    }

    suspend fun getCycleById(id: Long): Cycle? {
        val entity = cycleDao.getCycleById(id)
        return entity?.let { cycleMapper.toDomain(it) }
    }

    suspend fun insertCycle(cycle: Cycle): Long {
        val entity = cycleMapper.toEntity(cycle)
        return cycleDao.insertCycle(entity)
    }

    suspend fun updateCycle(cycle: Cycle) {
        val entity = cycleMapper.toEntity(cycle)
        cycleDao.updateCycle(entity)
    }

    suspend fun deleteCycle(cycle: Cycle) {
        val entity = cycleMapper.toEntity(cycle)
        cycleDao.deleteCycle(entity)
    }

    suspend fun deleteAllCycles() {
        cycleDao.deleteAllCycles()
    }

    suspend fun createInitialCycle(
        startDate: LocalDate,
        cycleLength: Int? = null,
        periodLength: Int? = null
    ): Cycle {
        val cycle = Cycle(
            startDate = startDate,
            endDate = null,
            cycleLength = cycleLength,
            periodLength = periodLength,
            isCompleted = false
        )
        val cycleId = insertCycle(cycle)
        return cycle.copy(id = cycleId)
    }

    suspend fun startNewCycle(startDate: LocalDate): Cycle {
        val activeCycle = getActiveCycle()
        
        if (activeCycle != null) {
            val records = getDailyRecordsByCycleId(activeCycle.id).first()
            val periodRecords = records.filter { it.isPeriod }
            val periodLength = if (periodRecords.isNotEmpty()) {
                periodRecords.size
            } else {
                null
            }
            
            val updatedCycle = activeCycle.updatePeriodLength(periodLength ?: 0)
            updateCycle(updatedCycle)
        }
        
        val newCycle = Cycle(
            startDate = startDate,
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false
        )
        val cycleId = insertCycle(newCycle)
        return newCycle.copy(id = cycleId)
    }

    suspend fun endCurrentCycle(endDate: LocalDate) {
        val activeCycle = getActiveCycle() ?: return
        
        val records = getDailyRecordsByCycleId(activeCycle.id).first()
        val periodRecords = records.filter { it.isPeriod }
        val periodLength = if (periodRecords.isNotEmpty()) {
            periodRecords.size
        } else {
            null
        }
        
        val completedCycle = activeCycle.complete(endDate).updatePeriodLength(periodLength ?: 0)
        updateCycle(completedCycle)
    }

    fun getAllDailyRecords(): Flow<List<com.example.periodvibe.domain.model.DailyRecord>> {
        return dailyRecordDao.getAllDailyRecords().map { entities ->
            entities.map { dailyRecordMapper.toDomain(it) }
        }
    }

    suspend fun getDailyRecordByDate(date: java.time.LocalDate): com.example.periodvibe.domain.model.DailyRecord? {
        val entity = dailyRecordDao.getDailyRecordByDate(date.toString())
        return entity?.let { dailyRecordMapper.toDomain(it) }
    }

    suspend fun getDailyRecordById(id: Long): com.example.periodvibe.domain.model.DailyRecord? {
        val entity = dailyRecordDao.getDailyRecordById(id)
        return entity?.let { dailyRecordMapper.toDomain(it) }
    }

    fun getDailyRecordsByCycleId(cycleId: Long): Flow<List<com.example.periodvibe.domain.model.DailyRecord>> {
        return dailyRecordDao.getDailyRecordsByCycleId(cycleId).map { entities ->
            entities.map { dailyRecordMapper.toDomain(it) }
        }
    }

    suspend fun saveDailyRecord(record: com.example.periodvibe.domain.model.DailyRecord): Long {
        val entity = dailyRecordMapper.toEntity(record)
        return dailyRecordDao.insertDailyRecord(entity)
    }

    suspend fun updateDailyRecord(record: com.example.periodvibe.domain.model.DailyRecord) {
        val entity = dailyRecordMapper.toEntity(record)
        dailyRecordDao.updateDailyRecord(entity)
    }

    suspend fun deleteDailyRecord(record: com.example.periodvibe.domain.model.DailyRecord) {
        val entity = dailyRecordMapper.toEntity(record)
        dailyRecordDao.deleteDailyRecord(entity)
    }

    suspend fun getPreviousDayRecord(date: java.time.LocalDate): com.example.periodvibe.domain.model.DailyRecord? {
        val previousDate = date.minusDays(1)
        return getDailyRecordByDate(previousDate)
    }
}