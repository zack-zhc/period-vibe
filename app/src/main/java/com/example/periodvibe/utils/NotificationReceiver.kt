package com.example.periodvibe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManager(context)
        notificationManager.showNotification(
            title = intent.getStringExtra("title") ?: "Period Reminder",
            message = intent.getStringExtra("message") ?: "Your period is coming soon!"
        )
    }
}