package com.example.periodvibe.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.usecase.GetCalendarDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getCalendarDataUseCase: GetCalendarDataUseCase,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _calendarData = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val calendarData: StateFlow<CalendarUiState> = _calendarData.asStateFlow()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _showEndCycleDialog = MutableStateFlow(false)
    val showEndCycleDialog: StateFlow<Boolean> = _showEndCycleDialog.asStateFlow()

    private val _showLegendDialog = MutableStateFlow(false)
    val showLegendDialog: StateFlow<Boolean> = _showLegendDialog.asStateFlow()

    private val _activeCycle = MutableStateFlow<com.example.periodvibe.domain.model.Cycle?>(null)
    val activeCycle: StateFlow<com.example.periodvibe.domain.model.Cycle?> = _activeCycle.asStateFlow()

    init {
        loadCalendarData()
        loadActiveCycle()
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            getCalendarDataUseCase(_currentYearMonth.value).collect { data ->
                _calendarData.value = CalendarUiState.Success(
                    yearMonth = data.yearMonth,
                    days = data.days,
                    prediction = data.prediction,
                    hasData = data.hasData
                )
            }
        }
    }

    private fun loadActiveCycle() {
        viewModelScope.launch {
            val cycle = cycleRepository.getActiveCycle()
            _activeCycle.value = cycle
        }
    }

    fun navigateToPreviousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
        _selectedDate.value = null
        loadCalendarData()
    }

    fun navigateToNextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
        _selectedDate.value = null
        loadCalendarData()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    fun navigateToToday() {
        _currentYearMonth.value = YearMonth.now()
        _selectedDate.value = LocalDate.now()
        loadCalendarData()
    }

    fun showEndCycleDialog() {
        _showEndCycleDialog.value = true
    }

    fun hideEndCycleDialog() {
        _showEndCycleDialog.value = false
    }

    fun showLegendDialog() {
        _showLegendDialog.value = true
    }

    fun hideLegendDialog() {
        _showLegendDialog.value = false
    }

    fun endCycle(endDate: LocalDate) {
        viewModelScope.launch {
            try {
                cycleRepository.endCurrentCycle(endDate)
                hideEndCycleDialog()
                clearSelectedDate()
                loadActiveCycle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveRecord(
        date: LocalDate,
        mode: com.example.periodvibe.ui.home.RecordMode,
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?,
        symptoms: List<com.example.periodvibe.domain.model.Symptom>,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = cycleRepository.getDailyRecordByDate(date)
                val activeCycle = cycleRepository.getActiveCycle()

                val isPeriod = when (mode) {
                    com.example.periodvibe.ui.home.RecordMode.NEW_CYCLE -> true
                    com.example.periodvibe.ui.home.RecordMode.SYMPTOM_ONLY -> false
                    com.example.periodvibe.ui.home.RecordMode.AUTO -> true
                }

                var targetCycle: com.example.periodvibe.domain.model.Cycle? = null

                when (mode) {
                    com.example.periodvibe.ui.home.RecordMode.NEW_CYCLE -> {
                        targetCycle = cycleRepository.startNewCycle(date)
                    }
                    com.example.periodvibe.ui.home.RecordMode.SYMPTOM_ONLY -> {
                        targetCycle = null
                    }
                    com.example.periodvibe.ui.home.RecordMode.AUTO -> {
                        if (activeCycle != null) {
                            targetCycle = activeCycle
                        } else {
                            targetCycle = cycleRepository.startNewCycle(date)
                        }
                    }
                }

                val record = if (existingRecord != null) {
                    existingRecord.copy(
                        isPeriod = isPeriod,
                        flowLevel = flowLevel,
                        symptoms = symptoms,
                        notes = notes,
                        cycleId = targetCycle?.id ?: existingRecord.cycleId
                    )
                } else {
                    com.example.periodvibe.domain.model.DailyRecord(
                        date = date,
                        cycleId = targetCycle?.id,
                        isPeriod = isPeriod,
                        flowLevel = flowLevel,
                        symptoms = symptoms,
                        notes = notes
                    )
                }

                if (existingRecord != null) {
                    cycleRepository.updateDailyRecord(record)
                } else {
                    cycleRepository.saveDailyRecord(record)
                }

                loadActiveCycle()
                loadCalendarData()
            } catch (e: Exception) {
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
