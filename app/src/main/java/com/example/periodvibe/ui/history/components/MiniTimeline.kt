package com.example.periodvibe.ui.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight

@Composable
fun MiniTimeline(
    cycleLengthDays: Int?,
    periodDays: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val totalDays = cycleLengthDays ?: 28

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.Start
        ) {
            if (totalDays > 0 && periodDays > 0) {
                val periodRatio = periodDays.toFloat() / totalDays.toFloat().coerceAtLeast(periodDays.toFloat())
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(periodRatio)
                        .background(periodColor, RoundedCornerShape(6.dp))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "经期 $periodDays 天",
                style = MaterialTheme.typography.bodyMedium,
                color = periodColor
            )
            Text(
                text = if (cycleLengthDays != null) "周期 $cycleLengthDays 天" else "周期 -- 天",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
