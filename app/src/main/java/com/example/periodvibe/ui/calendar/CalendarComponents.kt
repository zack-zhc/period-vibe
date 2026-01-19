package com.example.periodvibe.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodvibe.domain.usecase.CalendarDay
import com.example.periodvibe.domain.usecase.CalendarDayType
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

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

        Spacer(modifier = Modifier.height(8.dp))

        CalendarDaysGrid(
            days = days,
            selectedDate = selectedDate,
            onDateClick = onDateClick
        )
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
                style = MaterialTheme.typography.bodySmall,
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        day.isPredictedPeriod -> Color(0xFFFFCDD2)
        day.isPredictedOvulation -> Color(0xFFB2DFDB)
        day.isPredictedFertile -> Color(0xFFFFCDD2)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        day.isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        day.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )

            if (day.dayType == CalendarDayType.PERIOD) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B6B))
                )
            } else if (day.dayType == CalendarDayType.OVULATION) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ECDC4))
                )
            } else if (day.dayType == CalendarDayType.FERTILE) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB6C1))
                )
            }
        }
    }
}

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "上个月",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${yearMonth.year}年${yearMonth.monthValue}月",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "下个月",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SelectedDateCard(
    day: CalendarDay.Data,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${day.date.year}年${day.date.monthValue}月${day.dayOfMonth}日",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = day.phase.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (day.record != null) {
                RecordDetails(day.record)
            } else {
                Text(
                    text = "暂无记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecordDetails(record: com.example.periodvibe.domain.model.DailyRecord) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (record.isPeriod) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B6B))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "经期 - ${record.flowLevel?.displayName ?: "未记录经量"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (record.symptoms.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "症状: ${record.symptoms.joinToString(", ") { it.displayName }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!record.notes.isNullOrBlank()) {
            Text(
                text = "备注: ${record.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
