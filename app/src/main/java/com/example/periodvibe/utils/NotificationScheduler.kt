package com.example.periodvibe.utils

import android.content.Context
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.NotificationType
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

    suspend fun rescheduleAllNotifications() {
        withContext(Dispatchers.IO) {
            try {
                val settings = settingsRepository.getSettingsSync()
                if (settings != null && settings.notificationEnabled) {
                    val latestCycle = cycleRepository.getLatestCycle()
                    if (latestCycle != null) {
                        schedulePeriodNotification(settings)
                        scheduleOvulationNotification(settings)
                    } else {
                        alarmScheduler.cancelAll()
                    }
                } else {
                    alarmScheduler.cancelAll()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Deprecated("Use rescheduleAllNotifications() instead")
    suspend fun rescheduleNotificationIfNeeded() {
        rescheduleAllNotifications()
    }

    private suspend fun schedulePeriodNotification(settings: Settings) {
        if (!settings.periodNotificationEnabled) {
            alarmScheduler.cancel(NotificationType.PERIOD_START)
            return
        }

        try {
            val latestCycle = cycleRepository.getLatestCycle() ?: return

            val cycleLength = latestCycle.cycleLength ?: settings.cycleLengthDefault
            val nextPeriodDate = latestCycle.startDate.plusDays(cycleLength.toLong())
            val notificationDateTime = LocalDateTime.of(
                nextPeriodDate.minusDays(settings.notificationDaysBefore.toLong()),
                settings.notificationTime
            )

            if (notificationDateTime.isAfter(LocalDateTime.now())) {
                val message = if (settings.privacyModeEnabled) {
                    "You have a new notification."
                } else {
                    "Your period is expected to start in ${settings.notificationDaysBefore} days!"
                }
                alarmScheduler.schedule(
                    NotificationType.PERIOD_START,
                    notificationDateTime,
                    "Period Reminder",
                    message
                )
            } else {
                alarmScheduler.cancel(NotificationType.PERIOD_START)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun scheduleOvulationNotification(settings: Settings) {
        if (!settings.ovulationNotificationEnabled) {
            alarmScheduler.cancel(NotificationType.OVULATION)
            return
        }

        try {
            val latestCycle = cycleRepository.getLatestCycle() ?: return

            val cycleLength = latestCycle.cycleLength ?: settings.cycleLengthDefault
            val nextPeriodDate = latestCycle.startDate.plusDays(cycleLength.toLong())
            // 排卵日通常在下次月经前14天
            val ovulationDate = nextPeriodDate.minusDays(14)
            val notificationDateTime = LocalDateTime.of(
                ovulationDate.minusDays(settings.ovulationNotificationDaysBefore.toLong()),
                settings.notificationTime
            )

            if (notificationDateTime.isAfter(LocalDateTime.now())) {
                val message = if (settings.privacyModeEnabled) {
                    "You have a new notification."
                } else {
                    "Your ovulation period is coming soon!"
                }
                alarmScheduler.schedule(
                    NotificationType.OVULATION,
                    notificationDateTime,
                    "Ovulation Reminder",
                    message
                )
            } else {
                alarmScheduler.cancel(NotificationType.OVULATION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
