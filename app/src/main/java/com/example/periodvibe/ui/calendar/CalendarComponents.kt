package com.example.periodvibe.ui.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.periodvibe.domain.usecase.CalendarDay
import com.example.periodvibe.domain.usecase.CalendarDayType
import com.example.periodvibe.ui.theme.CalendarFertileDark
import com.example.periodvibe.ui.theme.CalendarFertileLight
import com.example.periodvibe.ui.theme.CalendarOvulationDark
import com.example.periodvibe.ui.theme.CalendarOvulationLight
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

// ==================== 月份头部 ====================

@Composable
fun CalendarMonthHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左边：左箭头
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "上个月",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 中间：月份标题
        Text(
            text = "${yearMonth.year}年${yearMonth.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 右边：右箭头
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp
        ) {
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "下个月",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ==================== 日历网格 ====================

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    days: List<CalendarDay>,
    selectedDate: java.time.LocalDate?,
    onDateClick: (java.time.LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WeekdayHeader()
        Spacer(modifier = Modifier.height(16.dp))
        CalendarDaysGrid(
            days = days,
            selectedDate = selectedDate,
            onDateClick = onDateClick
        )
    }
}

@Composable
private fun WeekdayHeader() {
    val isChinese = LocalLocale.current.platformLocale.language == Locale.CHINESE.language
    val weekdays = if (isChinese) {
        listOf("日", "一", "二", "三", "四", "五", "六")
    } else {
        listOf("S", "M", "T", "W", "T", "F", "S")
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekdays.forEach { weekday ->
            Text(
                text = weekday,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarDaysGrid(
    days: List<CalendarDay>,
    selectedDate: java.time.LocalDate?,
    onDateClick: (java.time.LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val rows = days.chunked(7)

        rows.forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                rowDays.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        isSelected = day is CalendarDay.Data && selectedDate == day.date,
                        onClick = { if (day is CalendarDay.Data) onDateClick(day.date) },
                        modifier = Modifier.weight(1f / 7)
                    )
                }
            }
        }
    }
}

// ==================== 日期单元格 ====================

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (day) {
        is CalendarDay.Empty -> {
            Spacer(modifier = modifier.aspectRatio(1f))
        }
        is CalendarDay.Data -> {
            DayContent(
                day = day,
                isSelected = isSelected,
                onClick = onClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun DayContent(
    day: CalendarDay.Data,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == androidx.compose.ui.graphics.Color(0xFF1F1A1B)

    val cellColors = getCellColors(day, isSelected, isDark)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "cell_scale"
    )
    val backgroundColor by animateColorAsState(
        targetValue = cellColors.container,
        animationSpec = tween(durationMillis = 200),
        label = "bg_color"
    )
    val textColor by animateColorAsState(
        targetValue = cellColors.text,
        animationSpec = tween(durationMillis = 200),
        label = "text_color"
    )
    val borderColor by animateColorAsState(
        targetValue = cellColors.border,
        animationSpec = tween(durationMillis = 200),
        label = "border_color"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .scale(scale)
            .then(
                if (cellColors.hasShadow) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        clip = false
                    )
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (cellColors.borderWidth > 0.dp) {
                    Modifier.border(
                        width = cellColors.borderWidth,
                        color = borderColor,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )

        // 右上角：已记录标记
        if (day.record?.flowLevel != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (day.isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已记录",
                    tint = if (day.isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}

private data class CellColors(
    val container: Color,
    val text: Color,
    val border: Color,
    val borderWidth: androidx.compose.ui.unit.Dp,
    val hasShadow: Boolean = false
)

@Composable
private fun getCellColors(
    day: CalendarDay.Data,
    isSelected: Boolean,
    isDark: Boolean
): CellColors {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface

    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val ovulationColor = if (isDark) CalendarOvulationDark else CalendarOvulationLight
    val fertileColor = if (isDark) CalendarFertileDark else CalendarFertileLight

    // 先获取基础背景色（非今天、非选中状态）
    val baseState = when {
        day.dayType == CalendarDayType.PERIOD -> {
            CellColors(
                container = periodColor.copy(alpha = 0.25f),
                text = onSurface,
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
        day.isPredictedPeriod -> {
            CellColors(
                container = periodColor.copy(alpha = 0.08f),
                text = onSurface.copy(alpha = 0.7f),
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
        day.dayType == CalendarDayType.OVULATION -> {
            CellColors(
                container = ovulationColor.copy(alpha = 0.25f),
                text = ovulationColor,
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
        day.isPredictedOvulation -> {
            CellColors(
                container = ovulationColor.copy(alpha = 0.08f),
                text = onSurface.copy(alpha = 0.7f),
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
        day.dayType == CalendarDayType.FERTILE -> {
            CellColors(
                container = fertileColor.copy(alpha = 0.25f),
                text = onSurface,
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
        day.isPredictedFertile -> {
            CellColors(
                container = fertileColor.copy(alpha = 0.08f),
                text = onSurface.copy(alpha = 0.7f),
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
        else -> {
            CellColors(
                container = Color.Transparent,
                text = onSurface,
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = false
            )
        }
    }

    // 叠加今天和选中状态
    return when {
        day.isToday && isSelected -> {
            // 今天 + 选中：Primary背景 + Primary边框 + 阴影
            CellColors(
                container = primary,
                text = onPrimary,
                border = primary,
                borderWidth = 2.dp,
                hasShadow = true
            )
        }
        day.isToday -> {
            // 今天：Primary背景 + 阴影
            CellColors(
                container = primary,
                text = onPrimary,
                border = Color.Transparent,
                borderWidth = 0.dp,
                hasShadow = true
            )
        }
        isSelected -> {
            // 选中（非今天）：保持原背景 + Primary边框，无阴影
            CellColors(
                container = baseState.container,
                text = baseState.text,
                border = primary,
                borderWidth = 2.dp,
                hasShadow = false
            )
        }
        else -> {
            baseState
        }
    }
}

// ==================== 图例 ====================

@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == androidx.compose.ui.graphics.Color(0xFF1F1A1B)
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val ovulationColor = if (isDark) CalendarOvulationDark else CalendarOvulationLight
    val fertileColor = if (isDark) CalendarFertileDark else CalendarFertileLight

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = periodColor, label = "经期", isPredicted = false)
            LegendItem(color = periodColor, label = "预测经期", isPredicted = true)
            LegendItem(color = fertileColor, label = "易孕", isPredicted = false)
            LegendItem(color = ovulationColor, label = "排卵", isPredicted = false)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, isPredicted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isPredicted) color.copy(alpha = 0.25f) else color.copy(alpha = 0.5f))
                .then(
                    if (isPredicted) {
                        Modifier.border(width = 1.5.dp, color = color.copy(alpha = 0.5f), shape = CircleShape)
                    } else {
                        Modifier
                    }
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==================== 空选择状态卡片 ====================

@Composable
fun EmptySelectionCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Text(
                    text = "选择日期",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "点击日历中的任意日期，查看详情并记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
