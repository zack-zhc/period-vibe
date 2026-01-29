package com.example.periodvibe.ui.applock

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.SecurityRepository
import com.example.periodvibe.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PinSetupUiState>(PinSetupUiState.Idle)
    val uiState: StateFlow<PinSetupUiState> = _uiState.asStateFlow()

    val pin = mutableStateOf("")
    val confirmPin = mutableStateOf("")

    fun onPinChange(newPin: String) {
        if (newPin.length <= 4) {
            pin.value = newPin
        }
    }

    fun onConfirmPinChange(newPin: String) {
        if (newPin.length <= 4) {
            confirmPin.value = newPin
        }
    }

    fun onSetPin() {
        if (pin.value.length < 4) {
            _uiState.value = PinSetupUiState.Error("PIN must be 4 digits")
            return
        }

        if (pin.value != confirmPin.value) {
            _uiState.value = PinSetupUiState.Error("PINs do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = PinSetupUiState.Loading
            securityRepository.savePin(pin.value)
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(appLockEnabled = true)
                settingsRepository.updateSettings(updatedSettings)
            }
            _uiState.value = PinSetupUiState.PinSet
        }
    }

    fun resetPin() {
        pin.value = ""
        confirmPin.value = ""
        _uiState.value = PinSetupUiState.Idle
    }

    fun resetErrorState() {
        _uiState.value = PinSetupUiState.Idle
    }
}

sealed class PinSetupUiState {
    object Idle : PinSetupUiState()
    object Loading : PinSetupUiState()
    object PinSet : PinSetupUiState()
    data class Error(val message: String) : PinSetupUiState()
}
