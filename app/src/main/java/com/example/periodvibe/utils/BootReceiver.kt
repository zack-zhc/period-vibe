package com.example.periodvibe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.periodvibe.ui.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent?.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            // 设备重启会清除所有闹钟，这里重新安排通知并刷新小组件（同步日期变化）
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    notificationScheduler.rescheduleAllNotifications()
                    widgetUpdater.refresh()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
