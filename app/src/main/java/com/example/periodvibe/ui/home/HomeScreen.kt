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
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.ui.theme.FertileColor
import com.example.periodvibe.ui.theme.FollicularColor
import com.example.periodvibe.ui.theme.LutealColor
import com.example.periodvibe.ui.theme.MenstruationColor
import com.example.periodvibe.ui.theme.OvulationColor
import com.example.periodvibe.ui.theme.SafeColor

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 问候区域
        GreetingSection()

        // 主要周期状态卡片 (Expressive Large Card)
        CycleStatusCard(
            cycleDay = cycleDay,
            daysUntilPeriod = daysUntilPeriod,
            phase = phase
        )

        // 阶段信息卡片 (Expressive Medium Card)
        PhaseInfoCard(phase = phase)

        // 今日摘要卡片
        TodaySummaryCard(
            totalCycles = totalCycles,
            hasData = hasData
        )

        // 额外的间距
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun GreetingSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "你好",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "关注你的身体变化",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CycleStatusCard(
    cycleDay: Int,
    daysUntilPeriod: Int,
    phase: CyclePhase
) {
    val phaseData = getPhaseData(phase)
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500),
        label = "card_scale"
    )

    // Expressive Card - 更大的圆角、更丰富的色彩渐变
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            phaseData.primary.copy(alpha = 0.3f),
                            phaseData.primary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(28.dp)
        ) {
            // 装饰性模糊圆形
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .blur(
                        radius = 40.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
                    .background(
                        color = phaseData.primary.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 周期天数 - Display 级别文字
                Text(
                    text = "第 $cycleDay 天",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = phaseData.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 周期天数标签
                Text(
                    text = "本周期",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 距离下次经期 - Expressive 样式
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (daysUntilPeriod == 0) "今天" else "$daysUntilPeriod 天",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = phaseData.primary
                        )
                        Text(
                            text = "距离下次经期",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 阶段标签
                PhaseTag(phase = phase, phaseData = phaseData)
            }
        }
    }
}

@Composable
private fun PhaseTag(phase: CyclePhase, phaseData: PhaseData) {
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
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = phaseData.primary,
                        shape = CircleShape
                    )
            )
            Text(
                text = phase.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PhaseInfoCard(phase: CyclePhase) {
    val phaseData = getPhaseData(phase)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = phaseData.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "当前状态",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = phase.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (hasData) "已记录 $totalCycles 个周期" else "开始你的第一个周期",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasData) "继续记录，让预测更准确" else "点击右下角按钮开始记录",
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "还没有记录",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "点击右下角按钮开始记录你的第一个周期",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
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

// 阶段数据类
private data class PhaseData(
    val primary: Color,
    val container: Color,
    val onContainer: Color
)

// 获取阶段数据
private fun getPhaseData(phase: CyclePhase): PhaseData {
    return when (phase) {
        CyclePhase.MENSTRATION -> PhaseData(
            primary = MenstruationColor,
            container = MenstruationColor.copy(alpha = 0.15f),
            onContainer = MenstruationColor
        )
        CyclePhase.OVULATION -> PhaseData(
            primary = OvulationColor,
            container = OvulationColor.copy(alpha = 0.15f),
            onContainer = OvulationColor
        )
        CyclePhase.FERTILE -> PhaseData(
            primary = FertileColor,
            container = FertileColor.copy(alpha = 0.15f),
            onContainer = FertileColor
        )
        CyclePhase.SAFE -> PhaseData(
            primary = SafeColor,
            container = SafeColor.copy(alpha = 0.15f),
            onContainer = SafeColor
        )
        CyclePhase.FOLLICULAR -> PhaseData(
            primary = FollicularColor,
            container = FollicularColor.copy(alpha = 0.15f),
            onContainer = FollicularColor
        )
        CyclePhase.LUTEAL -> PhaseData(
            primary = LutealColor,
            container = LutealColor.copy(alpha = 0.15f),
            onContainer = LutealColor
        )
    }
}
