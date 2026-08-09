package com.example.periodvibe.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.periodvibe.MainActivity
import com.example.periodvibe.R

/**
 * 桌面小组件：周期进度（当前阶段、距离下次经期天数、预计下次经期日期）。
 * 无记录时显示引导文案，隐私模式开启时隐藏所有具体数据。
 *
 * 数据经 GlanceState（Preferences）推送：provideGlance 首次渲染时写入当前数据，
 * 之后的每次数据变更由 [WidgetUpdater] 调用 updateAppWidgetState 写入新状态，
 * 组合通过 currentState() 订阅状态流自动重绘（不依赖会话重跑 provideGlance）。
 */
class PeriodVibeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val fresh = WidgetState.read(context)
        // 写入最新状态，确保组合的 currentState() 首次即可读到真实数据
        runCatching {
            updateAppWidgetState(context, id) { fresh.writeTo(it) }
        }.onFailure {
            android.util.Log.e(TAG, "write initial state failed", it)
        }
        android.util.Log.d(TAG, "provideGlance: hasData=${fresh.hasData} phase=${fresh.phaseName} privacy=${fresh.privacyMode}")
        provideContent {
            val state = WidgetState.from(currentState<Preferences>())
            // 状态尚未写入（极端时序）时退回刚读取的数据
            PeriodVibeWidgetContent(state = if (state.isEmpty) fresh else state)
        }
    }

    override fun onCompositionError(context: Context, glanceId: GlanceId, viewId: Int, throwable: Throwable) {
        android.util.Log.e(TAG, "onCompositionError: $throwable", throwable)
        super.onCompositionError(context, glanceId, viewId, throwable)
    }

    private companion object {
        const val TAG = "PV-LOG"
    }
}

@Composable
fun PeriodVibeWidgetContent(
    state: WidgetState,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current

    GlanceTheme(colors = DynamicThemeColorProviders) {
        Box(
            modifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            Alignment.TopStart
        ) {
            when {
                state.privacyMode -> PrivacyMaskedContent(context)
                !state.hasData -> EmptyContent(context)
                else -> DataContent(context, state)
            }
        }
    }
}

@Composable
private fun DataContent(
    context: Context,
    state: WidgetState
) {
    val phaseColorProvider = runCatching {
        ColorProvider(Color(android.graphics.Color.parseColor(state.phaseColorHex)))
    }.getOrDefault(GlanceTheme.colors.primary)

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // 阶段色竖条作为左侧强调色
        Box(
            GlanceModifier
                .fillMaxHeight()
                .width(4.dp)
                .background(phaseColorProvider)
        ) {}
        Spacer(GlanceModifier.width(12.dp))
        Column(
            modifier = GlanceModifier.defaultWeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = state.phaseName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = state.bigText,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = state.subtitle,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyContent(context: Context) {
    CenteredText(
        title = context.getString(R.string.widget_empty_title),
        hint = context.getString(R.string.widget_empty_hint)
    )
}

@Composable
private fun PrivacyMaskedContent(context: Context) {
    CenteredText(
        title = context.getString(R.string.widget_privacy_masked),
        hint = context.getString(R.string.widget_privacy_hint)
    )
}

@Composable
private fun CenteredText(title: String, hint: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = hint,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp
            ),
            maxLines = 2
        )
    }
}
