package com.example.periodvibe.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.periodvibe.utils.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class DeveloperOptionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val notificationManager = NotificationManager(context)

    fun sendTestNotification() {
        notificationManager.showNotification("Test Notification", "This is a test notification from Period Vibe.")
    }
}