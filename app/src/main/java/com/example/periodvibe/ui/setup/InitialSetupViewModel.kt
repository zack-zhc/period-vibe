package com.example.periodvibe.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.domain.usecase.SaveInitialSetupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InitialSetupViewModel @Inject constructor(
    private val saveInitialSetupUseCase: SaveInitialSetupUseCase
) : ViewModel() {

    private val _setupData = MutableStateFlow(InitialSetupData())
    val setupData: StateFlow<InitialSetupData> = _setupData.asStateFlow()

    private val _onComplete = MutableStateFlow(false)
    val onComplete: StateFlow<Boolean> = _onComplete.asStateFlow()

    fun updateLastPeriodStartDate(date: LocalDate) {
        _setupData.value = _setupData.value.copy(lastPeriodStartDate = date)
    }

    fun clearLastPeriodStartDate() {
        _setupData.value = _setupData.value.copy(lastPeriodStartDate = null)
    }

    fun updateCycleLength(length: Int) {
        if (length in 21..35) {
            _setupData.value = _setupData.value.copy(cycleLength = length)
        }
    }

    fun updatePeriodLength(length: Int) {
        if (length in 3..7) {
            _setupData.value = _setupData.value.copy(periodLength = length)
        }
    }

    fun onComplete() {
        viewModelScope.launch {
            if (_setupData.value.isValid()) {
                saveInitialSetupUseCase(
                    lastPeriodStartDate = _setupData.value.lastPeriodStartDate,
                    cycleLength = _setupData.value.cycleLength,
                    periodLength = _setupData.value.periodLength
                )
                _onComplete.value = true
            }
        }
    }

    fun isValid(): Boolean {
        return _setupData.value.isValid()
    }
}
