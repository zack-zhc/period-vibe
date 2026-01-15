package com.example.periodvibe.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.usecase.GetHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class RecordMode {
    AUTO,
    NEW_CYCLE,
    SYMPTOM_ONLY
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _homeData = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeData: StateFlow<HomeUiState> = _homeData.asStateFlow()

    private val _showRecordSheet = MutableStateFlow(false)
    val showRecordSheet: StateFlow<Boolean> = _showRecordSheet.asStateFlow()

    private val _showNewCycleSheet = MutableStateFlow(false)
    val showNewCycleSheet: StateFlow<Boolean> = _showNewCycleSheet.asStateFlow()

    private val _showNewSymptomSheet = MutableStateFlow(false)
    val showNewSymptomSheet: StateFlow<Boolean> = _showNewSymptomSheet.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _recordMode = MutableStateFlow<RecordMode>(RecordMode.AUTO)
    val recordMode: StateFlow<RecordMode> = _recordMode.asStateFlow()

    private val _suggestedIsPeriod = MutableStateFlow(false)
    val suggestedIsPeriod: StateFlow<Boolean> = _suggestedIsPeriod.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            getHomeDataUseCase().collect { data ->
                _homeData.value = if (data.cycleInfo != null) {
                    HomeUiState.Success(
                        cycleDay = data.cycleInfo.dayInCycle,
                        daysUntilPeriod = data.cycleInfo.daysUntilNextPeriod ?: 0,
                        phase = data.cycleInfo.phase,
                        totalCycles = data.totalCycles,
                        hasData = data.hasData
                    )
                } else {
                    HomeUiState.NoData
                }
            }
        }
    }

    fun showRecordSheet(date: LocalDate = LocalDate.now()) {
        _selectedDate.value = date
        _recordMode.value = RecordMode.AUTO
        viewModelScope.launch {
            _suggestedIsPeriod.value = inferSuggestedIsPeriod(date, RecordMode.AUTO)
        }
        _showRecordSheet.value = true
    }

    fun showRecordSheetWithMode(date: LocalDate, mode: RecordMode) {
        _selectedDate.value = date
        _recordMode.value = mode
        viewModelScope.launch {
            _suggestedIsPeriod.value = inferSuggestedIsPeriod(date, mode)
        }
        _showRecordSheet.value = true
    }

    private suspend fun inferSuggestedIsPeriod(date: LocalDate, mode: RecordMode): Boolean {
        return when (mode) {
            RecordMode.NEW_CYCLE -> true
            RecordMode.SYMPTOM_ONLY -> false
            RecordMode.AUTO -> {
                val activeCycle = cycleRepository.getActiveCycle()
                if (activeCycle != null) {
                    true
                } else {
                    false
                }
            }
        }
    }

    fun hideRecordSheet() {
        _showRecordSheet.value = false
    }

    fun showNewCycleSheet(date: LocalDate = LocalDate.now()) {
        _selectedDate.value = date
        _showNewCycleSheet.value = true
    }

    fun hideNewCycleSheet() {
        _showNewCycleSheet.value = false
    }

    fun showNewSymptomSheet(date: LocalDate = LocalDate.now()) {
        _selectedDate.value = date
        _showNewSymptomSheet.value = true
    }

    fun hideNewSymptomSheet() {
        _showNewSymptomSheet.value = false
    }

    fun saveDailyRecord(
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?,
        symptoms: List<com.example.periodvibe.domain.model.Symptom>,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = cycleRepository.getDailyRecordByDate(_selectedDate.value)
                val activeCycle = cycleRepository.getActiveCycle()
                val recordMode = _recordMode.value

                val isPeriod = when (recordMode) {
                    RecordMode.NEW_CYCLE -> true
                    RecordMode.SYMPTOM_ONLY -> false
                    RecordMode.AUTO -> true
                }

                var targetCycle: com.example.periodvibe.domain.model.Cycle? = null

                when (recordMode) {
                    RecordMode.NEW_CYCLE -> {
                        targetCycle = cycleRepository.startNewCycle(_selectedDate.value)
                    }
                    RecordMode.SYMPTOM_ONLY -> {
                        targetCycle = null
                    }
                    RecordMode.AUTO -> {
                        if (activeCycle != null) {
                            targetCycle = activeCycle
                        } else {
                            targetCycle = cycleRepository.startNewCycle(_selectedDate.value)
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
                        date = _selectedDate.value,
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

                hideRecordSheet()
                refresh()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveNewCycle(
        flowLevel: com.example.periodvibe.domain.model.FlowLevel?,
        symptoms: List<com.example.periodvibe.domain.model.Symptom>,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = cycleRepository.getDailyRecordByDate(_selectedDate.value)
                val targetCycle = cycleRepository.startNewCycle(_selectedDate.value)

                val record = if (existingRecord != null) {
                    existingRecord.copy(
                        isPeriod = true,
                        flowLevel = flowLevel,
                        symptoms = symptoms,
                        notes = notes,
                        cycleId = targetCycle.id
                    )
                } else {
                    com.example.periodvibe.domain.model.DailyRecord(
                        date = _selectedDate.value,
                        cycleId = targetCycle.id,
                        isPeriod = true,
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

                hideNewCycleSheet()
                refresh()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveNewSymptom(
        symptoms: List<com.example.periodvibe.domain.model.Symptom>,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = cycleRepository.getDailyRecordByDate(_selectedDate.value)

                val record = if (existingRecord != null) {
                    existingRecord.copy(
                        isPeriod = false,
                        flowLevel = null,
                        symptoms = symptoms,
                        notes = notes
                    )
                } else {
                    com.example.periodvibe.domain.model.DailyRecord(
                        date = _selectedDate.value,
                        cycleId = null,
                        isPeriod = false,
                        flowLevel = null,
                        symptoms = symptoms,
                        notes = notes
                    )
                }

                if (existingRecord != null) {
                    cycleRepository.updateDailyRecord(record)
                } else {
                    cycleRepository.saveDailyRecord(record)
                }

                hideNewSymptomSheet()
                refresh()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        loadHomeData()
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
        val hasData: Boolean
    ) : HomeUiState()
}