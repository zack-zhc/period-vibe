package com.example.periodvibe

import android.app.Application
import com.example.periodvibe.ui.widget.WidgetUpdater
import com.example.periodvibe.util.LanguageManager
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

    @Inject
    lateinit var languageManager: LanguageManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(
            LanguageManager.applyToConfiguration(
                LanguageManager.readStoredLanguage(newBase),
                newBase
            )
        )
    }

    override fun onCreate() {
        super.onCreate()
        val notificationManager = NotificationManager(this)
        notificationManager.createNotificationChannel()

        // 数据库中的语言设置与 SharedPreferences 镜像同步（存量用户保持原语言，新装跟随系统）
        applicationScope.launch {
            try {
                val repository = dagger.hilt.android.EntryPointAccessors.fromApplication(
                    this@PeriodVibeApplication,
                    com.example.periodvibe.di.SettingsEntryPoint::class.java
                ).settingsRepository()
                val dbLanguage = repository.getSettingsSync()?.language
                if (dbLanguage != null && dbLanguage != languageManager.storedLanguage()) {
                    languageManager.apply(dbLanguage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 应用启动时重新安排通知，并刷新桌面小组件（同步日期变化）
        applicationScope.launch {
            notificationScheduler.rescheduleAllNotifications()
            widgetUpdater.refresh()
        }
    }
}
