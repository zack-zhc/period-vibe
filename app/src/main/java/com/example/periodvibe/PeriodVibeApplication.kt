package com.example.periodvibe

import android.app.Application
import com.example.periodvibe.ui.widget.WidgetUpdater
import com.example.periodvibe.utils.NotificationManager
import com.example.periodvibe.utils.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PeriodVibeApplication : Application() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val notificationManager = NotificationManager(this)
        notificationManager.createNotificationChannel()

        // 应用启动时重新安排通知，并刷新桌面小组件（同步日期变化）
        applicationScope.launch {
            notificationScheduler.rescheduleAllNotifications()
            widgetUpdater.refresh()
        }
    }
}