package com.example.periodvibe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.util.OnboardingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val onboardingManager: OnboardingManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _showOnboarding = MutableStateFlow<Boolean?>(null)
    val showOnboarding: StateFlow<Boolean?> = _showOnboarding.asStateFlow()
    
    init {
        checkOnboardingStatus()
    }
    
    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            _showOnboarding.value = onboardingManager.shouldShowOnboarding()
        }
    }
    
    fun markOnboardingCompleted() {
        viewModelScope.launch {
            onboardingManager.markOnboardingCompleted()
            _showOnboarding.value = false
        }
    }

    fun resetOnboarding(onComplete: () -> Unit) {
        android.util.Log.d("MainViewModel", "resetOnboarding called")
        viewModelScope.launch {
            onboardingManager.resetOnboarding()
            android.util.Log.d("MainViewModel", "Database reset complete")
            _showOnboarding.value = true
            android.util.Log.d("MainViewModel", "showOnboarding set to true")
            onComplete()
            android.util.Log.d("MainViewModel", "onComplete callback called")
        }
    }

    fun getSettings() = settingsRepository.getSettings()
}
