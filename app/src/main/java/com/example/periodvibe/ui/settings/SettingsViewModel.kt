package com.example.periodvibe.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _showCycleDialog = MutableStateFlow(false)
    val showCycleDialog: StateFlow<Boolean> = _showCycleDialog.asStateFlow()

    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog: StateFlow<Boolean> = _showNotificationDialog.asStateFlow()

    private val _showDataManagementDialog = MutableStateFlow(false)
    val showDataManagementDialog: StateFlow<Boolean> = _showDataManagementDialog.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    private val _showPrivacyDialog = MutableStateFlow(false)
    val showPrivacyDialog: StateFlow<Boolean> = _showPrivacyDialog.asStateFlow()

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                if (settings != null) {
                    _uiState.value = SettingsUiState.Success(
                        autoCalculateCycle = settings.autoCalculateCycle,
                        cycleLengthDefault = settings.cycleLengthDefault,
                        periodLengthDefault = settings.periodLengthDefault,
                        cycleLengthRange = settings.cycleLengthRange,
                        periodLengthRange = settings.periodLengthRange,
                        notificationEnabled = settings.notificationEnabled,
                        notificationDaysBefore = settings.notificationDaysBefore,
                        notificationTime = settings.notificationTime,
                        themeMode = settings.themeMode,
                        appLockEnabled = settings.appLockEnabled,
                        privacyModeEnabled = settings.privacyModeEnabled,
                        language = settings.language
                    )
                } else {
                    _uiState.value = SettingsUiState.Loading
                }
            }
        }
    }

    fun showCycleDialog() {
        _showCycleDialog.value = true
    }

    fun hideCycleDialog() {
        _showCycleDialog.value = false
    }

    fun showNotificationDialog() {
        _showNotificationDialog.value = true
    }

    fun hideNotificationDialog() {
        _showNotificationDialog.value = false
    }

    fun showDataManagementDialog() {
        _showDataManagementDialog.value = true
    }

    fun hideDataManagementDialog() {
        _showDataManagementDialog.value = false
    }

    fun showThemeDialog() {
        _showThemeDialog.value = true
    }

    fun hideThemeDialog() {
        _showThemeDialog.value = false
    }

    fun showPrivacyDialog() {
        _showPrivacyDialog.value = true
    }

    fun hidePrivacyDialog() {
        _showPrivacyDialog.value = false
    }

    fun showAboutDialog() {
        _showAboutDialog.value = true
    }

    fun hideAboutDialog() {
        _showAboutDialog.value = false
    }

    fun updateCycleParameters(
        cycleLength: Int,
        periodLength: Int
    ) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.updateCycleParameters(cycleLength, periodLength)
                settingsRepository.updateSettings(updatedSettings)
            }
            hideCycleDialog()
        }
    }

    fun toggleAutoCalculateCycle(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(autoCalculateCycle = enabled)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun updateNotificationSettings(
        enabled: Boolean,
        daysBefore: Int,
        time: LocalTime
    ) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.updateNotificationSettings(enabled, daysBefore, time)
                settingsRepository.updateSettings(updatedSettings)
            }
            hideNotificationDialog()
        }
    }

    fun updateThemeMode(mode: com.example.periodvibe.domain.model.Settings.ThemeMode) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.updateThemeMode(mode)
                settingsRepository.updateSettings(updatedSettings)
            }
            hideThemeDialog()
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(appLockEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(privacyModeEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            hideDataManagementDialog()
        }
    }
}

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val autoCalculateCycle: Boolean,
        val cycleLengthDefault: Int,
        val periodLengthDefault: Int,
        val cycleLengthRange: IntRange,
        val periodLengthRange: IntRange,
        val notificationEnabled: Boolean,
        val notificationDaysBefore: Int,
        val notificationTime: LocalTime,
        val themeMode: com.example.periodvibe.domain.model.Settings.ThemeMode,
        val appLockEnabled: Boolean,
        val privacyModeEnabled: Boolean,
        val language: String
    ) : SettingsUiState()
}
