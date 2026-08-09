package com.example.periodvibe.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.periodvibe.R
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.usecase.GetHomeDataUseCase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 小组件渲染数据。Glance 1.1 的会话组合会复用首次 provideGlance 捕获的内容，
 * 因此数据通过 GlanceState（Preferences）推送：每次数据变更由 [WidgetUpdater]
 * 写入新状态，组合内 currentState() 订阅状态流自动重绘。
 */
data class WidgetState(
    val hasData: Boolean = false,
    val privacyMode: Boolean = false,
    val phaseName: String = "",
    val phaseColorHex: String = "",
    val bigText: String = "",
    val subtitle: String = ""
) {
    fun writeTo(prefs: MutablePreferences) {
        prefs[KEY_HAS_DATA] = hasData
        prefs[KEY_PRIVACY] = privacyMode
        prefs[KEY_PHASE_NAME] = phaseName
        prefs[KEY_PHASE_COLOR] = phaseColorHex
        prefs[KEY_BIG_TEXT] = bigText
        prefs[KEY_SUBTITLE] = subtitle
    }

    companion object {
        private val KEY_HAS_DATA = booleanPreferencesKey("has_data")
        private val KEY_PRIVACY = booleanPreferencesKey("privacy_mode")
        private val KEY_PHASE_NAME = stringPreferencesKey("phase_name")
        private val KEY_PHASE_COLOR = stringPreferencesKey("phase_color")
        private val KEY_BIG_TEXT = stringPreferencesKey("big_text")
        private val KEY_SUBTITLE = stringPreferencesKey("subtitle")

        fun from(prefs: Preferences): WidgetState {
            return WidgetState(
                hasData = prefs[KEY_HAS_DATA] ?: false,
                privacyMode = prefs[KEY_PRIVACY] ?: false,
                phaseName = prefs[KEY_PHASE_NAME] ?: "",
                phaseColorHex = prefs[KEY_PHASE_COLOR] ?: "",
                bigText = prefs[KEY_BIG_TEXT] ?: "",
                subtitle = prefs[KEY_SUBTITLE] ?: ""
            )
        }

        /** 从数据库读取最新数据并生成渲染文案（在挂起的取数上下文中调用） */
        suspend fun read(context: Context): WidgetState {
            val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            val homeData = runCatching { entryPoint.getHomeDataUseCase().invoke().first() }.getOrNull()
            val privacyMode = runCatching {
                entryPoint.settingsRepository().getSettingsSync()?.privacyModeEnabled == true
            }.getOrDefault(false)
            val cycleInfo = homeData?.cycleInfo
            if (cycleInfo == null) {
                return WidgetState(
                    hasData = false,
                    privacyMode = privacyMode
                )
            }
            val phase = cycleInfo.phase
            val bigText = if (phase == CyclePhase.MENSTRATION) {
                context.getString(R.string.widget_period_day, cycleInfo.dayInCycle)
            } else if (cycleInfo.daysUntilNextPeriod != null && cycleInfo.daysUntilNextPeriod > 0) {
                context.getString(R.string.widget_days_remaining, cycleInfo.daysUntilNextPeriod)
            } else {
                context.getString(R.string.widget_cycle_day, cycleInfo.dayInCycle)
            }
            val nextStart = cycleInfo.prediction?.nextPeriodStart ?: run {
                val cycleLength = cycleInfo.cycle.cycleLength ?: 28
                cycleInfo.cycle.startDate.plusDays(cycleLength.toLong())
            }
            return WidgetState(
                hasData = true,
                privacyMode = privacyMode,
                phaseName = phaseName(context, phase),
                phaseColorHex = phase.color,
                bigText = bigText,
                subtitle = context.getString(
                    R.string.widget_next_period_date,
                    nextStart.format(dateFormatter(context))
                )
            )
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
    }
}
