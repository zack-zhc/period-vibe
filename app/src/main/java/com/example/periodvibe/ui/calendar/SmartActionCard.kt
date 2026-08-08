package com.example.periodvibe.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.example.periodvibe.R
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.usecase.CalendarDay
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun SmartActionCard(
    day: CalendarDay.Data,
    activeCycle: Cycle?,
    onRecordClick: () -> Unit,
    onEndCycleClick: () -> Unit,
    onNewCycleClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = determineActionContext(day, activeCycle)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateHeader(day = day)
            StatusInfo(context = context, day = day, activeCycle = activeCycle)
            ActionButtons(
                context = context,
                day = day,
                onRecordClick = onRecordClick,
                onEndCycleClick = onEndCycleClick,
                onNewCycleClick = onNewCycleClick,
                onEditClick = onEditClick
            )
        }
    }
}

@Composable
private fun DateHeader(day: CalendarDay.Data) {
    val locale = LocalLocale.current.platformLocale
    val isChinese = locale.language == java.util.Locale.CHINESE.language
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofPattern(
            if (isChinese) "M月d日" else "MMM d",
            locale
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = day.date.format(dateFormatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 相位 pill
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp
            ) {
                Text(
                    text = day.phase.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (day.isToday) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp
            ) {
                Text(
                    text = stringResource(R.string.cal_today),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusInfo(
    context: ActionContext,
    day: CalendarDay.Data,
    activeCycle: Cycle?
) {
    when (context) {
        ActionContext.IN_CYCLE_WITH_RECORD,
        ActionContext.IN_CYCLE_NO_RECORD -> {
            CycleStatusInfo(day = day, activeCycle = activeCycle)
        }
        ActionContext.OUT_CYCLE_WITH_RECORD -> {
            RecordInfo(day = day)
        }
        ActionContext.OUT_CYCLE_NO_RECORD -> {
            StatusLine(
                dotColor = MaterialTheme.colorScheme.outline,
                text = stringResource(R.string.cal_cycle_start_hint)
            )
        }
    }
}

@Composable
private fun CycleStatusInfo(
    day: CalendarDay.Data,
    activeCycle: Cycle?
) {
    val cycleDay = if (activeCycle != null && day.date >= activeCycle.startDate) {
        Period.between(activeCycle.startDate, day.date).days + 1
    } else {
        null
    }

    StatusLine(
        dotColor = MaterialTheme.colorScheme.primary,
        text = if (cycleDay != null) {
            "${stringResource(R.string.cal_cycle_day, cycleDay)} · ${stringResource(R.string.cal_current_phase, day.phase.displayName)}"
        } else {
            day.phase.displayName
        }
    )
}

@Composable
private fun RecordInfo(day: CalendarDay.Data) {
    StatusLine(
        dotColor = MaterialTheme.colorScheme.primary,
        text = day.record?.flowLevel?.let { flowLevel ->
            stringResource(R.string.cal_recorded_flow, flowLevel.displayName)
        } ?: stringResource(R.string.cal_recorded)
    )
}

@Composable
private fun StatusLine(
    dotColor: Color,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtons(
    context: ActionContext,
    day: CalendarDay.Data,
    onRecordClick: () -> Unit,
    onEndCycleClick: () -> Unit,
    onNewCycleClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val hasPeriodRecord = day.record?.isPeriod == true
    val hasActiveCycle = context == ActionContext.IN_CYCLE_WITH_RECORD || context == ActionContext.IN_CYCLE_NO_RECORD

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            // 有经期记录：编辑 + 结束周期并排（周期内才显示结束周期）
            hasPeriodRecord -> {
                if (hasActiveCycle) {
                    SecondaryActionsRow(
                        onEditClick = onEditClick,
                        onEndCycleClick = onEndCycleClick
                    )
                } else {
                    EditButton(onClick = onEditClick, modifier = Modifier.fillMaxWidth())
                }
            }
            // 周期内、无记录：记录 + 结束周期
            context == ActionContext.IN_CYCLE_NO_RECORD -> {
                RecordButton(text = stringResource(R.string.cal_record_today), onClick = onRecordClick)
                EndCycleButton(onClick = onEndCycleClick, modifier = Modifier.fillMaxWidth())
            }
            // 周期内、有非经期记录：编辑 + 结束周期并排
            context == ActionContext.IN_CYCLE_WITH_RECORD -> {
                SecondaryActionsRow(
                    onEditClick = onEditClick,
                    onEndCycleClick = onEndCycleClick
                )
            }
            // 周期外、无记录：开始周期
            context == ActionContext.OUT_CYCLE_NO_RECORD -> {
                NewCycleButton(onClick = onNewCycleClick)
            }
        }
    }
}

@Composable
private fun SecondaryActionsRow(
    onEditClick: () -> Unit,
    onEndCycleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditButton(onClick = onEditClick, modifier = Modifier.weight(1f))
        EndCycleButton(onClick = onEndCycleClick, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RecordButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.WaterDrop,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.cal_edit_record),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EndCycleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.Stop,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.cal_end_cycle),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NewCycleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.cal_start_cycle),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EndCycleConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.cal_end_cycle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.cal_end_cycle_message),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.cal_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cal_cancel))
            }
        }
    )
}

private enum class ActionContext {
    IN_CYCLE_WITH_RECORD,
    IN_CYCLE_NO_RECORD,
    OUT_CYCLE_WITH_RECORD,
    OUT_CYCLE_NO_RECORD
}

private fun determineActionContext(
    day: CalendarDay.Data,
    activeCycle: Cycle?
): ActionContext {
    val cycle = activeCycle
    val isInCycle = cycle != null && cycle.isCurrentCycle && day.date >= cycle.startDate
    val hasRecord = day.record?.flowLevel != null

    return when {
        isInCycle && hasRecord -> ActionContext.IN_CYCLE_WITH_RECORD
        isInCycle && !hasRecord -> ActionContext.IN_CYCLE_NO_RECORD
        !isInCycle && hasRecord -> ActionContext.OUT_CYCLE_WITH_RECORD
        else -> ActionContext.OUT_CYCLE_NO_RECORD
    }
}
