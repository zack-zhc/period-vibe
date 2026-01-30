package com.example.periodvibe.ui.calendar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.ui.home.PeriodBottomNavigation
import com.example.periodvibe.ui.home.RecordBottomSheet
import com.example.periodvibe.ui.home.RecordMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDateClick: (java.time.LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val calendarData by viewModel.calendarData.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activeCycle by viewModel.activeCycle.collectAsState()
    val showEndCycleDialog by viewModel.showEndCycleDialog.collectAsState()
    val showLegendDialog by viewModel.showLegendDialog.collectAsState()
    var showRecordSheet by remember { mutableStateOf(false) }
    var recordDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var recordMode by remember { mutableStateOf(RecordMode.AUTO) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日历",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.showLegendDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "图例"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            PeriodBottomNavigation(
                currentRoute = "calendar",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "calendar" -> {}
                        "history" -> onNavigateToHistory()
                        "settings" -> onNavigateToSettings()
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
            when (val state = calendarData) {
                is CalendarUiState.Loading -> {
                    LoadingState()
                }
                is CalendarUiState.Success -> {
                    CalendarContent(
                        yearMonth = state.yearMonth,
                        days = state.days,
                        selectedDate = selectedDate,
                        activeCycle = activeCycle,
                        onDateClick = { date ->
                            viewModel.selectDate(date)
                            onDateClick(date)
                        },
                        onPreviousMonth = { viewModel.navigateToPreviousMonth() },
                        onNextMonth = { viewModel.navigateToNextMonth() },
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
                        },
                        onRecordSymptomClick = { date ->
                            recordDate = date
                            recordMode = RecordMode.SYMPTOM_ONLY
                            showRecordSheet = true
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
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

    if (showLegendDialog) {
        LegendDialog(
            onDismiss = { viewModel.hideLegendDialog() }
        )
    }

    val currentActiveCycle = activeCycle
    if (showRecordSheet) {
        RecordBottomSheet(
            initialDate = recordDate,
            recordMode = recordMode,
            hasCurrentCycle = currentActiveCycle != null && currentActiveCycle.isCurrentCycle,
            existingRecord = null,
            onDismiss = { showRecordSheet = false },
            onSave = { date, flowLevel, symptoms, notes ->
                viewModel.saveRecord(date, recordMode, flowLevel, symptoms, notes)
                showRecordSheet = false
            }
        )
    }
}

@Composable
private fun CalendarContent(
    yearMonth: java.time.YearMonth,
    days: List<com.example.periodvibe.domain.usecase.CalendarDay>,
    selectedDate: java.time.LocalDate?,
    activeCycle: com.example.periodvibe.domain.model.Cycle?,
    onDateClick: (java.time.LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onRecordClick: (java.time.LocalDate) -> Unit,
    onEndCycleClick: () -> Unit,
    onNewCycleClick: (java.time.LocalDate) -> Unit,
    onEditClick: (java.time.LocalDate) -> Unit,
    onRecordSymptomClick: (java.time.LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CalendarMonthHeader(
            yearMonth = yearMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onTodayClick = onTodayClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(
            yearMonth = yearMonth,
            days = days,
            selectedDate = selectedDate,
            onDateClick = onDateClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDate != null) {
            val date = selectedDate
            val selectedDay = days.find { 
                it is com.example.periodvibe.domain.usecase.CalendarDay.Data && it.date == date 
            }
            if (selectedDay is com.example.periodvibe.domain.usecase.CalendarDay.Data) {
                SmartActionCard(
                    day = selectedDay,
                    activeCycle = activeCycle,
                    onRecordClick = { onRecordClick(date) },
                    onEndCycleClick = onEndCycleClick,
                    onNewCycleClick = { onNewCycleClick(date) },
                    onEditClick = { onEditClick(date) },
                    onRecordSymptomClick = { onRecordSymptomClick(date) }
                )
            }
        } else {
            EmptySelectionCard()
        }
    }
}

@Composable
private fun EmptySelectionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📅",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "选择日期查看详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "点击日历中的任意日期，查看该日期的详细信息并进行记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "图例",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem(
                    color = Color(0xFFFF6B6B),
                    label = "已记录经期"
                )

                LegendItem(
                    color = Color(0xFFFFCDD2),
                    label = "预测经期"
                )

                LegendItem(
                    color = Color(0xFF4ECDC4),
                    label = "排卵期"
                )

                LegendItem(
                    color = Color(0xFFFFB6C1),
                    label = "易孕期"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
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
