package com.example.periodvibe.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.RecordMode
import com.example.periodvibe.domain.usecase.GetHomeDataUseCase
import com.example.periodvibe.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val cycleRepository: CycleRepository,
    private val saveRecordUseCase: com.example.periodvibe.domain.usecase.SaveRecordUseCase,
    private val endCycleUseCase: com.example.periodvibe.domain.usecase.EndCycleUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _homeData = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeData: StateFlow<HomeUiState> = _homeData.asStateFlow()

    private val _showRecordSheet = MutableStateFlow(false)
    val showRecordSheet: StateFlow<Boolean> = _showRecordSheet.asStateFlow()

    private val _showNewCycleSheet = MutableStateFlow(false)
    val showNewCycleSheet: StateFlow<Boolean> = _showNewCycleSheet.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _recordMode = MutableStateFlow<RecordMode>(RecordMode.AUTO)
    val recordMode: StateFlow<RecordMode> = _recordMode.asStateFlow()

    private val _existingRecord = MutableStateFlow<com.example.periodvibe.domain.model.DailyRecord?>(null)
    val existingRecord: StateFlow<com.example.periodvibe.domain.model.DailyRecord?> = _existingRecord.asStateFlow()

    private val _showEndCycleMenu = MutableStateFlow(false)
    val showEndCycleMenu: StateFlow<Boolean> = _showEndCycleMenu.asStateFlow()

    private val _showNewCycleConfirmation = MutableStateFlow(false)
    val showNewCycleConfirmation: StateFlow<Boolean> = _showNewCycleConfirmation.asStateFlow()

    private var pendingNewCycleDate: LocalDate? = null
    private var pendingNewCycleFlowLevel: FlowLevel? = null

    private var loadJob: Job? = null

    // 错误消息资源 ID，用于 Snackbar 反馈
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getHomeDataUseCase().collect { data ->
                _homeData.value = if (data.cycleInfo != null) {
                    HomeUiState.Success(
                        cycleDay = data.cycleInfo.dayInCycle,
                        daysUntilPeriod = data.cycleInfo.daysUntilNextPeriod ?: 0,
                        phase = data.cycleInfo.phase,
                        totalCycles = data.totalCycles,
                        hasData = data.hasData,
                        hasCurrentCycle = data.cycleInfo.cycle.isCurrentCycle,
                        cycleLength = data.cycleLength,
                        daysUntilNextPhase = data.daysUntilNextPhase,
                        nextPhaseName = data.nextPhaseName,
                        ovulationDate = data.ovulationDate
                    )
                } else {
                    HomeUiState.NoData
                }
            }
        }
    }

    fun showRecordSheet(date: LocalDate = LocalDate.now()) {
        _selectedDate.value = date
        viewModelScope.launch {
            val record = cycleRepository.getDailyRecordByDate(date)
            _existingRecord.value = record
            _recordMode.value = if (record != null) RecordMode.EDIT else RecordMode.AUTO
        }
        _showRecordSheet.value = true
    }

    fun hideRecordSheet() {
        _showRecordSheet.value = false
        _existingRecord.value = null
    }

    fun showNewCycleSheet(date: LocalDate = LocalDate.now()) {
        _selectedDate.value = date
        _existingRecord.value = null // 新周期不应该显示为编辑记录
        _showNewCycleSheet.value = true
    }

    fun hideNewCycleSheet() {
        _showNewCycleSheet.value = false
        _existingRecord.value = null
    }

    fun saveDailyRecord(
        date: LocalDate,
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?
    ) {
        viewModelScope.launch {
            saveRecordUseCase(date, _recordMode.value, flowLevel, _existingRecord.value)
                .onSuccess {
                    hideRecordSheet()
                    refresh()
                    notificationScheduler.rescheduleAllNotifications()
                }
                .onFailure { e ->
                    e.printStackTrace()
                    _errorMessage.value = com.example.periodvibe.R.string.error_save_failed
                }
        }
    }

    fun saveNewCycle(
        date: LocalDate,
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?
    ) {
        viewModelScope.launch {
            val hasActiveCycle = cycleRepository.getActiveCycle() != null
            if (hasActiveCycle) {
                pendingNewCycleDate = date
                pendingNewCycleFlowLevel = flowLevel
                _showNewCycleConfirmation.value = true
            } else {
                saveNewCycleInternal(date, flowLevel)
            }
        }
    }

    fun confirmNewCycle() {
        val date = pendingNewCycleDate ?: return
        val flowLevel = pendingNewCycleFlowLevel
        viewModelScope.launch {
            saveNewCycleInternal(date, flowLevel)
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

    private suspend fun saveNewCycleInternal(
        date: LocalDate,
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?
    ) {
        saveRecordUseCase(date, RecordMode.NEW_CYCLE, flowLevel)
            .onSuccess {
                hideNewCycleSheet()
                refresh()
                notificationScheduler.rescheduleAllNotifications()
            }
            .onFailure { e ->
                e.printStackTrace()
                _errorMessage.value = com.example.periodvibe.R.string.error_save_failed
            }
    }

    fun showEndCycleMenu() {
        _showEndCycleMenu.value = true
    }

    fun hideEndCycleMenu() {
        _showEndCycleMenu.value = false
    }

    fun endCycle() {
        viewModelScope.launch {
            endCycleUseCase(java.time.LocalDate.now())
                .onSuccess {
                    hideEndCycleMenu()
                    refresh()
                    notificationScheduler.rescheduleAllNotifications()
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
        }
    }

    fun refresh() {
        loadHomeData()
    }

    fun consumeError() {
        _errorMessage.value = null
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object NoData : HomeUiState()
    data class Success(
        val cycleDay: Int,
        val daysUntilPeriod: Int,
        val phase: com.example.periodvibe.domain.model.CyclePhase,
        val totalCycles: Int,
        val hasData: Boolean,
        val hasCurrentCycle: Boolean,
        val cycleLength: Int,
        val daysUntilNextPhase: Int,
        val nextPhaseName: String,
        val ovulationDate: java.time.LocalDate?
    ) : HomeUiState()
}
