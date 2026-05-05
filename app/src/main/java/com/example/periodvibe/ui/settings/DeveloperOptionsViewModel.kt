package com.example.periodvibe.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
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
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val notificationManager = NotificationManager(context)
    private val alarmScheduler = AlarmScheduler(context)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sendTestNotification() {
        notificationManager.showNotification("Test Notification", "This is a test notification from Period Vibe.")
    }

    fun sendTestDelayedNotification(seconds: Long = 10) {
        alarmScheduler.scheduleInSeconds(
            seconds,
            "Test Delayed Notification",
            "This is a delayed test notification from Period Vibe. It was scheduled $seconds seconds ago."
        )
    }

    fun rescheduleNotification() {
        viewModelScope.launch {
            notificationScheduler.rescheduleNotificationIfNeeded()
        }
    }
}