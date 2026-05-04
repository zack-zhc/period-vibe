package com.example.periodvibe.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.ui.home.RecordBottomSheetContent
import com.example.periodvibe.ui.home.RecordMode
import com.example.periodvibe.ui.theme.CalendarFertileDark
import com.example.periodvibe.ui.theme.CalendarFertileLight
import com.example.periodvibe.ui.theme.CalendarOvulationDark
import com.example.periodvibe.ui.theme.CalendarOvulationLight
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight
import kotlinx.coroutines.launch
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDateClick: (java.time.LocalDate) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onLegendClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val calendarPages by viewModel.calendarPages.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activeCycle by viewModel.activeCycle.collectAsState()
    val showEndCycleDialog by viewModel.showEndCycleDialog.collectAsState()
    var showRecordSheet by remember { mutableStateOf(false) }
    var recordDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var recordMode by remember { mutableStateOf(RecordMode.AUTO) }

    val hasCurrentCycle = activeCycle != null && activeCycle?.isCurrentCycle == true

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

    // 当 Pager 页面变化时通知 ViewModel
    LaunchedEffect(currentPageYearMonth) {
        viewModel.onPageChanged(currentPageYearMonth)
    }

    // 当 ViewModel 的 currentYearMonth 变化时（比如点击"今天"按钮），同步 Pager
    LaunchedEffect(currentYearMonth) {
        val targetPage = (currentYearMonth.year - startMonth.year) * 12 +
            (currentYearMonth.monthValue - startMonth.monthValue)
        if (targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .let { if (scrollBehavior != null) it.nestedScroll(scrollBehavior.nestedScrollConnection) else it }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CalendarContent(
                pagerState = pagerState,
                startMonth = startMonth,
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
                onTodayClick = { viewModel.navigateToToday() },
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
                    recordMode = RecordMode.AUTO
                    showRecordSheet = true
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showRecordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecordSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            RecordBottomSheetContent(
                initialDate = recordDate,
                recordMode = recordMode,
                hasCurrentCycle = hasCurrentCycle,
                existingRecord = null,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showRecordSheet = false
                    }
                },
                onSave = { date, flowLevel ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModel.saveRecord(date, recordMode, flowLevel)
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
    calendarPages: Map<YearMonth, CalendarUiState>,
    selectedDate: java.time.LocalDate?,
    activeCycle: com.example.periodvibe.domain.model.Cycle?,
    onDateClick: (java.time.LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onRecordClick: (java.time.LocalDate) -> Unit,
    onEndCycleClick: () -> Unit,
    onNewCycleClick: (java.time.LocalDate) -> Unit,
    onEditClick: (java.time.LocalDate) -> Unit
) {
    val currentPageYearMonth = remember(pagerState.currentPage) {
        startMonth.plusMonths(pagerState.currentPage.toLong())
    }
    val currentUiState = calendarPages[currentPageYearMonth]

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 月份头部 - 固定显示当前页面的月份
        CalendarMonthHeader(
            yearMonth = currentPageYearMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onTodayClick = onTodayClick
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
                        onDateClick = onDateClick
                    )
                }
                null -> {
                    LoadingState()
                }
            }
        }

        CalendarLegend()

        if (currentUiState is CalendarUiState.Success && selectedDate != null) {
            val date = selectedDate
            val selectedDay = currentUiState.days.find {
                it is com.example.periodvibe.domain.usecase.CalendarDay.Data && it.date == date
            }
            if (selectedDay is com.example.periodvibe.domain.usecase.CalendarDay.Data) {
                SmartActionCard(
                    day = selectedDay,
                    activeCycle = activeCycle,
                    onRecordClick = { onRecordClick(date) },
                    onEndCycleClick = onEndCycleClick,
                    onNewCycleClick = { onNewCycleClick(date) },
                    onEditClick = { onEditClick(date) }
                )
            }
        }

        if (selectedDate == null) {
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
fun LegendDialog(onDismiss: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background == androidx.compose.ui.graphics.Color(0xFF1F1A1B)
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val ovulationColor = if (isDark) CalendarOvulationDark else CalendarOvulationLight
    val fertileColor = if (isDark) CalendarFertileDark else CalendarFertileLight

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "图例说明",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = periodColor, label = "已记录经期", isPredicted = false)
                LegendItem(color = periodColor, label = "预测经期", isPredicted = true)
                LegendItem(color = ovulationColor, label = "排卵期", isPredicted = false)
                LegendItem(color = fertileColor, label = "易孕期", isPredicted = false)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

@Composable
private fun LegendItem(color: Color, label: String, isPredicted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (isPredicted) 0.5f else 1f))
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
