package com.example.periodvibe.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.util.OnboardingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingManager: OnboardingManager
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _onComplete = MutableStateFlow(false)
    val onComplete: StateFlow<Boolean> = _onComplete.asStateFlow()

    val pages = listOf(
        OnboardingPage(
            titleRes = com.example.periodvibe.R.string.onb_welcome_title,
            descriptionRes = com.example.periodvibe.R.string.onb_welcome_desc,
            iconRes = "calendar"
        ),
        OnboardingPage(
            titleRes = com.example.periodvibe.R.string.onb_prediction_title,
            descriptionRes = com.example.periodvibe.R.string.onb_prediction_desc,
            iconRes = "prediction"
        ),
        OnboardingPage(
            titleRes = com.example.periodvibe.R.string.onb_health_title,
            descriptionRes = com.example.periodvibe.R.string.onb_health_desc,
            iconRes = "health"
        ),
        OnboardingPage(
            titleRes = com.example.periodvibe.R.string.onb_reminder_title,
            descriptionRes = com.example.periodvibe.R.string.onb_reminder_desc,
            iconRes = "notification"
        )
    )

    val totalPages = pages.size

    fun nextPage() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.value++
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    fun onPageChanged(page: Int) {
        if (page in 0 until totalPages) {
            _currentPage.value = page
        }
    }

    fun onComplete() {
        viewModelScope.launch {
            onboardingManager.markOnboardingCompleted()
            _onComplete.value = true
        }
    }

    fun resetState() {
        _currentPage.value = 0
        _onComplete.value = false
    }

    fun isLastPage(): Boolean {
        return _currentPage.value == totalPages - 1
    }
}
