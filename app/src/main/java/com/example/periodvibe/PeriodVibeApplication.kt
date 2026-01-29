package com.example.periodvibe

import android.app.Application
import com.example.periodvibe.utils.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PeriodVibeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val notificationManager = NotificationManager(this)
        notificationManager.createNotificationChannel()
    }
}