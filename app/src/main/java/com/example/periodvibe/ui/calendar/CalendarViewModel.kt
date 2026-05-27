package com.example.periodvibe.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.usecase.GetCalendarDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
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

    private val _activeCycle = MutableStateFlow<com.example.periodvibe.domain.model.Cycle?>(null)
    val activeCycle: StateFlow<com.example.periodvibe.domain.model.Cycle?> = _activeCycle.asStateFlow()

    // 预加载的月份数量
    private val preloadCount = 2

    init {
        loadActiveCycle()
        // 初始加载当前月及前后各2个月的数据
        loadMonthsAround(YearMonth.now())
    }

    private fun loadMonthsAround(centerMonth: YearMonth) {
        val monthsToLoad = mutableListOf<YearMonth>()
        for (i in -preloadCount..preloadCount) {
            monthsToLoad.add(centerMonth.plusMonths(i.toLong()))
        }
        monthsToLoad.forEach { yearMonth ->
            loadMonthData(yearMonth)
        }
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        if (_calendarPages.value.containsKey(yearMonth)) {
            return
        }
        _calendarPages.value = _calendarPages.value.toMutableMap().also {
            it[yearMonth] = CalendarUiState.Loading
        }
        viewModelScope.launch {
            getCalendarDataUseCase(yearMonth).collect { data ->
                _calendarPages.value = _calendarPages.value.toMutableMap().also {
                    it[yearMonth] = CalendarUiState.Success(
                        yearMonth = data.yearMonth,
                        days = data.days,
                        prediction = data.prediction,
                        hasData = data.hasData
                    )
                }
            }
        }
    }

    fun onPageChanged(yearMonth: YearMonth) {
        if (_currentYearMonth.value != yearMonth) {
            _currentYearMonth.value = yearMonth
            _selectedDate.value = null
            // 预加载更多月份
            loadMonthsAround(yearMonth)
        }
    }

    private fun loadActiveCycle() {
        viewModelScope.launch {
            val cycle = cycleRepository.getActiveCycle()
            _activeCycle.value = cycle
        }
    }

    private fun loadActiveCycleForDate(date: LocalDate) {
        viewModelScope.launch {
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
                    // 刷新所有月份数据
                    _calendarPages.value = emptyMap()
                    loadMonthsAround(_currentYearMonth.value)
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
        }
    }

    fun saveRecord(
        date: LocalDate,
        mode: com.example.periodvibe.ui.home.RecordMode,
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?
    ) {
        viewModelScope.launch {
            saveRecordUseCase(date, mode, flowLevel)
                .onSuccess {
                    loadActiveCycle()
                    // 刷新所有月份数据
                    _calendarPages.value = emptyMap()
                    loadMonthsAround(_currentYearMonth.value)
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
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
