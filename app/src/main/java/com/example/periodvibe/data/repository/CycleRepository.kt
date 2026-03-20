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
import java.time.Period
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

    suspend fun getAllCyclesOnce(): List<Cycle> {
        return cycleDao.getAllCycles().first().map { cycleMapper.toDomain(it) }
    }

    suspend fun insertAllCycles(cycles: List<Cycle>): List<Long> {
        val entities = cycleMapper.toEntityList(cycles)
        return cycleDao.insertAllCycles(entities)
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

            // 如果当前活跃周期还没有结束日期，则以新周期开始日期的前一天作为结束日期
            val cycleToComplete = if (activeCycle.endDate == null) {
                activeCycle.complete(startDate.minusDays(1))
            } else {
                activeCycle
            }

            // 计算并设置 cycleLength：找到上一个已完成的周期，计算两个周期开始日期的间隔
            val allCycles = getAllCyclesOnce()
            val previousCompletedCycle = allCycles
                .filter { it.isCompleted && it.id != activeCycle.id }
                .maxByOrNull { it.startDate }

            val cycleLength = if (previousCompletedCycle != null) {
                Period.between(previousCompletedCycle.startDate, cycleToComplete.startDate).days
            } else {
                null
            }

            val updatedCycle = cycleToComplete
                .updatePeriodLength(periodLength ?: 0)
                .copy(cycleLength = cycleLength)

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
        val activeCycle = getActiveCycleBeforeDate(endDate) ?: return

        val records = getDailyRecordsByCycleId(activeCycle.id).first()
        val periodRecords = records.filter { it.isPeriod }

        // 找到最早的经期日期（如果没有则使用周期开始日期）
        val firstPeriodDate = periodRecords.minByOrNull { it.date }?.date ?: activeCycle.startDate

        // 确保结束日期不早于最早的经期日期
        val actualEndDate = if (endDate.isBefore(firstPeriodDate)) firstPeriodDate else endDate

        // 生成从 firstPeriodDate 到 actualEndDate 的所有日期
        val datesToFill = mutableListOf<LocalDate>()
        var currentDate = firstPeriodDate
        while (!currentDate.isAfter(actualEndDate)) {
            datesToFill.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        // 为缺失的日期创建经期记录
        val existingDates = periodRecords.map { it.date }.toSet()
        datesToFill.forEach { date ->
            if (!existingDates.contains(date)) {
                val newRecord = com.example.periodvibe.domain.model.DailyRecord(
                    date = date,
                    cycleId = activeCycle.id,
                    isPeriod = true,
                    flowLevel = null
                )
                saveDailyRecord(newRecord)
            }
        }

        // 重新获取更新后的记录并计算 periodLength
        val updatedRecords = getDailyRecordsByCycleId(activeCycle.id).first()
        val updatedPeriodRecords = updatedRecords.filter { it.isPeriod }
        val periodLength = if (updatedPeriodRecords.isNotEmpty()) {
            updatedPeriodRecords.size
        } else {
            null
        }

        val completedCycle = activeCycle.complete(actualEndDate).updatePeriodLength(periodLength ?: 0)
        updateCycle(completedCycle)
    }

    suspend fun getActiveCycleBeforeDate(endDate: LocalDate): Cycle? {
        val entity = cycleDao.getActiveCycleBeforeDate(endDate.toString())
        return entity?.let { cycleMapper.toDomain(it) }
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

    suspend fun deleteAllDailyRecords() {
        dailyRecordDao.deleteAllDailyRecords()
    }

    suspend fun getAllDailyRecordsOnce(): List<com.example.periodvibe.domain.model.DailyRecord> {
        return dailyRecordDao.getAllDailyRecords().first().map { dailyRecordMapper.toDomain(it) }
    }

    suspend fun insertAllDailyRecords(records: List<com.example.periodvibe.domain.model.DailyRecord>): List<Long> {
        val entities = dailyRecordMapper.toEntityList(records)
        return dailyRecordDao.insertAllDailyRecords(entities)
    }

    suspend fun getPreviousDayRecord(date: java.time.LocalDate): com.example.periodvibe.domain.model.DailyRecord? {
        val previousDate = date.minusDays(1)
        return getDailyRecordByDate(previousDate)
    }
}