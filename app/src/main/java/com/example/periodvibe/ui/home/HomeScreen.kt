package com.example.periodvibe.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
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
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Context
import com.example.periodvibe.R
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.ui.home.EmptyStateCard
import com.example.periodvibe.ui.home.NextPhaseCard
import com.example.periodvibe.ui.home.PregnancyChanceCard
import com.example.periodvibe.ui.home.getNextPhase
import com.example.periodvibe.ui.home.getPregnancyChance
import com.example.periodvibe.ui.theme.FertileColor
import com.example.periodvibe.ui.theme.FollicularColor
import com.example.periodvibe.ui.theme.LutealColor
import com.example.periodvibe.ui.theme.MenstruationColor
import com.example.periodvibe.ui.theme.OvulationColor
import com.example.periodvibe.ui.theme.SafeColor
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecordClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showRecordSheetOnStart: Boolean = false,
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeData by viewModel.homeData.collectAsState()
    val showRecordSheet by viewModel.showRecordSheet.collectAsState()
    val showNewCycleSheet by viewModel.showNewCycleSheet.collectAsState()
    val showEndCycleMenu by viewModel.showEndCycleMenu.collectAsState()
    val showNewCycleConfirmation by viewModel.showNewCycleConfirmation.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val recordMode by viewModel.recordMode.collectAsState()
    val existingRecord by viewModel.existingRecord.collectAsState()

    val hasCurrentCycle = when (val state = homeData) {
        is HomeUiState.Success -> state.hasCurrentCycle
        else -> false
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 如果需要在启动时显示记录弹窗
    LaunchedEffect(showRecordSheetOnStart) {
        if (showRecordSheetOnStart) {
            if (hasCurrentCycle) {
                viewModel.showRecordSheet()
            } else {
                viewModel.showNewCycleSheet()
            }
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = homeData) {
            is HomeUiState.Loading -> {
                LoadingState()
            }
            is HomeUiState.NoData -> {
                NoDataState(darkTheme = darkTheme)
            }
            is HomeUiState.Success -> {
                HomeContent(
                    cycleDay = state.cycleDay,
                    daysUntilPeriod = state.daysUntilPeriod,
                    phase = state.phase,
                    hasCurrentCycle = state.hasCurrentCycle,
                    cycleLength = state.cycleLength,
                    daysUntilNextPhase = state.daysUntilNextPhase,
                    nextPhaseName = state.nextPhaseName,
                    ovulationDate = state.ovulationDate
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
                    hasCurrentCycle = true,
                    onClick = { viewModel.showEndCycleMenu() }
                )
            } else {
                RecordFAB(
                    hasCurrentCycle = false,
                    onClick = { viewModel.showNewCycleSheet() }
                )
            }
        }
    }

    if (showRecordSheet || showNewCycleSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                if (showRecordSheet) viewModel.hideRecordSheet()
                else viewModel.hideNewCycleSheet()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            RecordBottomSheetContent(
                initialDate = selectedDate,
                recordMode = if (showNewCycleSheet) RecordMode.NEW_CYCLE else recordMode,
                hasCurrentCycle = hasCurrentCycle,
                existingRecord = existingRecord,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (showRecordSheet) viewModel.hideRecordSheet()
                        else viewModel.hideNewCycleSheet()
                    }
                },
                onSave = { date, flowLevel ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (showNewCycleSheet) {
                            viewModel.saveNewCycle(date, flowLevel)
                        } else {
                            viewModel.saveDailyRecord(date, flowLevel)
                        }
                    }
                }
            )
        }
    }

    if (showEndCycleMenu) {
        EndCycleConfirmationDialog(
            onDismiss = { viewModel.hideEndCycleMenu() },
            onConfirm = { viewModel.endCycle() }
        )
    }

    if (showNewCycleConfirmation) {
        NewCycleConfirmationDialog(
            onDismiss = { viewModel.cancelNewCycle() },
            onConfirm = { viewModel.confirmNewCycle() }
        )
    }
}

@Composable
private fun HomeContent(
    cycleDay: Int,
    daysUntilPeriod: Int,
    phase: CyclePhase,
    hasCurrentCycle: Boolean,
    cycleLength: Int,
    daysUntilNextPhase: Int,
    nextPhaseName: String,
    ovulationDate: java.time.LocalDate?
) {
    val phaseData = getPhaseData(phase)
    val nextPhase = getNextPhase(phase)
    val pregnancyChance = getPregnancyChance(phase)

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
            phase = phase,
            phaseData = phaseData,
            cycleLength = cycleLength,
            daysUntilNextPhase = daysUntilNextPhase,
            nextPhaseName = nextPhaseName
        )

        // 信息卡片网格
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NextPhaseCard(
                phaseName = nextPhase.first,
                daysUntil = daysUntilPeriod,
                modifier = Modifier.weight(1f)
            )

            PregnancyChanceCard(
                chanceLevel = pregnancyChance.first,
                chanceLabel = pregnancyChance.second,
                isFertile = pregnancyChance.third,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun GreetingSection() {
    val greeting = getGreeting()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
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
}

@Composable
private fun MainStatusCard(
    cycleDay: Int,
    phase: CyclePhase,
    phaseData: PhaseData,
    cycleLength: Int,
    daysUntilNextPhase: Int,
    nextPhaseName: String
) {
    val progress = cycleDay.toFloat() / cycleLength.toFloat()
    val phaseTip = getPhaseTip(phase)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular Progress Ring
        CircularProgressRing(
            progress = progress,
            size = 256.dp,
            strokeWidth = 8.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            progressColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "第",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$cycleDay",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = phase.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Days until next phase
        Text(
            text = if (daysUntilNextPhase == 1) {
                "距离 $nextPhaseName 还有 1 天"
            } else {
                "距离 $nextPhaseName 还有 $daysUntilNextPhase 天"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Phase tip
        Text(
            text = phaseTip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private fun getPhaseTip(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRATION -> "记得多休息，补充铁质，注意保暖"
        CyclePhase.FOLLICULAR -> "能量正在回升，适合开始新计划或运动"
        CyclePhase.OVULATION -> "精力最旺盛的时期，好好把握"
        CyclePhase.FERTILE -> "注意身体变化，保持轻松心情"
        CyclePhase.LUTEAL -> "可能会有情绪波动，尝试放松活动"
        CyclePhase.SAFE -> "享受这段平稳的时光"
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

@Composable
private fun RecordFAB(
    hasCurrentCycle: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = if (hasCurrentCycle) Icons.Default.Check else Icons.Default.Add,
            contentDescription = if (hasCurrentCycle) "结束周期" else "开始新周期"
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
            LoadingIndicator()
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoDataState(darkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 问候语区域
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "欢迎！",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "开始记录你的健康旅程。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 主Hero卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 装饰性渐变背景
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 圆形图片 - 使用本地图片
                    Box(
                        modifier = Modifier
                            .size(192.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(
                                id = if (darkTheme) R.drawable.welcome_image_dark else R.drawable.welcome_image
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    // 标题
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 描述
                    Text(
                        text = "记录你的第一次周期，解锁个性化洞察和预测。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 空状态卡片网格
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 下一阶段卡片
            EmptyStateCard(
                icon = Icons.Default.AutoAwesome,
                title = "下一阶段",
                subtitle = "等待数据中...",
                modifier = Modifier.weight(1f)
            )

            // 怀孕追踪卡片
            EmptyStateCard(
                icon = Icons.Default.ChildCare,
                title = "怀孕追踪",
                subtitle = "未追踪",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
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

@Composable
private fun NewCycleConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "开始新周期",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "这将同时结束上一个周期。上一个周期的结束日期将设为昨天。确定要继续吗？",
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
