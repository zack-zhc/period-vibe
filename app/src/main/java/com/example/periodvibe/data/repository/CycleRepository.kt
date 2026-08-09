package com.example.periodvibe.data.repository

import com.example.periodvibe.data.local.dao.CycleDao
import com.example.periodvibe.data.local.dao.DailyRecordDao
import com.example.periodvibe.data.mapper.CycleMapper
import com.example.periodvibe.data.mapper.DailyRecordMapper
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.ui.widget.WidgetUpdater
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val cycleDao: CycleDao,
    private val dailyRecordDao: DailyRecordDao,
    private val cycleMapper: CycleMapper,
    private val dailyRecordMapper: DailyRecordMapper,
    private val widgetUpdater: WidgetUpdater
) {

    /** 数据变更后刷新桌面小组件（内部已容错，不影响主流程） */
    private suspend fun notifyWidgetChanged(op: String) {
        Log.d(TAG, "notifyWidgetChanged: op=$op")
        widgetUpdater.refresh()
    }

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
        val id = cycleDao.insertCycle(entity)
        Log.d(TAG, "insertCycle: id=$id startDate=${cycle.startDate} isCompleted=${cycle.isCompleted}")
        notifyWidgetChanged("insertCycle")
        return id
    }

    suspend fun updateCycle(cycle: Cycle) {
        val entity = cycleMapper.toEntity(cycle)
        cycleDao.updateCycle(entity)
        Log.d(TAG, "updateCycle: id=${cycle.id} startDate=${cycle.startDate} endDate=${cycle.endDate} cycleLength=${cycle.cycleLength}")
        notifyWidgetChanged("updateCycle")
    }

    suspend fun deleteCycle(cycle: Cycle) {
        val entity = cycleMapper.toEntity(cycle)
        cycleDao.deleteCycle(entity)
        Log.d(TAG, "deleteCycle: id=${cycle.id}")
        notifyWidgetChanged("deleteCycle")
    }

    suspend fun deleteAllCycles() {
        cycleDao.deleteAllCycles()
        Log.d(TAG, "deleteAllCycles")
        notifyWidgetChanged("deleteAllCycles")
    }

    suspend fun getAllCyclesOnce(): List<Cycle> {
        return cycleDao.getAllCycles().first().map { cycleMapper.toDomain(it) }
    }

    suspend fun insertAllCycles(cycles: List<Cycle>): List<Long> {
        val entities = cycleMapper.toEntityList(cycles)
        val ids = cycleDao.insertAllCycles(entities)
        Log.d(TAG, "insertAllCycles: count=${cycles.size}")
        notifyWidgetChanged("insertAllCycles")
        return ids
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

            val updatedCycle = cycleToComplete
                .updatePeriodLength(periodLength ?: 0)

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

        // 在新周期创建后，更新上一个周期的 cycleLength 为：新周期开始日 - 上一个周期开始日
        if (activeCycle != null) {
            val allCyclesAfterNew = getAllCyclesOnce()
            val updatedPreviousCycle = allCyclesAfterNew.find { it.id == activeCycle.id }
            if (updatedPreviousCycle != null) {
                val cycleLength = Period.between(activeCycle.startDate, startDate).days
                val finalCycle = updatedPreviousCycle.copy(cycleLength = cycleLength)
                updateCycle(finalCycle)
            }
        }

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
        val entity = cycleDao.getActiveCycleBeforeDate(endDate)
        return entity?.let { cycleMapper.toDomain(it) }
    }

    /**
     * 获取覆盖指定日期（start_date <= date <= end_date，未完成周期视为无限延伸）的最新周期
     */
    suspend fun getCycleForDate(date: LocalDate): Cycle? {
        val entity = cycleDao.getCycleForDate(date)
        return entity?.let { cycleMapper.toDomain(it) }
    }

    /**
     * 按日期重新归属记录到对应周期；找不到时保留原 cycleId
     */
    suspend fun reassignCycleForDate(record: com.example.periodvibe.domain.model.DailyRecord): com.example.periodvibe.domain.model.DailyRecord {
        val newCycleId = getAllCyclesOnce()
            .firstOrNull { cycle ->
                !record.date.isBefore(cycle.startDate) &&
                    (cycle.endDate == null || !record.date.isAfter(cycle.endDate))
            }
            ?.id
            ?: record.cycleId
        return record.copy(cycleId = newCycleId)
    }

    fun getAllDailyRecords(): Flow<List<com.example.periodvibe.domain.model.DailyRecord>> {
        return dailyRecordDao.getAllDailyRecords().map { entities ->
            entities.map { dailyRecordMapper.toDomain(it) }
        }
    }

    suspend fun getDailyRecordByDate(date: java.time.LocalDate): com.example.periodvibe.domain.model.DailyRecord? {
        val entity = dailyRecordDao.getDailyRecordByDate(date)
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
        val id = dailyRecordDao.insertDailyRecord(entity)
        Log.d(TAG, "saveDailyRecord: id=$id date=${record.date} isPeriod=${record.isPeriod} flow=${record.flowLevel} cycleId=${record.cycleId}")
        notifyWidgetChanged("saveDailyRecord")
        return id
    }

    suspend fun updateDailyRecord(record: com.example.periodvibe.domain.model.DailyRecord) {
        val entity = dailyRecordMapper.toEntity(record.copy(updatedAt = LocalDateTime.now()))
        dailyRecordDao.updateDailyRecord(entity)
        Log.d(TAG, "updateDailyRecord: id=${record.id} date=${record.date} isPeriod=${record.isPeriod} flow=${record.flowLevel} cycleId=${record.cycleId}")
        notifyWidgetChanged("updateDailyRecord")
    }

    suspend fun deleteDailyRecord(record: com.example.periodvibe.domain.model.DailyRecord) {
        val entity = dailyRecordMapper.toEntity(record)
        dailyRecordDao.deleteDailyRecord(entity)
        Log.d(TAG, "deleteDailyRecord: id=${record.id} date=${record.date}")
        notifyWidgetChanged("deleteDailyRecord")
    }

    suspend fun deleteCycles(cycles: List<Cycle>): Int {
        val entities = cycles.map { cycleMapper.toEntity(it) }
        val count = cycleDao.deleteCycles(entities)
        Log.d(TAG, "deleteCycles: count=${cycles.size} ids=${cycles.map { it.id }}")
        notifyWidgetChanged("deleteCycles")
        return count
    }

    suspend fun deleteAllDailyRecords() {
        dailyRecordDao.deleteAllDailyRecords()
        Log.d(TAG, "deleteAllDailyRecords")
        notifyWidgetChanged("deleteAllDailyRecords")
    }

    suspend fun getAllDailyRecordsOnce(): List<com.example.periodvibe.domain.model.DailyRecord> {
        return dailyRecordDao.getAllDailyRecords().first().map { dailyRecordMapper.toDomain(it) }
    }

    suspend fun insertAllDailyRecords(records: List<com.example.periodvibe.domain.model.DailyRecord>): List<Long> {
        val entities = dailyRecordMapper.toEntityList(records)
        val ids = dailyRecordDao.insertAllDailyRecords(entities)
        Log.d(TAG, "insertAllDailyRecords: count=${records.size}")
        notifyWidgetChanged("insertAllDailyRecords")
        return ids
    }

    suspend fun getPreviousDayRecord(date: java.time.LocalDate): com.example.periodvibe.domain.model.DailyRecord? {
        val previousDate = date.minusDays(1)
        return getDailyRecordByDate(previousDate)
    }

    private companion object {
        const val TAG = "PV-LOG"
    }
}