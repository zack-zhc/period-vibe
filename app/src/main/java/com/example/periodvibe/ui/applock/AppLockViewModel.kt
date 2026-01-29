package com.example.periodvibe.ui.applock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppLockUiState>(AppLockUiState.Idle)
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    val pin = mutableStateOf("")

    fun onPinChange(newPin: String) {
        pin.value = newPin
    }

    fun onPinEntered() {
        viewModelScope.launch {
            val storedPin = securityRepository.getPin()
            if (pin.value == storedPin) {
                _uiState.value = AppLockUiState.Unlocked
            } else {
                _uiState.value = AppLockUiState.Error("Invalid PIN")
            }
        }
    }

    fun hasPin(): Boolean {
        return securityRepository.hasPin()
    }

    fun setPin(newPin: String) {
        securityRepository.savePin(newPin)
    }

    fun showBiometricPrompt(activity: FragmentActivity) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricManager = BiometricManager.from(activity)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for Period Vibe")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Use account password")
                    .build()

                val biometricPrompt = BiometricPrompt(activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            _uiState.value = AppLockUiState.Unlocked
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            _uiState.value = AppLockUiState.Error(errString.toString())
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            _uiState.value = AppLockUiState.Error("Authentication failed")
                        }
                    })

                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                // Biometric not available, fallback to PIN
            }
        }
    }
}

sealed class AppLockUiState {
    object Idle : AppLockUiState()
    object Unlocked : AppLockUiState()
    data class Error(val message: String) : AppLockUiState()
}