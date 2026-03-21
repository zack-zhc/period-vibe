package com.example.periodvibe.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Context
import com.example.periodvibe.R
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.ui.theme.FertileColor
import com.example.periodvibe.ui.theme.FollicularColor
import com.example.periodvibe.ui.theme.LutealColor
import com.example.periodvibe.ui.theme.MenstruationColor
import com.example.periodvibe.ui.theme.OvulationColor
import com.example.periodvibe.ui.theme.SafeColor
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecordClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeData by viewModel.homeData.collectAsState()
    val showRecordSheet by viewModel.showRecordSheet.collectAsState()
    val showNewCycleSheet by viewModel.showNewCycleSheet.collectAsState()
    val showEndCycleMenu by viewModel.showEndCycleMenu.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val recordMode by viewModel.recordMode.collectAsState()
    val existingRecord by viewModel.existingRecord.collectAsState()

    val hasCurrentCycle = when (val state = homeData) {
        is HomeUiState.Success -> state.hasCurrentCycle
        else -> false
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = homeData) {
            is HomeUiState.Loading -> {
                LoadingState()
            }
            is HomeUiState.NoData -> {
                NoDataState()
            }
            is HomeUiState.Success -> {
                HomeContent(
                    cycleDay = state.cycleDay,
                    daysUntilPeriod = state.daysUntilPeriod,
                    phase = state.phase,
                    hasCurrentCycle = state.hasCurrentCycle
                )
            }
        }

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.BottomEnd
        ) {
            if (hasCurrentCycle) {
                RecordFAB(
                    onClick = { viewModel.showEndCycleMenu() },
                    onEditClick = { viewModel.showRecordSheet() }
                )
            } else {
                RecordFAB(
                    onClick = { viewModel.showNewCycleSheet() }
                )
            }
        }
    }

    if (showRecordSheet) {
        RecordBottomSheet(
            initialDate = selectedDate,
            recordMode = recordMode,
            hasCurrentCycle = hasCurrentCycle,
            existingRecord = existingRecord,
            onDismiss = { viewModel.hideRecordSheet() },
            onSave = { date, flowLevel ->
                viewModel.saveDailyRecord(date, flowLevel)
            }
        )
    }

    if (showNewCycleSheet) {
        RecordBottomSheet(
            initialDate = selectedDate,
            recordMode = RecordMode.NEW_CYCLE,
            hasCurrentCycle = false,
            existingRecord = existingRecord,
            onDismiss = { viewModel.hideNewCycleSheet() },
            onSave = { date, flowLevel ->
                viewModel.saveNewCycle(date, flowLevel)
            }
        )
    }

    if (showEndCycleMenu) {
        EndCycleConfirmationDialog(
            onDismiss = { viewModel.hideEndCycleMenu() },
            onConfirm = { viewModel.endCycle() }
        )
    }
}

@Composable
private fun HomeContent(
    cycleDay: Int,
    daysUntilPeriod: Int,
    phase: CyclePhase,
    hasCurrentCycle: Boolean
) {
    val phaseData = getPhaseData(phase)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GreetingSection()

        MainStatusCard(
            cycleDay = cycleDay,
            daysUntilPeriod = daysUntilPeriod,
            phase = phase,
            phaseData = phaseData,
            hasCurrentCycle = hasCurrentCycle
        )

        PhaseDetailCard(
            phase = phase,
            phaseData = phaseData
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun GreetingSection() {
    val greeting = getGreeting()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "今天是你的专属时刻",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun MainStatusCard(
    cycleDay: Int,
    daysUntilPeriod: Int,
    phase: CyclePhase,
    phaseData: PhaseData,
    hasCurrentCycle: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, delayMillis = 100),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            phaseData.primary.copy(alpha = 0.08f),
                            phaseData.primary.copy(alpha = 0.02f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-60).dp)
                    .blur(
                        radius = 50.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
                    .background(
                        color = phaseData.primary.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = phaseData.container,
                    contentColor = phaseData.onContainer,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = phaseData.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = phase.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasCurrentCycle) {
                        StatItem(
                            value = "$cycleDay",
                            label = "周期天数",
                            highlightColor = phaseData.primary
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                        )
                    }

                    StatItem(
                        value = if (daysUntilPeriod == 0) "今天" else "$daysUntilPeriod",
                        label = "距离下次",
                        highlightColor = phaseData.primary,
                        valueUnit = if (daysUntilPeriod != 0) "天" else ""
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    highlightColor: Color,
    valueUnit: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
            if (valueUnit.isNotEmpty()) {
                Text(
                    text = valueUnit,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = highlightColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhaseDetailCard(
    phase: CyclePhase,
    phaseData: PhaseData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(phaseData.primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = phaseData.icon,
                        contentDescription = null,
                        tint = phaseData.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
                Text(
                    text = "了解你的身体",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = phase.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PhaseTip(phase = phase)
        }
    }
}

@Composable
private fun PhaseTip(phase: CyclePhase) {
    val tip = when (phase) {
        CyclePhase.MENSTRATION -> "记得多休息，补充铁质，保持温暖"
        CyclePhase.FOLLICULAR -> "这是能量回升的好时机，适合开始新计划"
        CyclePhase.OVULATION -> "精力最旺盛的时期，把握好状态"
        CyclePhase.FERTILE -> "注意身体变化，保持轻松心情"
        CyclePhase.LUTEAL -> "可能会有情绪波动，尝试放松活动"
        CyclePhase.SAFE -> "享受这段平稳的时光"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StatsCard(totalCycles: Int, hasData: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
                Column {
                    Text(
                        text = if (hasData) "已记录 $totalCycles 个周期" else "开始你的记录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (hasData) "坚持记录，让预测更精准" else "点击右下角开始",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RecordFAB(
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    if (onEditClick != null) {
        FloatingActionButtonMenu(
            expanded = expanded,
            button = {
                ToggleFloatingActionButton(
                    checked = expanded,
                    onCheckedChange = { expanded = it }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        ) {
            FloatingActionButtonMenuItem(
                text = { Text("结束周期") },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = "结束周期") },
                onClick = {
                    expanded = false
                    onClick()
                }
            )
            FloatingActionButtonMenuItem(
                text = { Text("修改记录") },
                icon = { Icon(Icons.Default.EditNote, contentDescription = "修改记录") },
                onClick = {
                    expanded = false
                    onEditClick()
                }
            )
        }
    } else {
        FloatingActionButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "开始新周期"
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoDataState() {
    val heartColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(300.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_woman_figure),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(heartColor)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "开启你的健康记录",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "点击右下角按钮，记录你的第一个周期，\n让我们一起好好照顾自己",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
    }
}


@Composable
private fun EndCycleConfirmationDialog(
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        }
    )
}

private fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "早上好"
        in 12..17 -> "下午好"
        else -> "晚上好"
    }
}

private data class PhaseData(
    val primary: Color,
    val container: Color,
    val onContainer: Color,
    val icon: ImageVector
)

private fun getPhaseData(phase: CyclePhase): PhaseData {
    return when (phase) {
        CyclePhase.MENSTRATION -> PhaseData(
            primary = MenstruationColor,
            container = MenstruationColor.copy(alpha = 0.15f),
            onContainer = MenstruationColor,
            icon = Icons.Default.WaterDrop
        )
        CyclePhase.OVULATION -> PhaseData(
            primary = OvulationColor,
            container = OvulationColor.copy(alpha = 0.15f),
            onContainer = OvulationColor,
            icon = Icons.Default.LocalFireDepartment
        )
        CyclePhase.FERTILE -> PhaseData(
            primary = FertileColor,
            container = FertileColor.copy(alpha = 0.15f),
            onContainer = FertileColor,
            icon = Icons.Default.Spa
        )
        CyclePhase.SAFE -> PhaseData(
            primary = SafeColor,
            container = SafeColor.copy(alpha = 0.15f),
            onContainer = SafeColor,
            icon = Icons.Default.Nightlight
        )
        CyclePhase.FOLLICULAR -> PhaseData(
            primary = FollicularColor,
            container = FollicularColor.copy(alpha = 0.15f),
            onContainer = FollicularColor,
            icon = Icons.Default.EnergySavingsLeaf
        )
        CyclePhase.LUTEAL -> PhaseData(
            primary = LutealColor,
            container = LutealColor.copy(alpha = 0.15f),
            onContainer = LutealColor,
            icon = Icons.Default.Nightlight
        )
    }
}
