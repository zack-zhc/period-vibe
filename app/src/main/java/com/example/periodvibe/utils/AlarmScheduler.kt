package com.example.periodvibe.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.periodvibe.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(type: NotificationType, dateTime: LocalDateTime, title: String, message: String) {
        try {
            // 先取消同类型的闹钟
            cancel(type)

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("message", message)
                putExtra("notification_type", type.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCodeForType(type),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (triggerAtMillis > System.currentTimeMillis()) {
                // 检查是否有精确闹钟权限
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
                    alarmManager.canScheduleExactAlarms()
                ) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // 没有权限时使用不精确的闹钟
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            // 忽略闹钟设置错误，不影响主要功能
            e.printStackTrace()
        }
    }

    fun cancel(type: NotificationType) {
        try {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCodeForType(type),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAll() {
        NotificationType.values().forEach { cancel(it) }
    }

    fun scheduleInSeconds(type: NotificationType, seconds: Long, title: String, message: String) {
        val dateTime = LocalDateTime.now().plusSeconds(seconds)
        schedule(type, dateTime, title, message)
    }

    companion object {
        private fun getRequestCodeForType(type: NotificationType): Int {
            return when (type) {
                NotificationType.PERIOD_START -> 1001
                NotificationType.PERIOD_END -> 1002
                NotificationType.OVULATION -> 1003
                NotificationType.FERTILE -> 1004
                NotificationType.DAILY_RECORD -> 1005
                NotificationType.CYCLE_SUMMARY -> 1006
            }
        }
    }
}
