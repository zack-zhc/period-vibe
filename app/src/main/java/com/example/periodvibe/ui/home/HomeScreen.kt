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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.domain.model.CyclePhase

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
    val showNewSymptomSheet by viewModel.showNewSymptomSheet.collectAsState()
    val showEndCycleMenu by viewModel.showEndCycleMenu.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val recordMode by viewModel.recordMode.collectAsState()
    val suggestedIsPeriod by viewModel.suggestedIsPeriod.collectAsState()
    val existingRecord by viewModel.existingRecord.collectAsState()

    val hasCurrentCycle = when (val state = homeData) {
        is HomeUiState.Success -> state.hasCurrentCycle
        else -> false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (hasCurrentCycle) {
                RecordFAB(
                    onClick = { viewModel.showEndCycleMenu() },
                    onEditClick = { viewModel.showRecordSheet() }
                )
            } else {
                RecordFABGroup(
                    onNewCycleClick = { viewModel.showNewCycleSheet() },
                    onNewSymptomClick = { viewModel.showNewSymptomSheet() }
                )
            }
        },
        bottomBar = {
            PeriodBottomNavigation(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "home" -> {}
                        "calendar" -> onCalendarClick()
                        "history" -> onHistoryClick()
                        "settings" -> onSettingsClick()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        totalCycles = state.totalCycles,
                        hasData = state.hasData
                    )
                }
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
            onSave = { date, flowLevel, symptoms, notes ->
                viewModel.saveDailyRecord(date, flowLevel, symptoms, notes)
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
            onSave = { date, flowLevel, symptoms, notes ->
                viewModel.saveNewCycle(date, flowLevel, symptoms, notes)
            }
        )
    }

    if (showNewSymptomSheet) {
        RecordBottomSheet(
            initialDate = selectedDate,
            recordMode = RecordMode.SYMPTOM_ONLY,
            hasCurrentCycle = false,
            existingRecord = existingRecord,
            onDismiss = { viewModel.hideNewSymptomSheet() },
            onSave = { date, flowLevel, symptoms, notes ->
                viewModel.saveNewSymptom(date, symptoms, notes)
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
    totalCycles: Int,
    hasData: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CycleStatusCard(
            cycleDay = cycleDay,
            daysUntilPeriod = daysUntilPeriod,
            phase = phase
        )

        Spacer(modifier = Modifier.height(8.dp))

        PhaseInfoCard(phase = phase)

        Spacer(modifier = Modifier.height(8.dp))

        TodaySummaryCard(
            totalCycles = totalCycles,
            hasData = hasData
        )
    }
}

@Composable
private fun CycleStatusCard(
    cycleDay: Int,
    daysUntilPeriod: Int,
    phase: CyclePhase
) {
    val phaseColor = when (phase) {
        CyclePhase.MENSTRATION -> Color(0xFFFF6B6B)
        CyclePhase.OVULATION -> Color(0xFF4ECDC4)
        CyclePhase.FERTILE -> Color(0xFFFFB6C1)
        CyclePhase.SAFE -> Color(0xFF95E1D3)
        CyclePhase.FOLLICULAR -> Color(0xFF95E1D3)
        CyclePhase.LUTEAL -> Color(0xFFFFB6C1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            phaseColor.copy(alpha = 0.1f),
                            phaseColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "第 $cycleDay 天",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "距离下次经期还有 $daysUntilPeriod 天",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                PhaseTag(phase = phase)
            }
        }
    }
}

@Composable
private fun PhaseTag(phase: CyclePhase) {
    val (backgroundColor, textColor) = when (phase) {
        CyclePhase.MENSTRATION -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
        CyclePhase.OVULATION -> Color(0xFFB2DFDB) to Color(0xFF00695C)
        CyclePhase.FERTILE -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
        CyclePhase.SAFE -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
        CyclePhase.FOLLICULAR -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
        CyclePhase.LUTEAL -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = phase.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PhaseInfoCard(phase: CyclePhase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "当前状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = phase.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(
    totalCycles: Int,
    hasData: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (hasData) "已记录 $totalCycles 个周期" else "开始记录您的第一个周期",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasData) "继续记录，让预测更准确" else "点击右下角按钮开始记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RecordFAB(
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded, onCheckedChange = { expanded = it }
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RecordFABGroup(
    onNewCycleClick: () -> Unit,
    onNewSymptomClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(checked = expanded, onCheckedChange = { expanded = it}) {
                Icon(imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            text = { Text("新建周期") },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "新建周期") },
            onClick = {
                expanded = false
                onNewCycleClick()
            }
        )
        FloatingActionButtonMenuItem(
            text = { Text("记录症状") },
            icon = { Icon(Icons.Default.EditNote, contentDescription = "记录症状") },
            onClick = {
                expanded = false
                onNewSymptomClick()
            }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoDataState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "还没有记录任何周期",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "点击右下角按钮开始记录您的第一个周期",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
