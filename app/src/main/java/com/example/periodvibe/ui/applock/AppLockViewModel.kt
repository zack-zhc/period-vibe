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
import kotlinx.coroutines.delay
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

    // 锁定倒计时（秒），0 表示未在锁定中
    private val _lockoutRemainingSeconds = MutableStateFlow(0L)
    val lockoutRemainingSeconds: StateFlow<Long> = _lockoutRemainingSeconds.asStateFlow()

    val pin = mutableStateOf("")

    fun onPinChange(newPin: String) {
        pin.value = newPin
    }

    fun onPinEntered() {
        viewModelScope.launch {
            // 锁定期间忽略输入
            if (securityRepository.getLockoutRemainingMillis() > 0) return@launch
            val storedPin = securityRepository.getPin()
            if (pin.value == storedPin) {
                securityRepository.resetFailedAttempts()
                _uiState.value = AppLockUiState.Unlocked
            } else {
                securityRepository.recordFailedAttempt()
                _uiState.value = AppLockUiState.Error("PIN 码错误")
                startLockoutTicker()
            }
        }
    }

    private fun startLockoutTicker() {
        viewModelScope.launch {
            while (true) {
                val remainingMillis = securityRepository.getLockoutRemainingMillis()
                _lockoutRemainingSeconds.value = (remainingMillis + 999L) / 1000L
                if (remainingMillis <= 0L) {
                    _lockoutRemainingSeconds.value = 0L
                    break
                }
                delay(1000)
            }
        }
    }

    fun clearError() {
        _uiState.value = AppLockUiState.Idle
    }

    /** 重置为初始状态（进入锁屏页时调用，防止复用旧 ViewModel 的 Unlocked 残留导致自动解锁） */
    fun resetToIdle() {
        _uiState.value = AppLockUiState.Idle
        pin.value = ""
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
                    .setTitle("Period Vibe 生物识别登录")
                    .setSubtitle("使用您的生物识别凭证登录")
                    .setNegativeButtonText("使用 PIN 码")
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
                            // 用户主动取消（如点击"使用 PIN 码"）不算错误
                            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_CANCELED
                            ) {
                                _uiState.value = AppLockUiState.Idle
                                return
                            }
                            _uiState.value = AppLockUiState.Error(errString.toString())
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            _uiState.value = AppLockUiState.Error("认证失败")
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
