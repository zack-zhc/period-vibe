package com.example.periodvibe.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据变更后刷新桌面小组件。失败不影响主流程。
 *
 * 刷新走系统标准的 APPWIDGET_UPDATE 广播（与系统更新小组件的机制一致），
 * 不依赖 Glance 内部 DataStore 的注册信息——即使注册数据丢失或异常，
 * 只要 widget 仍绑定在桌面上，广播都能可靠送达并触发重新渲染。
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun refresh() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PeriodVibeWidgetReceiver::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isEmpty()) {
                Log.d(TAG, "refresh: no bound widgets")
                return
            }
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .setComponent(componentName)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
            Log.d(TAG, "refresh: sent APPWIDGET_UPDATE for ${ids.size} widget(s)")
        } catch (e: Exception) {
            Log.e(TAG, "refresh failed", e)
        }
    }

    private companion object {
        const val TAG = "PeriodVibeWidget"
    }
}
