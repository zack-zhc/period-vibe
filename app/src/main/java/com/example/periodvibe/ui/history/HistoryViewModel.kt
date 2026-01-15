package com.example.periodvibe.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.domain.usecase.GetHistoryDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryDataUseCase: GetHistoryDataUseCase
) : ViewModel() {

    private val _historyData = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val historyData: StateFlow<HistoryUiState> = _historyData.asStateFlow()

    private val _selectedCycleId = MutableStateFlow<Long?>(null)
    val selectedCycleId: StateFlow<Long?> = _selectedCycleId.asStateFlow()

    private val _selectedRecordId = MutableStateFlow<Long?>(null)
    val selectedRecordId: StateFlow<Long?> = _selectedRecordId.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<Long?>(null)
    val showDeleteDialog: StateFlow<Long?> = _showDeleteDialog.asStateFlow()

    private val _showDeleteRecordDialog = MutableStateFlow<Long?>(null)
    val showDeleteRecordDialog: StateFlow<Long?> = _showDeleteRecordDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow<com.example.periodvibe.domain.model.DailyRecord?>(null)
    val showEditDialog: StateFlow<com.example.periodvibe.domain.model.DailyRecord?> = _showEditDialog.asStateFlow()

    init {
        loadHistoryData()
    }

    private fun loadHistoryData() {
        viewModelScope.launch {
            getHistoryDataUseCase().collect { data ->
                _historyData.value = HistoryUiState.Success(
                    cycles = data.cycles,
                    totalCycles = data.totalCycles,
                    hasData = data.hasData,
                    unassociatedRecords = data.unassociatedRecords
                )
            }
        }
    }

    fun selectCycle(cycleId: Long) {
        _selectedCycleId.value = cycleId
    }

    fun deselectCycle() {
        _selectedCycleId.value = null
    }

    fun selectRecord(recordId: Long) {
        _selectedRecordId.value = recordId
    }

    fun deselectRecord() {
        _selectedRecordId.value = null
    }

    fun showDeleteDialog(cycleId: Long) {
        _showDeleteDialog.value = cycleId
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = null
    }

    fun showDeleteRecordDialog(recordId: Long) {
        _showDeleteRecordDialog.value = recordId
    }

    fun hideDeleteRecordDialog() {
        _showDeleteRecordDialog.value = null
    }

    fun showEditDialog(record: com.example.periodvibe.domain.model.DailyRecord) {
        _showEditDialog.value = record
    }

    fun hideEditDialog() {
        _showEditDialog.value = null
    }

    fun deleteCycle(cycleId: Long) {
        viewModelScope.launch {
            try {
                getHistoryDataUseCase.deleteCycle(cycleId)
                _showDeleteDialog.value = null
                if (_selectedCycleId.value == cycleId) {
                    _selectedCycleId.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteDailyRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                getHistoryDataUseCase.deleteDailyRecord(recordId)
                _showDeleteRecordDialog.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateDailyRecord(record: com.example.periodvibe.domain.model.DailyRecord) {
        viewModelScope.launch {
            try {
                getHistoryDataUseCase.updateDailyRecord(record)
                _showEditDialog.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        loadHistoryData()
    }
}

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(
        val cycles: List<com.example.periodvibe.domain.usecase.CycleWithRecords>,
        val totalCycles: Int,
        val hasData: Boolean,
        val unassociatedRecords: List<com.example.periodvibe.domain.model.DailyRecord> = emptyList()
    ) : HistoryUiState()
}
