package com.example.periodvibe.utils

import android.content.Context
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val cycleRepository: CycleRepository
) {
    private val alarmScheduler = AlarmScheduler(context)

    suspend fun rescheduleNotificationIfNeeded() {
        withContext(Dispatchers.IO) {
            try {
                val settings = settingsRepository.getSettingsSync()
                if (settings != null && settings.notificationEnabled) {
                    val latestCycle = cycleRepository.getLatestCycle()
                    if (latestCycle != null) {
                        scheduleNotification(settings)
                    } else {
                        alarmScheduler.cancel()
                    }
                } else {
                    alarmScheduler.cancel()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun scheduleNotification(settings: Settings) {
        try {
            val latestCycle = cycleRepository.getLatestCycle() ?: return

            val cycleLength = latestCycle.cycleLength ?: settings.cycleLengthDefault
            val nextPeriodDate = latestCycle.startDate.plusDays(cycleLength.toLong())
            val notificationDateTime = LocalDateTime.of(
                nextPeriodDate.minusDays(settings.notificationDaysBefore.toLong()),
                settings.notificationTime
            )

            // 只有当通知时间在未来时才安排
            if (notificationDateTime.isAfter(LocalDateTime.now())) {
                val message = if (settings.privacyModeEnabled) {
                    "You have a new notification."
                } else {
                    "Your period is expected to start in ${settings.notificationDaysBefore} days!"
                }
                alarmScheduler.schedule(
                    notificationDateTime,
                    "Period Reminder",
                    message
                )
            } else {
                // 如果时间已过，取消现有通知
                alarmScheduler.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
