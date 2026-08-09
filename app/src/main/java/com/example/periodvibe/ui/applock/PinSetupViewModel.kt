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
    val verifyPin = mutableStateOf("")

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

    fun onVerifyPinChange(newPin: String) {
        if (newPin.length <= 4) {
            verifyPin.value = newPin
        }
    }

    fun resetVerifyPin() {
        verifyPin.value = ""
    }

    /** 验证当前 PIN（修改 PIN / 关闭应用锁场景），同步返回是否匹配 */
    fun verifyCurrentPin(): Boolean = securityRepository.getPin() == verifyPin.value

    fun onSetPin() {
        if (pin.value.length < 4) {
            _uiState.value = PinSetupUiState.Error(com.example.periodvibe.R.string.pin_error_length)
            return
        }

        if (pin.value != confirmPin.value) {
            _uiState.value = PinSetupUiState.Error(com.example.periodvibe.R.string.pin_error_mismatch)
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
    data class Error(val messageRes: Int) : PinSetupUiState()
}
