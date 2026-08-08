package com.example.periodvibe.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders
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
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.usecase.GetHomeDataUseCase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 桌面小组件：周期进度（当前阶段、距离下次经期天数、预计下次经期日期）。
 * 无记录时显示引导文案，隐私模式开启时隐藏所有具体数据。
 */
class PeriodVibeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val homeData = runCatching { entryPoint.getHomeDataUseCase().invoke().first() }.getOrNull()
        val privacyMode = runCatching {
            entryPoint.settingsRepository().getSettingsSync()?.privacyModeEnabled == true
        }.getOrDefault(false)
        provideContent {
            PeriodVibeWidgetContent(homeData = homeData, privacyMode = privacyMode)
        }
    }
}

@Composable
fun PeriodVibeWidgetContent(
    homeData: GetHomeDataUseCase.HomeData?,
    privacyMode: Boolean,
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
                privacyMode -> PrivacyMaskedContent(context)
                homeData?.cycleInfo == null -> EmptyContent(context)
                else -> DataContent(context, homeData)
            }
        }
    }
}

@Composable
private fun DataContent(
    context: Context,
    homeData: GetHomeDataUseCase.HomeData
) {
    val cycleInfo = homeData.cycleInfo
        ?: run { EmptyContent(context); return }
    val phaseColorProvider = runCatching {
        ColorProvider(Color(android.graphics.Color.parseColor(cycleInfo.phase.color)))
    }.getOrDefault(GlanceTheme.colors.primary)
    val phaseName = phaseName(context, cycleInfo.phase)

    val bigText = if (cycleInfo.daysUntilNextPeriod != null && cycleInfo.daysUntilNextPeriod > 0) {
        context.getString(R.string.widget_days_remaining, cycleInfo.daysUntilNextPeriod)
    } else if (cycleInfo.phase == CyclePhase.MENSTRATION) {
        context.getString(R.string.widget_period_day, cycleInfo.dayInCycle)
    } else {
        context.getString(R.string.widget_cycle_day, cycleInfo.dayInCycle)
    }

    val nextStart = cycleInfo.prediction?.nextPeriodStart ?: run {
        val cycleLength = cycleInfo.cycle.cycleLength ?: 28
        cycleInfo.cycle.startDate.plusDays(cycleLength.toLong())
    }
    val subtitle = context.getString(
        R.string.widget_next_period_date,
        nextStart.format(dateFormatter(context))
    )

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
                text = phaseName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = bigText,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = subtitle,
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

private fun phaseName(context: Context, phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRATION -> context.getString(R.string.widget_phase_menstruation)
        CyclePhase.FOLLICULAR -> context.getString(R.string.widget_phase_follicular)
        CyclePhase.OVULATION -> context.getString(R.string.widget_phase_ovulation)
        CyclePhase.LUTEAL -> context.getString(R.string.widget_phase_luteal)
        CyclePhase.FERTILE -> context.getString(R.string.widget_phase_fertile)
        CyclePhase.SAFE -> context.getString(R.string.widget_phase_safe)
    }
}

private fun dateFormatter(context: Context): DateTimeFormatter {
    val locale = Locale.getDefault()
    val isChinese = locale.language == Locale.CHINESE.language
    return DateTimeFormatter.ofPattern(
        if (isChinese) "M月d日" else "MMM d",
        locale
    )
}
