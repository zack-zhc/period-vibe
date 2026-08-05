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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = day.date.format(dateFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
            if (day.record?.flowLevel != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.cal_recorded),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Text(
            text = day.phase.displayName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatusInfo(
    context: ActionContext,
    day: CalendarDay.Data,
    activeCycle: Cycle?
) {
    when (context) {
        ActionContext.IN_CYCLE_WITH_RECORD -> {
            CycleStatusInfo(day = day, activeCycle = activeCycle)
        }
        ActionContext.IN_CYCLE_NO_RECORD -> {
            CycleStatusInfo(day = day, activeCycle = activeCycle)
        }
        ActionContext.OUT_CYCLE_WITH_RECORD -> {
            RecordInfo(day = day)
        }
        ActionContext.OUT_CYCLE_NO_RECORD -> {
            NoCycleInfo(day = day)
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (cycleDay != null) {
                Text(
                    text = stringResource(R.string.cal_cycle_day, cycleDay),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.cal_current_phase, day.phase.displayName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecordInfo(day: CalendarDay.Data) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (day.record?.isPeriod == true) {
                Text(
                    text = stringResource(R.string.cal_period),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = day.record?.flowLevel?.displayName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoCycleInfo(day: CalendarDay.Data) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = day.phase.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.cal_cycle_start_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            // 只要有经期记录，就显示编辑按钮（周期内还显示结束周期按钮）
            hasPeriodRecord -> {
                EditButton(onClick = onEditClick)
                if (hasActiveCycle) {
                    EndCycleButton(onClick = onEndCycleClick)
                }
            }
            // 周期内、无记录：记录 + 结束周期
            context == ActionContext.IN_CYCLE_NO_RECORD -> {
                RecordButton(text = stringResource(R.string.cal_record_today), onClick = onRecordClick)
                EndCycleButton(onClick = onEndCycleClick)
            }
            // 周期内、有非经期记录：编辑 + 结束周期
            context == ActionContext.IN_CYCLE_WITH_RECORD -> {
                EditButton(onClick = onEditClick)
                EndCycleButton(onClick = onEndCycleClick)
            }
            // 周期外、无记录：开始周期
            context == ActionContext.OUT_CYCLE_NO_RECORD -> {
                NewCycleButton(onClick = onNewCycleClick)
            }
        }
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
private fun EditButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
private fun EndCycleButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
