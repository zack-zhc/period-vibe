package com.example.periodvibe.ui.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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

// ==================== 月份头部 ====================

@Composable
fun CalendarMonthHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "上个月",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.CHINA),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${yearMonth.year}年",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onTodayClick,
                modifier = Modifier.size(48.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "今天",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(48.dp)
            ) {
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun WeekdayHeader() {
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekdays.forEach { weekday ->
            Text(
                text = weekday,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    val cellStyle = getCellStyle(day)
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

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .clip(cellStyle.shape)
            .background(backgroundColor)
            .then(
                if (cellColors.border != Color.Transparent) {
                    Modifier.border(
                        width = if (day.isToday) 2.dp else 0.dp,
                        color = cellColors.border,
                        shape = cellStyle.shape
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )

            if (cellColors.showDot) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(cellStyle.dotSize)
                        .clip(cellStyle.dotShape)
                        .background(cellColors.dotColor)
                )
            }
        }
    }
}

private data class CellStyle(
    val shape: androidx.compose.ui.graphics.Shape,
    val dotSize: androidx.compose.ui.unit.Dp,
    val dotShape: androidx.compose.ui.graphics.Shape
)

private fun getCellStyle(day: CalendarDay.Data): CellStyle {
    return when {
        day.dayType == CalendarDayType.PERIOD || day.isPredictedPeriod -> {
            // 经期：使用更大的圆点标记
            CellStyle(
                shape = RoundedCornerShape(16.dp),
                dotSize = 8.dp,
                dotShape = CircleShape
            )
        }
        day.dayType == CalendarDayType.OVULATION || day.isPredictedOvulation -> {
            // 排卵期：使用菱形/方形标记
            CellStyle(
                shape = RoundedCornerShape(16.dp),
                dotSize = 7.dp,
                dotShape = RoundedCornerShape(2.dp)
            )
        }
        day.dayType == CalendarDayType.FERTILE || day.isPredictedFertile -> {
            // 易孕期：使用小的圆点
            CellStyle(
                shape = RoundedCornerShape(16.dp),
                dotSize = 5.dp,
                dotShape = CircleShape
            )
        }
        else -> {
            CellStyle(
                shape = RoundedCornerShape(16.dp),
                dotSize = 5.dp,
                dotShape = CircleShape
            )
        }
    }
}

private data class CellColors(
    val container: Color,
    val text: Color,
    val border: Color,
    val showDot: Boolean,
    val dotColor: Color
)

@Composable
private fun getCellColors(
    day: CalendarDay.Data,
    isSelected: Boolean,
    isDark: Boolean
): CellColors {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val ovulationColor = if (isDark) CalendarOvulationDark else CalendarOvulationLight
    val fertileColor = if (isDark) CalendarFertileDark else CalendarFertileLight

    return when {
        isSelected -> {
            val hasRecord = day.record != null
            val dotColor = when {
                day.dayType == CalendarDayType.PERIOD -> periodColor
                day.dayType == CalendarDayType.OVULATION -> ovulationColor
                day.dayType == CalendarDayType.FERTILE -> fertileColor
                else -> primary
            }
            CellColors(
                container = primaryContainer,
                text = onPrimaryContainer,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = hasRecord || day.isPredictedPeriod || day.isPredictedOvulation || day.isPredictedFertile,
                dotColor = dotColor
            )
        }
        day.dayType == CalendarDayType.PERIOD -> {
            CellColors(
                container = periodColor.copy(alpha = 0.15f),
                text = periodColor,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = true,
                dotColor = periodColor
            )
        }
        day.dayType == CalendarDayType.OVULATION -> {
            CellColors(
                container = ovulationColor.copy(alpha = 0.15f),
                text = ovulationColor,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = true,
                dotColor = ovulationColor
            )
        }
        day.dayType == CalendarDayType.FERTILE -> {
            CellColors(
                container = fertileColor.copy(alpha = 0.12f),
                text = fertileColor,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = true,
                dotColor = fertileColor
            )
        }
        day.isPredictedPeriod -> {
            CellColors(
                container = periodColor.copy(alpha = 0.08f),
                text = onSurface,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = true,
                dotColor = periodColor.copy(alpha = 0.5f)
            )
        }
        day.isPredictedOvulation -> {
            CellColors(
                container = ovulationColor.copy(alpha = 0.08f),
                text = onSurface,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = true,
                dotColor = ovulationColor.copy(alpha = 0.5f)
            )
        }
        day.isPredictedFertile -> {
            CellColors(
                container = fertileColor.copy(alpha = 0.06f),
                text = onSurface,
                border = if (day.isToday) primary else Color.Transparent,
                showDot = true,
                dotColor = fertileColor.copy(alpha = 0.5f)
            )
        }
        day.isToday -> {
            CellColors(
                container = surface,
                text = primary,
                border = primary,
                showDot = day.record != null,
                dotColor = primary
            )
        }
        else -> {
            CellColors(
                container = Color.Transparent,
                text = onSurface,
                border = Color.Transparent,
                showDot = day.record != null,
                dotColor = onSurfaceVariant
            )
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
            LegendItem(color = periodColor, label = "经期")
            LegendItem(color = fertileColor, label = "易孕")
            LegendItem(color = ovulationColor, label = "排卵")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .blur(
                        radius = 40.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )

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
