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
            title = "欢迎使用 Period Vibe",
            description = "您的智能经期管理助手，帮助您轻松记录和预测周期",
            iconRes = "calendar"
        ),
        OnboardingPage(
            title = "智能预测",
            description = "基于您的周期数据，准确预测下次经期和排卵期",
            iconRes = "prediction"
        ),
        OnboardingPage(
            title = "健康追踪",
            description = "记录症状、情绪和身体变化，全面了解身体状况",
            iconRes = "health"
        ),
        OnboardingPage(
            title = "贴心提醒",
            description = "及时提醒您重要日期，让您做好充分准备",
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
