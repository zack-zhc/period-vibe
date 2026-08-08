package com.example.periodvibe.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.RecordMode
import com.example.periodvibe.domain.usecase.GetCalendarDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getCalendarDataUseCase: GetCalendarDataUseCase,
    private val cycleRepository: CycleRepository,
    private val saveRecordUseCase: com.example.periodvibe.domain.usecase.SaveRecordUseCase,
    private val endCycleUseCase: com.example.periodvibe.domain.usecase.EndCycleUseCase
) : ViewModel() {

    // 保存多个月份的日历数据
    private val _calendarPages = MutableStateFlow<Map<YearMonth, CalendarUiState>>(emptyMap())
    val calendarPages: StateFlow<Map<YearMonth, CalendarUiState>> = _calendarPages.asStateFlow()

    // 当前显示的月份（用于 Pager 的 key）
    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _showEndCycleDialog = MutableStateFlow(false)
    val showEndCycleDialog: StateFlow<Boolean> = _showEndCycleDialog.asStateFlow()

    // 新周期确认弹窗（与首页行为一致，避免误触直接结束当前周期）
    private val _showNewCycleConfirmation = MutableStateFlow(false)
    val showNewCycleConfirmation: StateFlow<Boolean> = _showNewCycleConfirmation.asStateFlow()

    private var pendingNewCycleDate: LocalDate? = null
    private var pendingNewCycleFlowLevel: FlowLevel? = null

    private val _activeCycle = MutableStateFlow<com.example.periodvibe.domain.model.Cycle?>(null)
    val activeCycle: StateFlow<com.example.periodvibe.domain.model.Cycle?> = _activeCycle.asStateFlow()

    // 错误消息资源 ID，用于 Snackbar 反馈
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    // 活动周期的加载 Job，避免快速切换日期时旧结果覆盖新结果
    private var activeCycleJob: Job? = null

    // 预加载的月份数量
    private val preloadCount = 2

    // 离当前月份超过该距离的月份会被移出缓存并取消加载，防止订阅和内存无限增长
    private val maxCachedDistance = preloadCount + 2

    // 各月份的加载 Job，用于滑动远离后取消订阅。
    // 使用线程安全容器：collect 回调（Room 后台线程）与 invokeOnCompletion 可能并发访问
    private val loadJobs = ConcurrentHashMap<YearMonth, Job>()

    init {
        loadActiveCycle()
        // 初始加载当前月及前后各2个月的数据
        loadMonthsAround(YearMonth.now())
    }

    private fun monthDistance(from: YearMonth, to: YearMonth): Int =
        (from.year - to.year) * 12 + (from.monthValue - to.monthValue)

    private fun isWithinCacheWindow(yearMonth: YearMonth): Boolean =
        kotlin.math.abs(monthDistance(yearMonth, _currentYearMonth.value)) <= maxCachedDistance

    private fun loadMonthsAround(centerMonth: YearMonth) {
        val monthsToLoad = mutableListOf<YearMonth>()
        for (i in -preloadCount..preloadCount) {
            monthsToLoad.add(centerMonth.plusMonths(i.toLong()))
        }
        monthsToLoad.forEach { yearMonth ->
            loadMonthData(yearMonth)
        }
    }

    private fun loadMonthData(yearMonth: YearMonth, forceReload: Boolean = false) {
        if (loadJobs.containsKey(yearMonth)) {
            return
        }
        if (!forceReload && _calendarPages.value.containsKey(yearMonth)) {
            return
        }
        // 刷新时保留旧数据，避免整页 Loading 闪烁
        if (_calendarPages.value[yearMonth] !is CalendarUiState.Success) {
            _calendarPages.value = _calendarPages.value + (yearMonth to CalendarUiState.Loading)
        }
        val job = viewModelScope.launch {
            getCalendarDataUseCase(yearMonth).collect { data ->
                if (!isWithinCacheWindow(yearMonth)) {
                    // 已被滑出缓存窗口（竞态：取消尚未生效），丢弃结果
                    loadJobs[yearMonth]?.cancel()
                    _calendarPages.value = _calendarPages.value - yearMonth
                    return@collect
                }
                _calendarPages.value = _calendarPages.value + (yearMonth to CalendarUiState.Success(
                    yearMonth = data.yearMonth,
                    days = data.days,
                    prediction = data.prediction,
                    hasData = data.hasData
                ))
            }
        }
        loadJobs[yearMonth] = job
        job.invokeOnCompletion {
            if (loadJobs[yearMonth] == job) {
                loadJobs.remove(yearMonth)
            }
        }
    }

    // 移除离当前月份过远的数据并取消对应订阅
    private fun evictFarMonths() {
        val staleMonths = _calendarPages.value.keys
            .filter { !isWithinCacheWindow(it) }
        if (staleMonths.isEmpty()) return
        staleMonths.forEach { yearMonth ->
            loadJobs.remove(yearMonth)?.cancel()
        }
        _calendarPages.value = _calendarPages.value.filterKeys { isWithinCacheWindow(it) }
    }

    fun onPageChanged(yearMonth: YearMonth) {
        if (_currentYearMonth.value != yearMonth) {
            _currentYearMonth.value = yearMonth
            _selectedDate.value = null
            // 预加载更多月份
            loadMonthsAround(yearMonth)
            evictFarMonths()
        }
    }

    private fun loadActiveCycle() {
        activeCycleJob?.cancel()
        activeCycleJob = viewModelScope.launch {
            val cycle = cycleRepository.getActiveCycle()
            _activeCycle.value = cycle
        }
    }

    private fun loadActiveCycleForDate(date: LocalDate) {
        activeCycleJob?.cancel()
        activeCycleJob = viewModelScope.launch {
            val cycle = cycleRepository.getActiveCycleBeforeDate(date)
            _activeCycle.value = cycle
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadActiveCycleForDate(date)
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    fun navigateToToday() {
        _currentYearMonth.value = YearMonth.now()
        _selectedDate.value = LocalDate.now()
        loadMonthsAround(YearMonth.now())
        evictFarMonths()
    }

    fun showEndCycleDialog() {
        _showEndCycleDialog.value = true
    }

    fun hideEndCycleDialog() {
        _showEndCycleDialog.value = false
    }

    fun endCycle(endDate: LocalDate) {
        viewModelScope.launch {
            endCycleUseCase(endDate)
                .onSuccess {
                    hideEndCycleDialog()
                    clearSelectedDate()
                    loadActiveCycle()
                    refreshAllCachedMonths()
                }
                .onFailure { e ->
                    e.printStackTrace()
                    _errorMessage.value = com.example.periodvibe.R.string.error_end_cycle_failed
                }
        }
    }

    fun saveRecord(
        date: LocalDate,
        mode: RecordMode,
        flowLevel: FlowLevel?,
        existingRecord: DailyRecord? = null
    ) {
        viewModelScope.launch {
            // 与首页一致：存在活动周期时，开始新周期需用户确认（会结束当前周期）
            if (mode == RecordMode.NEW_CYCLE && cycleRepository.getActiveCycle() != null) {
                pendingNewCycleDate = date
                pendingNewCycleFlowLevel = flowLevel
                _showNewCycleConfirmation.value = true
                return@launch
            }
            doSaveRecord(date, mode, flowLevel, existingRecord)
        }
    }

    fun confirmNewCycle() {
        val date = pendingNewCycleDate ?: return
        val flowLevel = pendingNewCycleFlowLevel
        viewModelScope.launch {
            doSaveRecord(date, RecordMode.NEW_CYCLE, flowLevel, null)
            _showNewCycleConfirmation.value = false
            pendingNewCycleDate = null
            pendingNewCycleFlowLevel = null
        }
    }

    fun cancelNewCycle() {
        _showNewCycleConfirmation.value = false
        pendingNewCycleDate = null
        pendingNewCycleFlowLevel = null
    }

    private suspend fun doSaveRecord(
        date: LocalDate,
        mode: RecordMode,
        flowLevel: FlowLevel?,
        existingRecord: DailyRecord?
    ) {
        saveRecordUseCase(date, mode, flowLevel, existingRecord)
            .onSuccess {
                loadActiveCycle()
                refreshAllCachedMonths()
            }
            .onFailure { e ->
                e.printStackTrace()
                _errorMessage.value = com.example.periodvibe.R.string.error_save_failed
            }
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    // 取消所有订阅并重新加载缓存窗口内的月份，保证数据最新
    // 保留已有 Success 数据，新数据到达后再替换，避免整页 Loading 闪烁
    fun refreshAllCachedMonths() {
        val monthsToReload = _calendarPages.value.keys.toList()
        // 先快照再取消：cancel() 可能让协程在 Room 后台线程完成取消，
        // invokeOnCompletion 会在该线程修改 loadJobs，迭代中取消会抛 ConcurrentModificationException
        val jobsToCancel = loadJobs.values.toList()
        loadJobs.clear()
        jobsToCancel.forEach { it.cancel() }
        monthsToReload.forEach { yearMonth ->
            loadMonthData(yearMonth, forceReload = true)
        }
    }
}

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(
        val yearMonth: java.time.YearMonth,
        val days: List<com.example.periodvibe.domain.usecase.CalendarDay>,
        val prediction: com.example.periodvibe.domain.model.Prediction?,
        val hasData: Boolean
    ) : CalendarUiState()
}
