package com.example.periodvibe.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 Expressive 风格配色方案
// 采用温暖、柔和的粉色和桃色作为主色调，配合中性色

// 主色调 - 柔和桃红色 (Expressive Primary)
val PrimaryLight = Color(0xFFB82951)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFD9DE)
val OnPrimaryContainerLight = Color(0xFF3F0014)

// 次要色调 - 柔和紫红色 (Expressive Secondary)
val SecondaryLight = Color(0xFF994060)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFD9E3)
val OnSecondaryContainerLight = Color(0xFF3F001E)

// 第三色调 - 柔和紫罗兰色 (Expressive Tertiary)
val TertiaryLight = Color(0xFF7C536E)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8EE)
val OnTertiaryContainerLight = Color(0xFF301127)

// 错误色
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// 背景和表面
val BackgroundLight = Color(0xFFFFF8F9)
val OnBackgroundLight = Color(0xFF1F1A1B)
// M3 规范：surface 与 background 同值；表面色带主色倾向（Expressive 表面染色）
val SurfaceLight = Color(0xFFFFF8F9)
val OnSurfaceLight = Color(0xFF1F1A1B)
val SurfaceVariantLight = Color(0xFFF2DDE0)
val OnSurfaceVariantLight = Color(0xFF514345)
val OutlineLight = Color(0xFF837375)
val OutlineVariantLight = Color(0xFFD5C2C4)

// 浅色表面层级（Expressive：从背景到最高容器逐级加深，均带粉色倾向）
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFFF2F4)
val SurfaceContainerLight = Color(0xFFFFECEF)
val SurfaceContainerHighLight = Color(0xFFFFE6EA)
val SurfaceContainerHighestLight = Color(0xFFFFE0E5)
val SurfaceDimLight = Color(0xFFFAD9DE)
val SurfaceBrightLight = Color(0xFFFFF8F9)

// 反色与 scrim
val InverseSurfaceLight = Color(0xFF322C2E)
val InverseOnSurfaceLight = Color(0xFFF5EBED)
val InversePrimaryLight = Color(0xFFFFB1BF)
val Scrim = Color(0xFF000000)

// 固定色（Expressive 特征：两主题同值，用于 chips 与选中态容器）
val PrimaryFixed = Color(0xFFFFD9DE)
val PrimaryFixedDim = Color(0xFFFFB7C5)
val OnPrimaryFixed = Color(0xFF3F0014)
val OnPrimaryFixedVariant = Color(0xFF8E0032)

val SecondaryFixed = Color(0xFFFFD9E3)
val SecondaryFixedDim = Color(0xFFFFB3CD)
val OnSecondaryFixed = Color(0xFF3F001E)
val OnSecondaryFixedVariant = Color(0xFF792848)

val TertiaryFixed = Color(0xFFFFD8EE)
val TertiaryFixedDim = Color(0xFFF3B5D8)
val OnTertiaryFixed = Color(0xFF301127)
val OnTertiaryFixedVariant = Color(0xFF633B55)

// 深色主题
val PrimaryDark = Color(0xFFFFB1BF)
val OnPrimaryDark = Color(0xFF660024)
val PrimaryContainerDark = Color(0xFF93003A)
val OnPrimaryContainerDark = Color(0xFFFFD9DE)

val SecondaryDark = Color(0xFFFFB1C8)
val OnSecondaryDark = Color(0xFF640034)
val SecondaryContainerDark = Color(0xFF802748)
val OnSecondaryContainerDark = Color(0xFFFFD9E3)

val TertiaryDark = Color(0xFFEDB8D6)
val OnTertiaryDark = Color(0xFF48253D)
val TertiaryContainerDark = Color(0xFF623B55)
val OnTertiaryContainerDark = Color(0xFFFFD8EE)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF1F1A1B)
val OnBackgroundDark = Color(0xFFEBE0E1)
// M3 规范：surface 与 background 同值
val SurfaceDark = Color(0xFF1F1A1B)
val OnSurfaceDark = Color(0xFFEBE0E1)
val SurfaceVariantDark = Color(0xFF514345)
val OnSurfaceVariantDark = Color(0xFFD5C2C4)
val OutlineDark = Color(0xFF9E8C8E)
val OutlineVariantDark = Color(0xFF514345)

// 深色表面层级（从背景到最高容器逐级提亮，均带粉色倾向）
val SurfaceContainerLowestDark = Color(0xFF241E20)
val SurfaceContainerLowDark = Color(0xFF2A2426)
val SurfaceContainerDark = Color(0xFF2F292B)
val SurfaceContainerHighDark = Color(0xFF3A3335)
val SurfaceContainerHighestDark = Color(0xFF453E40)
val SurfaceDimDark = Color(0xFF1F1A1B)
val SurfaceBrightDark = Color(0xFF473F42)

// 反色
val InverseSurfaceDark = Color(0xFFEBE0E1)
val InverseOnSurfaceDark = Color(0xFF322C2E)
val InversePrimaryDark = Color(0xFFB82951)

// 周期阶段颜色（Material 3 Expressive 风格）
// 浅色统一为 M2 500 色板（亮度一致）：月经红 / 卵泡绿 / 排卵紫 / 易孕蓝 / 安全蓝绿 / 黄体橙
val MenstruationColor = Color(0xFFE91E63)
val FollicularColor = Color(0xFF4CAF50)
val OvulationColor = Color(0xFF9C27B0)
val FertileColor = Color(0xFF2196F3)
val SafeColor = Color(0xFF009688)
val LutealColor = Color(0xFFFF9800)

// 暗色变体：统一为 M2 300 色板（提高亮度保证深色背景上的对比度，与浅色 500 成对）
val MenstruationColorDark = Color(0xFFF06292)
val FollicularColorDark = Color(0xFF81C784)
val OvulationColorDark = Color(0xFFCE93D8)
val FertileColorDark = Color(0xFF4FC3F7)
val SafeColorDark = Color(0xFF4DB6AC)
val LutealColorDark = Color(0xFFFFCC80)

// 日历专用颜色
// 经期：鲜明的红色，容易识别
val CalendarPeriodLight = Color(0xFFC2185B)
val CalendarPeriodDark = Color(0xFFFF79A0)
// 预测经期：柔和的粉色，与实际经期区分
val CalendarPredictedPeriodLight = Color(0xFFF48FB1)
val CalendarPredictedPeriodDark = Color(0xFFFFCDD2)
// 排卵期：紫色
val CalendarOvulationLight = Color(0xFF7B1FA2)
val CalendarOvulationDark = Color(0xFFCE93D8)
// 易孕期：青绿色/蓝绿色，与红色明显区分
val CalendarFertileLight = Color(0xFF0288D1)
val CalendarFertileDark = Color(0xFF4FC3F7)
val CalendarTodayLight = Color(0xFFFF6F00)
val CalendarTodayDark = Color(0xFFFFB74D)
