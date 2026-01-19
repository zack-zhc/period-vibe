package com.example.periodvibe.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateHeader(day = day)

            Spacer(modifier = Modifier.height(4.dp))

            StatusInfo(context = context, day = day, activeCycle = activeCycle)

            Spacer(modifier = Modifier.height(8.dp))

            ActionButtons(
                context = context,
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
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE") }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day.date.format(dateFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (day.isToday) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "今天",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
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
private fun CycleStatusInfo(day: CalendarDay.Data, activeCycle: Cycle?) {
    val cycleDay = if (activeCycle != null) {
        Period.between(activeCycle.startDate, day.date).days + 1
    } else {
        0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFF6B6B))
        )
        Text(
            text = "当前状态：第 $cycleDay 天 ${day.phase.displayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecordInfo(day: CalendarDay.Data) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (day.record?.isPeriod == true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF6B6B))
                )
                Text(
                    text = "已记录：经期 - ${day.record?.flowLevel?.displayName ?: "未记录经量"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (day.record?.hasSymptoms == true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "症状：${day.record?.symptoms?.joinToString("、") { it.displayName }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!day.record?.notes.isNullOrBlank()) {
            Text(
                text = "备注：${day.record?.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoCycleInfo(day: CalendarDay.Data) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF95E1D3))
        )
        Text(
            text = "当前状态：${day.phase.displayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionButtons(
    context: ActionContext,
    onRecordClick: () -> Unit,
    onEndCycleClick: () -> Unit,
    onNewCycleClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (context) {
            ActionContext.IN_CYCLE_WITH_RECORD -> {
                EditButton(onClick = onEditClick)
                EndCycleButton(onClick = onEndCycleClick)
            }
            ActionContext.IN_CYCLE_NO_RECORD -> {
                RecordButton(text = "记录今日", onClick = onRecordClick)
                EndCycleButton(onClick = onEndCycleClick)
            }
            ActionContext.OUT_CYCLE_WITH_RECORD -> {
                EditButton(onClick = onEditClick)
                NewCycleButton(onClick = onNewCycleClick)
            }
            ActionContext.OUT_CYCLE_NO_RECORD -> {
                NewCycleButton(onClick = onNewCycleClick)
                RecordSymptomButton(onClick = onRecordClick)
            }
        }
    }
}

@Composable
private fun RecordButton(text: String = "记录", onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "编辑记录",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("编辑记录", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EndCycleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text("结束周期", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NewCycleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "开始新周期",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("开始周期", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecordSymptomButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text("记录症状", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                text = "结束周期",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "确定要结束当前周期吗？这将标记当前周期的结束日期。",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
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
    val hasActiveCycle = activeCycle != null && activeCycle.isCurrentCycle
    val isInCycle = hasActiveCycle && day.date >= activeCycle!!.startDate
    val hasRecord = day.record != null

    return when {
        isInCycle && hasRecord -> ActionContext.IN_CYCLE_WITH_RECORD
        isInCycle && !hasRecord -> ActionContext.IN_CYCLE_NO_RECORD
        !isInCycle && hasRecord -> ActionContext.OUT_CYCLE_WITH_RECORD
        else -> ActionContext.OUT_CYCLE_NO_RECORD
    }
}
