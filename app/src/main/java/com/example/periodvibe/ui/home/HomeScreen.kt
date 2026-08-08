package com.example.periodvibe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.R
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.ui.home.CombinedInfoCard
import com.example.periodvibe.ui.home.FeaturePreviewCard
import com.example.periodvibe.ui.theme.FertileColor
import com.example.periodvibe.ui.theme.FertileColorDark
import com.example.periodvibe.ui.theme.FollicularColor
import com.example.periodvibe.ui.theme.FollicularColorDark
import com.example.periodvibe.ui.theme.HomeCardShape
import com.example.periodvibe.ui.theme.LutealColor
import com.example.periodvibe.ui.theme.LutealColorDark
import com.example.periodvibe.ui.theme.MenstruationColor
import com.example.periodvibe.ui.theme.MenstruationColorDark
import com.example.periodvibe.ui.theme.OvulationColor
import com.example.periodvibe.ui.theme.OvulationColorDark
import com.example.periodvibe.ui.theme.SafeColor
import com.example.periodvibe.ui.theme.SafeColorDark
import com.example.periodvibe.ui.theme.cardBorderStroke
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCalendarClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showRecordSheetOnStart: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeData by viewModel.homeData.collectAsState()
    val showRecordSheet by viewModel.showRecordSheet.collectAsState()
    val showNewCycleSheet by viewModel.showNewCycleSheet.collectAsState()
    val showEndCycleMenu by viewModel.showEndCycleMenu.collectAsState()
    val showNewCycleConfirmation by viewModel.showNewCycleConfirmation.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val existingRecord by viewModel.existingRecord.collectAsState()
    val errorMessageRes by viewModel.errorMessage.collectAsState()

    val hasCurrentCycle = when (val state = homeData) {
        is HomeUiState.Success -> state.hasCurrentCycle
        else -> false
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessageText = errorMessageRes?.let { stringResource(it) }
    LaunchedEffect(errorMessageText) {
        errorMessageText?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

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

    Box(
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
                    ovulationDate = state.ovulationDate,
                    darkTheme = darkTheme
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            RecordFAB(
                hasCurrentCycle = hasCurrentCycle,
                onClick = {
                    if (hasCurrentCycle) {
                        viewModel.showRecordSheet()
                    } else {
                        viewModel.showNewCycleSheet()
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
    ovulationDate: java.time.LocalDate?,
    darkTheme: Boolean = false
) {
    val phaseData = getPhaseData(phase, darkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 560.dp)
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
            hasCurrentCycle = hasCurrentCycle,
            cycleLength = cycleLength,
            daysUntilPeriod = daysUntilPeriod,
            daysUntilNextPhase = daysUntilNextPhase,
            nextPhaseName = nextPhaseName,
            ovulationDate = ovulationDate
        )

        // 合并信息卡片
        CombinedInfoCard(
            phase = phase,
            nextPhaseName = nextPhaseName,
            daysUntilNextPhase = daysUntilNextPhase
        )

        Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun GreetingSection(modifier: Modifier = Modifier) {
    val greeting = getGreeting()
    val dateText = remember {
        java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("M月d日 · EEEE", java.util.Locale.CHINESE)
        )
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = dateText,
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
    hasCurrentCycle: Boolean,
    cycleLength: Int,
    daysUntilPeriod: Int,
    daysUntilNextPhase: Int,
    nextPhaseName: String,
    ovulationDate: java.time.LocalDate?,
    modifier: Modifier = Modifier
) {
    val progress = if (hasCurrentCycle) {
        cycleDay.toFloat() / cycleLength.toFloat()
    } else {
        (cycleLength - daysUntilPeriod).toFloat() / cycleLength.toFloat()
    }
    // 预测模式且预测日期已过时，不再显示误导性的"第 X 天"
    val showPredictedCountdown = !hasCurrentCycle && daysUntilPeriod > 0
    val mainText = when {
        !hasCurrentCycle && daysUntilPeriod == 0 -> "月经临近，记得记录开始时间"
        !hasCurrentCycle -> "预计下次月经还有 $daysUntilPeriod 天"
        daysUntilPeriod == 0 -> "月经预计今天来"
        daysUntilPeriod == 1 -> "距离下次月经还有 1 天"
        phase == CyclePhase.MENSTRATION -> "本次月经第 $cycleDay 天"
        else -> "距离下次月经还有 $daysUntilPeriod 天"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = cardBorderStroke()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            phaseData.container,
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // Circular Progress Ring
        CircularProgressRing(
            progress = progress,
            size = 200.dp,
            strokeWidth = 14.dp,
            backgroundColor = phaseData.container,
            progressColor = phaseData.primary,
            contentDescription = if (hasCurrentCycle) {
                "周期进度，第 $cycleDay 天"
            } else {
                "预计下次月经还有 $daysUntilPeriod 天"
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (hasCurrentCycle) {
                    Text(
                        text = "第",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$cycleDay",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = phaseData.primary
                    )
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (showPredictedCountdown) {
                    Text(
                        text = "还有",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$daysUntilPeriod",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = phaseData.primary
                    )
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "今天",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = phaseData.primary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = phase.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = phaseData.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Days until next period
        Text(
            text = mainText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // 预计排卵日
        if (ovulationDate != null) {
            Text(
                text = "预计排卵日 ${ovulationDate.monthValue}月${ovulationDate.dayOfMonth}日",
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null
            )
        },
        text = {
            Text(
                text = if (hasCurrentCycle) "记录" else "开始"
            )
        }
    )
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LoadingIndicator(modifier = Modifier.size(96.dp))
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoDataState(darkTheme: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
            shape = HomeCardShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = cardBorderStroke()
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
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
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
                        Image(
                            painter = painterResource(
                                id = if (darkTheme) R.drawable.welcome_image_dark else R.drawable.welcome_image
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 标题
                    Text(
                        text = "一切从今天开始",
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

        // 功能预告卡片
        FeaturePreviewCard()

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun EndCycleConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
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
        },
        modifier = modifier
    )
}

@Composable
internal fun NewCycleConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
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
        },
        modifier = modifier
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
    val icon: ImageVector
)

private fun getPhaseData(phase: CyclePhase, darkTheme: Boolean = false): PhaseData {
    fun pick(light: Color, dark: Color) = if (darkTheme) dark else light
    return when (phase) {
        CyclePhase.MENSTRATION -> PhaseData(
            primary = pick(MenstruationColor, MenstruationColorDark),
            container = MenstruationColor.copy(alpha = 0.15f),
            icon = PhaseIcons.Menstruation
        )
        CyclePhase.OVULATION -> PhaseData(
            primary = pick(OvulationColor, OvulationColorDark),
            container = OvulationColor.copy(alpha = 0.15f),
            icon = PhaseIcons.Ovulation
        )
        CyclePhase.FERTILE -> PhaseData(
            primary = pick(FertileColor, FertileColorDark),
            container = FertileColor.copy(alpha = 0.15f),
            icon = PhaseIcons.Fertile
        )
        CyclePhase.SAFE -> PhaseData(
            primary = pick(SafeColor, SafeColorDark),
            container = SafeColor.copy(alpha = 0.15f),
            icon = PhaseIcons.Safe
        )
        CyclePhase.FOLLICULAR -> PhaseData(
            primary = pick(FollicularColor, FollicularColorDark),
            container = FollicularColor.copy(alpha = 0.15f),
            icon = PhaseIcons.Follicular
        )
        CyclePhase.LUTEAL -> PhaseData(
            primary = pick(LutealColor, LutealColorDark),
            container = LutealColor.copy(alpha = 0.15f),
            icon = PhaseIcons.Luteal
        )
    }
}

// region Preview

@Preview(showBackground = true, name = "首页 - 月经期")
@Composable
private fun HomeScreenPreview_Menstruation() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeContent(
                    cycleDay = 3,
                    daysUntilPeriod = 0,
                    phase = CyclePhase.MENSTRATION,
                    cycleLength = 28,
                    hasCurrentCycle = true,
                    daysUntilNextPhase = 4,
                    nextPhaseName = "卵泡期",
                    ovulationDate = null
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "首页 - 排卵期")
@Composable
private fun HomeScreenPreview_Ovulation() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeContent(
                    cycleDay = 14,
                    daysUntilPeriod = 14,
                    phase = CyclePhase.OVULATION,
                    cycleLength = 28,
                    hasCurrentCycle = true,
                    daysUntilNextPhase = 2,
                    nextPhaseName = "黄体期",
                    ovulationDate = null
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "首页 - 卵泡期")
@Composable
private fun HomeScreenPreview_Follicular() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeContent(
                    cycleDay = 8,
                    daysUntilPeriod = 20,
                    phase = CyclePhase.FOLLICULAR,
                    cycleLength = 28,
                    hasCurrentCycle = true,
                    daysUntilNextPhase = 6,
                    nextPhaseName = "排卵期",
                    ovulationDate = null
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "首页 - 黄体期")
@Composable
private fun HomeScreenPreview_Luteal() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeContent(
                    cycleDay = 22,
                    daysUntilPeriod = 6,
                    phase = CyclePhase.LUTEAL,
                    cycleLength = 28,
                    hasCurrentCycle = true,
                    daysUntilNextPhase = 6,
                    nextPhaseName = "月经期",
                    ovulationDate = null
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "首页 - 安全期")
@Composable
private fun HomeScreenPreview_Safe() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeContent(
                    cycleDay = 26,
                    daysUntilPeriod = 2,
                    phase = CyclePhase.SAFE,
                    cycleLength = 28,
                    hasCurrentCycle = true,
                    daysUntilNextPhase = 2,
                    nextPhaseName = "月经期",
                    ovulationDate = null
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "首页 - 空状态")
@Composable
private fun HomeScreenPreview_NoData() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NoDataState(darkTheme = false)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = false,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "首页 - 加载中")
@Composable
private fun HomeScreenPreview_Loading() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LoadingState()
            }
        }
    }
}

// region 新合并卡片 Preview

@Preview(showBackground = true, name = "合并卡片 - 月经期")
@Composable
private fun CombinedInfoCardPreview_Menstruation() {
    PeriodVibeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            CombinedInfoCard(
                phase = CyclePhase.MENSTRATION
            )
        }
    }
}

@Preview(showBackground = true, name = "合并卡片 - 排卵期")
@Composable
private fun CombinedInfoCardPreview_Ovulation() {
    PeriodVibeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            CombinedInfoCard(
                phase = CyclePhase.OVULATION
            )
        }
    }
}

@Preview(showBackground = true, name = "首页 - 使用合并卡片（备选方案）")
@Composable
private fun HomeScreenPreview_WithCombinedCard() {
    PeriodVibeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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
                        cycleDay = 3,
                        phase = CyclePhase.MENSTRATION,
                        phaseData = getPhaseData(CyclePhase.MENSTRATION),
                        hasCurrentCycle = true,
                        cycleLength = 28,
                        daysUntilPeriod = 0,
                        daysUntilNextPhase = 4,
                        nextPhaseName = "卵泡期",
                        ovulationDate = null
                    )

                    // 使用合并卡片
                    CombinedInfoCard(
                        phase = CyclePhase.MENSTRATION
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    RecordFAB(
                        hasCurrentCycle = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

// endregion
