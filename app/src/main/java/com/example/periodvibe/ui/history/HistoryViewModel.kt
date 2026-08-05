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

    private val _showDeleteDialog = MutableStateFlow<Long?>(null)
    val showDeleteDialog: StateFlow<Long?> = _showDeleteDialog.asStateFlow()

    private val _showDeleteRecordDialog = MutableStateFlow<Long?>(null)
    val showDeleteRecordDialog: StateFlow<Long?> = _showDeleteRecordDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow<com.example.periodvibe.domain.model.DailyRecord?>(null)
    val showEditDialog: StateFlow<com.example.periodvibe.domain.model.DailyRecord?> = _showEditDialog.asStateFlow()

    // 编辑模式状态
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // 多选选中的周期
    private val _selectedCycles = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCycles: StateFlow<Set<Long>> = _selectedCycles.asStateFlow()

    // 批量删除确认弹窗
    private val _showDeleteSelectedDialog = MutableStateFlow(false)
    val showDeleteSelectedDialog: StateFlow<Boolean> = _showDeleteSelectedDialog.asStateFlow()

    // 错误消息资源 ID，用于 Snackbar 反馈
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

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
                    avgCycleLength = data.avgCycleLength,
                    longestCycle = data.longestCycle,
                    shortestCycle = data.shortestCycle,
                    avgPeriodLength = data.avgPeriodLength
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
                _errorMessage.value = com.example.periodvibe.R.string.error_delete_failed
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
                _errorMessage.value = com.example.periodvibe.R.string.error_delete_failed
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
                _errorMessage.value = com.example.periodvibe.R.string.error_save_failed
            }
        }
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
        if (!_isEditMode.value) {
            _selectedCycles.value = emptySet()
        }
    }

    // 长按周期：进入编辑模式并选中该周期（替代直接弹删除确认，避免误触）
    fun enterEditModeWithSelection(cycleId: Long) {
        _isEditMode.value = true
        _selectedCycles.value = setOf(cycleId)
    }

    fun toggleCycleSelection(cycleId: Long) {
        val current = _selectedCycles.value.toMutableSet()
        if (current.contains(cycleId)) {
            current.remove(cycleId)
        } else {
            current.add(cycleId)
        }
        _selectedCycles.value = current
    }

    fun showDeleteSelectedDialog() {
        _showDeleteSelectedDialog.value = true
    }

    fun hideDeleteSelectedDialog() {
        _showDeleteSelectedDialog.value = false
    }

    fun deleteSelectedCycles() {
        viewModelScope.launch {
            val targets = _selectedCycles.value
            try {
                targets.forEach { cycleId ->
                    getHistoryDataUseCase.deleteCycle(cycleId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = com.example.periodvibe.R.string.error_delete_failed
            }
            _selectedCycles.value = emptySet()
            _isEditMode.value = false
            _showDeleteSelectedDialog.value = false
        }
    }

    fun clearSelection() {
        _selectedCycles.value = emptySet()
    }
}

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(
        val cycles: List<com.example.periodvibe.domain.usecase.CycleWithRecords>,
        val totalCycles: Int,
        val hasData: Boolean,
        val avgCycleLength: Int? = null,
        val longestCycle: Int? = null,
        val shortestCycle: Int? = null,
        val avgPeriodLength: Int? = null
    ) : HistoryUiState()
}
