package com.example.periodvibe.ui.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.usecase.CalendarDay
import com.example.periodvibe.ui.theme.CalendarFertileDark
import com.example.periodvibe.ui.theme.CalendarFertileLight
import com.example.periodvibe.ui.theme.CalendarOvulationDark
import com.example.periodvibe.ui.theme.CalendarOvulationLight
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight
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
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF1F1A1B)
    val phaseData = getPhaseData(day.phase, isDark)
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 400, delayMillis = 100),
        label = "card_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
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
                            phaseData.color.copy(alpha = 0.08f),
                            phaseData.color.copy(alpha = 0.02f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .blur(
                        radius = 50.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
                    .background(
                        color = phaseData.color.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                DateHeader(day = day, phaseData = phaseData)
                StatusInfo(context = context, day = day, activeCycle = activeCycle, phaseData = phaseData)
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
}

@Composable
private fun DateHeader(day: CalendarDay.Data, phaseData: PhaseDisplayData) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日 EEEE") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = day.date.format(dateFormatter),
                style = MaterialTheme.typography.headlineSmall,
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
                        text = "今天",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = phaseData.containerColor,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = phaseData.color,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = day.phase.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = phaseData.color
                )
            }
        }
    }
}

@Composable
private fun StatusInfo(
    context: ActionContext,
    day: CalendarDay.Data,
    activeCycle: Cycle?,
    phaseData: PhaseDisplayData
) {
    when (context) {
        ActionContext.IN_CYCLE_WITH_RECORD -> {
            CycleStatusInfo(day = day, activeCycle = activeCycle, phaseData = phaseData)
        }
        ActionContext.IN_CYCLE_NO_RECORD -> {
            CycleStatusInfo(day = day, activeCycle = activeCycle, phaseData = phaseData)
        }
        ActionContext.OUT_CYCLE_WITH_RECORD -> {
            RecordInfo(day = day, phaseData = phaseData)
        }
        ActionContext.OUT_CYCLE_NO_RECORD -> {
            NoCycleInfo(day = day, phaseData = phaseData)
        }
    }
}

@Composable
private fun CycleStatusInfo(
    day: CalendarDay.Data,
    activeCycle: Cycle?,
    phaseData: PhaseDisplayData
) {
    val cycleDay = if (activeCycle != null && day.date >= activeCycle.startDate) {
        Period.between(activeCycle.startDate, day.date).days + 1
    } else {
        null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = phaseData.containerColor.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(phaseData.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(phaseData.color)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (cycleDay != null) {
                    Text(
                        text = "周期第 $cycleDay 天",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (cycleDay != null) "当前处于 ${phaseData.phaseName}" else "存在未结束的周期，请先结束",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecordInfo(day: CalendarDay.Data, phaseData: PhaseDisplayData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = phaseData.containerColor.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(phaseData.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = phaseData.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "已有记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (day.record?.isPeriod == true) {
                    Text(
                        text = "经期 - ${day.record?.flowLevel?.displayName ?: "未记录经量"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NoCycleInfo(day: CalendarDay.Data, phaseData: PhaseDisplayData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
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
                    text = "选择下方操作开始记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
            }
        }
    }
}

@Composable
private fun RecordButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "编辑记录",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EndCycleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
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
            text = "结束周期",
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
        shape = RoundedCornerShape(16.dp),
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
            text = "开始周期",
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
                text = "结束周期",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "确定要结束当前周期吗？这将标记当前周期的结束日期。",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

    if (hasActiveCycle && !isInCycle) {
        return ActionContext.IN_CYCLE_NO_RECORD
    }

    return when {
        isInCycle && hasRecord -> ActionContext.IN_CYCLE_WITH_RECORD
        isInCycle && !hasRecord -> ActionContext.IN_CYCLE_NO_RECORD
        !isInCycle && hasRecord -> ActionContext.OUT_CYCLE_WITH_RECORD
        else -> ActionContext.OUT_CYCLE_NO_RECORD
    }
}

private data class PhaseDisplayData(
    val color: Color,
    val containerColor: Color,
    val phaseName: String
)

@Composable
private fun getPhaseData(phase: CyclePhase, isDark: Boolean): PhaseDisplayData {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val ovulationColor = if (isDark) CalendarOvulationDark else CalendarOvulationLight
    val fertileColor = if (isDark) CalendarFertileDark else CalendarFertileLight

    return when (phase) {
        CyclePhase.MENSTRATION -> PhaseDisplayData(
            color = periodColor,
            containerColor = periodColor.copy(alpha = 0.12f),
            phaseName = "经期"
        )
        CyclePhase.OVULATION -> PhaseDisplayData(
            color = ovulationColor,
            containerColor = ovulationColor.copy(alpha = 0.12f),
            phaseName = "排卵期"
        )
        CyclePhase.FERTILE -> PhaseDisplayData(
            color = fertileColor,
            containerColor = fertileColor.copy(alpha = 0.1f),
            phaseName = "易孕期"
        )
        CyclePhase.SAFE -> PhaseDisplayData(
            color = MaterialTheme.colorScheme.tertiary,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            phaseName = "安全期"
        )
        CyclePhase.FOLLICULAR -> PhaseDisplayData(
            color = MaterialTheme.colorScheme.tertiary,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            phaseName = "卵泡期"
        )
        CyclePhase.LUTEAL -> PhaseDisplayData(
            color = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            phaseName = "黄体期"
        )
    }
}
