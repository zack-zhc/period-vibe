package com.example.periodvibe.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.NotificationType
import com.example.periodvibe.utils.AlarmScheduler
import com.example.periodvibe.utils.NotificationManager
import com.example.periodvibe.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperOptionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationScheduler: NotificationScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val notificationManager = NotificationManager(context)
    private val alarmScheduler = AlarmScheduler(context)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sendTestNotification() {
        notificationManager.showNotification("Test Notification", "This is a test notification from Period Vibe.")
    }

    fun sendTestPrivacyNotification() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsSync()
            val isPrivacyMode = settings?.privacyModeEnabled ?: false
            notificationManager.showNotification(
                "Period Reminder",
                "Your period is coming soon!",
                isPrivacyMode
            )
        }
    }

    fun sendTestDelayedNotification(seconds: Long = 10) {
        alarmScheduler.scheduleInSeconds(
            NotificationType.PERIOD_START,
            seconds,
            "Test Delayed Notification",
            "This is a delayed test notification from Period Vibe. It was scheduled $seconds seconds ago."
        )
    }

    fun sendTestOvulationNotification(seconds: Long = 10) {
        alarmScheduler.scheduleInSeconds(
            NotificationType.OVULATION,
            seconds,
            "Ovulation Reminder",
            "Your ovulation period is coming soon!"
        )
    }

    fun rescheduleNotification() {
        viewModelScope.launch {
            notificationScheduler.rescheduleAllNotifications()
        }
    }

    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsSync()
            settings?.let {
                val updatedSettings = it.copy(privacyModeEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }
}