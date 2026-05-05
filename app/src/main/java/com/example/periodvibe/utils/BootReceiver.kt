package com.example.periodvibe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent?.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            // 设备已启动 - 目前我们依赖用户下次打开应用时重新安排通知
            // 这样做可以避免复杂的依赖注入问题
            // 当用户再次打开应用时，PeriodVibeApplication 和 MainActivity 都会重新安排通知
        }
    }
}
