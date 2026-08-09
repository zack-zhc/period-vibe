package com.example.periodvibe.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据变更后刷新桌面小组件。失败不影响主流程。
 *
 * 刷新分两步：
 * 1. 通过 GlanceState（Preferences）推送最新渲染数据（updateAppWidgetState）——
 *    组合内的 currentState() 订阅状态流，新状态写入后自动重绘，不依赖会话重建；
 * 2. 发送系统 APPWIDGET_UPDATE 广播兜底触发渲染（覆盖会话/worker 未运行的场景）。
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun refresh() {
        try {
            val glanceManager = GlanceAppWidgetManager(context)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PeriodVibeWidgetReceiver::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            Log.d(TAG, "refresh: boundWidgetIds=${ids.joinToString()}")
            if (ids.isEmpty()) {
                Log.d(TAG, "refresh: no bound widgets, skip")
                return
            }
            ids.forEach { rawId ->
                val glanceId = glanceManager.getGlanceIdBy(rawId)
                runCatching {
                    val state = WidgetState.read(context)
                    updateAppWidgetState(context, glanceId) { state.writeTo(it) }
                    Log.d(TAG, "refresh: state pushed for widget $rawId (hasData=${state.hasData} phase=${state.phaseName})")
                }.onFailure { e ->
                    Log.e(TAG, "refresh: state push failed for widget $rawId", e)
                }
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
        const val TAG = "PV-LOG"
    }
}
