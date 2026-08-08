package com.example.periodvibe.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.periodvibe.R
import com.example.periodvibe.domain.model.RecordMode
import com.example.periodvibe.domain.usecase.CalendarDay
import com.example.periodvibe.ui.home.NewCycleConfirmationDialog
import com.example.periodvibe.ui.home.RecordBottomSheetContent
import com.example.periodvibe.ui.theme.CalendarFertileDark
import com.example.periodvibe.ui.theme.CalendarFertileLight
import com.example.periodvibe.ui.theme.CalendarOvulationDark
import com.example.periodvibe.ui.theme.CalendarOvulationLight
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDateClick: (java.time.LocalDate) -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val calendarPages by viewModel.calendarPages.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activeCycle by viewModel.activeCycle.collectAsState()
    val showEndCycleDialog by viewModel.showEndCycleDialog.collectAsState()
    val showNewCycleConfirmation by viewModel.showNewCycleConfirmation.collectAsState()
    val errorMessageRes by viewModel.errorMessage.collectAsState()
    var showRecordSheet by remember { mutableStateOf(false) }
    var recordDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var recordMode by remember { mutableStateOf(RecordMode.AUTO) }

    val hasCurrentCycle = activeCycle?.isCurrentCycle == true

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessageText = errorMessageRes?.let { stringResource(it) }

    // 保存/结束周期失败时通过 Snackbar 反馈
    LaunchedEffect(errorMessageText) {
        errorMessageText?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    // 回到前台时刷新缓存的月份数据，避免其他页面（如历史页）修改后被旧缓存覆盖
    LifecycleResumeEffect(Unit) {
        viewModel.refreshAllCachedMonths()
        onPauseOrDispose { }
    }

    // 生成一个足够大的月份范围作为 Pager 的页面
    val startMonth = remember { YearMonth.now().minusMonths(120) } // 10年前
    val endMonth = remember { YearMonth.now().plusMonths(120) }  // 10年后
    val totalMonths = remember {
        val years = endMonth.year - startMonth.year
        years * 12 + (endMonth.monthValue - startMonth.monthValue + 1)
    }
    val initialPage = remember {
        val now = YearMonth.now()
        val years = now.year - startMonth.year
        years * 12 + (now.monthValue - startMonth.monthValue)
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        initialPageOffsetFraction = 0f,
        pageCount = { totalMonths }
    )

    val coroutineScope = rememberCoroutineScope()

    // 当前页面显示的 YearMonth
    val currentPageYearMonth by remember {
        derivedStateOf {
            startMonth.plusMonths(pagerState.currentPage.toLong())
        }
    }

    // 当 Pager 页面停稳后通知 ViewModel，避免动画/快速滑动时逐页重复触发
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { (page, isScrolling) ->
                if (!isScrolling) {
                    viewModel.onPageChanged(startMonth.plusMonths(page.toLong()))
                }
            }
    }

    // 当 ViewModel 的 currentYearMonth 变化时（比如点击"今天"按钮），同步 Pager
    LaunchedEffect(currentYearMonth) {
        val targetPage = (currentYearMonth.year - startMonth.year) * 12 +
            (currentYearMonth.monthValue - startMonth.monthValue)
        if (targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val todayYearMonth = remember { YearMonth.now() }
    val isCurrentMonth = currentPageYearMonth == todayYearMonth

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CalendarContent(
                pagerState = pagerState,
                startMonth = startMonth,
                currentPageYearMonth = currentPageYearMonth,
                darkTheme = darkTheme,
                calendarPages = calendarPages,
                selectedDate = selectedDate,
                activeCycle = activeCycle,
                onDateClick = { date ->
                    viewModel.selectDate(date)
                    onDateClick(date)
                },
                onPreviousMonth = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNextMonth = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < totalMonths - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onRecordClick = { date ->
                    recordDate = date
                    recordMode = RecordMode.AUTO
                    showRecordSheet = true
                },
                onEndCycleClick = { viewModel.showEndCycleDialog() },
                onNewCycleClick = { date ->
                    recordDate = date
                    recordMode = RecordMode.NEW_CYCLE
                    showRecordSheet = true
                },
                onEditClick = { date ->
                    recordDate = date
                    recordMode = RecordMode.EDIT
                    showRecordSheet = true
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }

        // 今天按钮 FAB - 仅在不在当前月份时显示
        if (!isCurrentMonth) {
            androidx.compose.material3.FloatingActionButton(
                onClick = { viewModel.navigateToToday() },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.cal_today_fab),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 操作失败的 Snackbar - FAB 可见时抬高，避免重叠
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (!isCurrentMonth) 104.dp else 24.dp)
        )
    }

    if (showEndCycleDialog) {
        EndCycleConfirmationDialog(
            onDismiss = { viewModel.hideEndCycleDialog() },
            onConfirm = {
                selectedDate?.let { date ->
                    viewModel.endCycle(date)
                }
            }
        )
    }

    if (showNewCycleConfirmation) {
        NewCycleConfirmationDialog(
            onDismiss = { viewModel.cancelNewCycle() },
            onConfirm = { viewModel.confirmNewCycle() }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 获取选中日期的记录
    val selectedDateRecord by remember(selectedDate, currentPageYearMonth, calendarPages) {
        derivedStateOf {
            selectedDate?.let { date ->
                val yearMonth = YearMonth.from(date)
                val uiState = calendarPages[yearMonth]
                if (uiState is CalendarUiState.Success) {
                    uiState.days
                        .filterIsInstance<CalendarDay.Data>()
                        .find { it.date == date }
                        ?.record
                } else {
                    null
                }
            }
        }
    }

    if (showRecordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecordSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            RecordBottomSheetContent(
                initialDate = recordDate,
                hasCurrentCycle = hasCurrentCycle,
                existingRecord = selectedDateRecord,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showRecordSheet = false
                    }
                },
                onSave = { date, flowLevel ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModel.saveRecord(date, recordMode, flowLevel, selectedDateRecord)
                        showRecordSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun CalendarContent(
    pagerState: androidx.compose.foundation.pager.PagerState,
    startMonth: YearMonth,
    currentPageYearMonth: YearMonth,
    darkTheme: Boolean,
    calendarPages: Map<YearMonth, CalendarUiState>,
    selectedDate: java.time.LocalDate?,
    activeCycle: com.example.periodvibe.domain.model.Cycle?,
    onDateClick: (java.time.LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRecordClick: (java.time.LocalDate) -> Unit,
    onEndCycleClick: () -> Unit,
    onNewCycleClick: (java.time.LocalDate) -> Unit,
    onEditClick: (java.time.LocalDate) -> Unit
) {
    val currentUiState = calendarPages[currentPageYearMonth]

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 月份头部 - 固定显示当前页面的月份
        CalendarMonthHeader(
            yearMonth = currentPageYearMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )

        // HorizontalPager - 日历内容滑动区域
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            key = { page -> startMonth.plusMonths(page.toLong()) }
        ) { page ->
            val yearMonth = startMonth.plusMonths(page.toLong())
            val uiState = calendarPages[yearMonth]

            when (uiState) {
                is CalendarUiState.Loading -> {
                    LoadingState()
                }
                is CalendarUiState.Success -> {
                    CalendarGrid(
                        yearMonth = yearMonth,
                        days = uiState.days,
                        selectedDate = if (yearMonth == currentPageYearMonth) selectedDate else null,
                        darkTheme = darkTheme,
                        onDateClick = onDateClick
                    )
                }
                null -> {
                    LoadingState()
                }
            }
        }

        CalendarLegend(darkTheme = darkTheme)

        // 选中的日期在可见月份中才显示操作卡；否则显示空状态卡，避免翻页过渡时空白
        val selectedDayInMonth = if (currentUiState is CalendarUiState.Success && selectedDate != null) {
            currentUiState.days
                .filterIsInstance<CalendarDay.Data>()
                .find { it.date == selectedDate }
        } else {
            null
        }

        if (selectedDayInMonth != null) {
            SmartActionCard(
                day = selectedDayInMonth,
                activeCycle = activeCycle,
                onRecordClick = { onRecordClick(selectedDayInMonth.date) },
                onEndCycleClick = onEndCycleClick,
                onNewCycleClick = { onNewCycleClick(selectedDayInMonth.date) },
                onEditClick = { onEditClick(selectedDayInMonth.date) }
            )
        } else {
            EmptySelectionCard()
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Composable
fun LegendDialog(
    onDismiss: () -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val periodColor = if (darkTheme) CalendarPeriodDark else CalendarPeriodLight
    val ovulationColor = if (darkTheme) CalendarOvulationDark else CalendarOvulationLight
    val fertileColor = if (darkTheme) CalendarFertileDark else CalendarFertileLight

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.cal_legend_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    label = stringResource(R.string.cal_legend_today_selected),
                    isPredicted = false,
                    isTodayOrSelected = true
                )
                LegendItem(color = periodColor, label = stringResource(R.string.cal_legend_recorded_period), isPredicted = false)
                LegendItem(color = periodColor, label = stringResource(R.string.cal_legend_predicted_period), isPredicted = true)
                LegendItem(color = ovulationColor, label = stringResource(R.string.cal_legend_ovulation), isPredicted = false)
                LegendItem(color = fertileColor, label = stringResource(R.string.cal_legend_fertile), isPredicted = false)
                Text(
                    text = stringResource(R.string.cal_legend_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cal_legend_ok))
            }
        }
    )
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isPredicted: Boolean,
    isTodayOrSelected: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .then(
                    if (isTodayOrSelected) {
                        Modifier
                            .background(color)
                            .shadow(elevation = 4.dp, shape = CircleShape)
                    } else {
                        Modifier.background(color.copy(alpha = if (isPredicted) 0.5f else 1f))
                    }
                )
                .then(
                    if (isPredicted) {
                        Modifier.border(
                            width = 1.5.dp,
                            color = color,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
