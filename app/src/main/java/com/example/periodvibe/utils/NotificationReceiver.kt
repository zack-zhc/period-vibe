package com.example.periodvibe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManager(context)
        notificationManager.showNotification(
            title = intent.getStringExtra("title") ?: context.getString(com.example.periodvibe.R.string.notif_period_title),
            message = intent.getStringExtra("message") ?: context.getString(com.example.periodvibe.R.string.notif_period_message_default),
            id = NotificationManager.notificationIdFor(intent.getStringExtra("notification_type"))
        )
    }
}