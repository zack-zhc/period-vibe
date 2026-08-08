
package com.example.periodvibe.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.periodvibe.R

class NotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        title: String,
        message: String,
        isPrivacyMode: Boolean = false,
        id: Int = NOTIFICATION_ID
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (isPrivacyMode) context.getString(R.string.notif_privacy_title) else title)
            .setContentText(if (isPrivacyMode) context.getString(R.string.notif_privacy_message) else message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(id, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "period_reminder_channel"
        const val NOTIFICATION_ID = 1

        fun notificationIdFor(type: String?): Int {
            return when (type) {
                "PERIOD_START" -> 1
                "OVULATION" -> 3
                else -> NOTIFICATION_ID
            }
        }
    }
}
