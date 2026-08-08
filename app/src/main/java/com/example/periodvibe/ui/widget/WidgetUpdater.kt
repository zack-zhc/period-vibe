package com.example.periodvibe.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据变更后刷新桌面小组件。失败不影响主流程。
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun refresh() {
        try {
            PeriodVibeWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
