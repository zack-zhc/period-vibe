package com.example.periodvibe.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.usecase.CycleWithRecords
import com.example.periodvibe.ui.history.components.DailyRecordRow
import com.example.periodvibe.ui.history.components.EditModeBottomBar
import com.example.periodvibe.ui.home.RecordBottomSheetContent
import com.example.periodvibe.ui.home.RecordMode
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateHomeToRecord: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyData by viewModel.historyData.collectAsState()
    val selectedCycleId by viewModel.selectedCycleId.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showDeleteRecordDialog by viewModel.showDeleteRecordDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val selectedCycles by viewModel.selectedCycles.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF1F1A1B)

    // 检查是否有数据
    val hasData = when (val state = historyData) {
        is HistoryUiState.Success -> state.hasData
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (hasData) {
                        TextButton(onClick = { viewModel.toggleEditMode() }) {
                            Text(if (isEditMode) "取消" else "编辑")
                        }
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isEditMode,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                EditModeBottomBar(
                    selectedCount = selectedCycles.size,
                    onDeleteClick = { viewModel.deleteSelectedCycles() }
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = historyData) {
                is HistoryUiState.Loading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                is HistoryUiState.Success -> {
                    if (state.hasData) {
                        HistoryContent(
                            cycles = state.cycles,
                            selectedCycleId = selectedCycleId,
                            isEditMode = isEditMode,
                            selectedCycles = selectedCycles,
                            onCycleClick = { cycleId ->
                                if (isEditMode) {
                                    viewModel.toggleCycleSelection(cycleId)
                                } else {
                                    if (selectedCycleId == cycleId) {
                                        viewModel.deselectCycle()
                                    } else {
                                        viewModel.selectCycle(cycleId)
                                    }
                                }
                            },
                            onCycleLongClick = { cycleId ->
                                if (!isEditMode) {
                                    viewModel.showDeleteDialog(cycleId)
                                }
                            },
                            onRecordEditClick = { record ->
                                viewModel.showEditDialog(record)
                            },
                            onRecordDeleteClick = { recordId ->
                                viewModel.showDeleteRecordDialog(recordId)
                            },
                            avgCycleLength = state.avgCycleLength,
                            longestCycle = state.longestCycle,
                            shortestCycle = state.shortestCycle,
                            avgPeriodLength = state.avgPeriodLength,
                            isDark = isDark,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EmptyState(
                            onNavigateHomeToRecord = onNavigateHomeToRecord,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showDeleteDialog != null) {
        DeleteConfirmDialog(
            title = "删除周期",
            message = "确定要删除这个周期记录吗？此操作无法撤销。",
            onConfirm = {
                viewModel.deleteCycle(showDeleteDialog!!)
            },
            onDismiss = {
                viewModel.hideDeleteDialog()
            }
        )
    }

    if (showDeleteRecordDialog != null) {
        DeleteConfirmDialog(
            title = "删除记录",
            message = "确定要删除这条记录吗？此操作无法撤销。",
            onConfirm = {
                viewModel.deleteDailyRecord(showDeleteRecordDialog!!)
            },
            onDismiss = {
                viewModel.hideDeleteRecordDialog()
            }
        )
    }

    if (showEditDialog != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideEditDialog() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            RecordBottomSheetContent(
                initialDate = showEditDialog!!.date,
                initialFlowLevel = showEditDialog!!.flowLevel,
                recordMode = RecordMode.AUTO,
                hasCurrentCycle = true,
                existingRecord = showEditDialog,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModel.hideEditDialog()
                    }
                },
                onSave = { date, flowLevel ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        val updatedRecord = showEditDialog!!.copy(
                            date = date,
                            flowLevel = flowLevel
                        )
                        viewModel.updateDailyRecord(updatedRecord)
                    }
                }
            )
        }
    }
}

@Composable
private fun HistoryContent(
    cycles: List<CycleWithRecords>,
    selectedCycleId: Long?,
    isEditMode: Boolean,
    selectedCycles: Set<Long>,
    onCycleClick: (Long) -> Unit,
    onCycleLongClick: (Long) -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    onRecordDeleteClick: (Long) -> Unit,
    avgCycleLength: Int?,
    longestCycle: Int?,
    shortestCycle: Int?,
    avgPeriodLength: Int?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cyclesByYear = remember(cycles) {
        cycles.groupBy { it.year }.toSortedMap(reverseOrder())
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            StatsCards(
                totalCycles = cycles.size,
                avgCycleLength = avgCycleLength,
                longestCycle = longestCycle,
                shortestCycle = shortestCycle,
                avgPeriodLength = avgPeriodLength,
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        cyclesByYear.forEach { (year, yearCycles) ->
            item {
                YearGroupHeader(
                    year = year,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(yearCycles, key = { it.cycle.id }) { cycleWithRecords ->
                CycleCard(
                    cycleWithRecords = cycleWithRecords,
                    isExpanded = selectedCycleId == cycleWithRecords.cycle.id,
                    isEditMode = isEditMode,
                    isSelected = selectedCycles.contains(cycleWithRecords.cycle.id),
                    onClick = { onCycleClick(cycleWithRecords.cycle.id) },
                    onLongClick = { onCycleLongClick(cycleWithRecords.cycle.id) },
                    onRecordEditClick = onRecordEditClick,
                    onRecordDeleteClick = onRecordDeleteClick,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun StatsCards(
    totalCycles: Int,
    avgCycleLength: Int?,
    longestCycle: Int?,
    shortestCycle: Int?,
    avgPeriodLength: Int?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 第一行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 总周期数卡片
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "总周期数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$totalCycles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = periodColor
                    )
                }
            }

            // 平均周期卡片
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "平均周期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (avgCycleLength != null) "$avgCycleLength 天" else "-- 天",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = periodColor
                    )
                }
            }
        }

        // 第二行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 平均经期卡片
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "平均经期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (avgPeriodLength != null) "$avgPeriodLength 天" else "-- 天",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = periodColor
                    )
                }
            }

            // 最长/最短周期卡片
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "最长/最短周期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (longestCycle != null && shortestCycle != null) {
                            "$longestCycle 天/$shortestCycle 天"
                        } else {
                            "-- 天/-- 天"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = periodColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun EmptyState(
    onNavigateHomeToRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp, 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "还没有记录",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "记录你的第一次经期，开始追踪你的旅程并发现你个人的周期规律。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateHomeToRecord,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("去记录")
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "你的旅程",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "追踪你的周期，发现规律和洞察",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun YearGroupHeader(
    year: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CycleCard(
    cycleWithRecords: CycleWithRecords,
    isExpanded: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    onRecordDeleteClick: (Long) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val iconColor = when (cycleWithRecords.averageFlowLevel) {
        FlowLevel.LIGHT -> periodColor.copy(alpha = 0.5f)
        FlowLevel.MEDIUM -> periodColor.copy(alpha = 0.75f)
        FlowLevel.HEAVY -> periodColor
        null -> periodColor.copy(alpha = 0.5f)
    }
    val flowDisplayText = when (cycleWithRecords.averageFlowLevel) {
        FlowLevel.LIGHT -> "少量"
        FlowLevel.MEDIUM -> "中等"
        FlowLevel.HEAVY -> "大量"
        null -> ""
    }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "arrowRotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(
                if (!isEditMode) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSelected -> periodColor.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        shadowElevation = 1.dp,
        border = if (isSelected) BorderStroke(1.dp, periodColor) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditMode) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) periodColor else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(24.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = cycleWithRecords.dateRangeWithoutYear,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${cycleWithRecords.periodDaysCount}天",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (flowDisplayText.isNotEmpty()) {
                            Text(
                                text = flowDisplayText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (cycleWithRecords.cycleLengthDays != null) "周期${cycleWithRecords.cycleLengthDays}天" else "周期--天",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isEditMode) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !isEditMode,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "每日记录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    cycleWithRecords.records.forEach { record ->
                        DailyRecordRow(
                            record = record,
                            onEditClick = { onRecordEditClick(record) },
                            onDeleteClick = { onRecordDeleteClick(record.id) },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    title: String = "确认删除",
    message: String = "确定要删除这个周期记录吗？此操作无法撤销。",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
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
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// region Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "历史记录页面 - 有数据")
@Composable
private fun HistoryScreenPreview_WithData() {
    PeriodVibeTheme {
        val testCycles = generateTestCycles()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("历史记录") },
                    navigationIcon = { }
                )
            }
        ) { paddingValues ->
            HistoryContent(
                cycles = testCycles,
                selectedCycleId = testCycles.first().cycle.id,
                isEditMode = false,
                selectedCycles = emptySet(),
                onCycleClick = { },
                onCycleLongClick = { },
                onRecordEditClick = { },
                onRecordDeleteClick = { },
                avgCycleLength = 30,
                longestCycle = 35,
                shortestCycle = 28,
                avgPeriodLength = 5,
                isDark = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "历史记录页面 - 空状态")
@Composable
private fun HistoryScreenPreview_Empty() {
    PeriodVibeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("历史记录") },
                    navigationIcon = { }
                )
            }
        ) { paddingValues ->
            EmptyState(
                onNavigateHomeToRecord = { },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "历史记录页面 - 编辑模式")
@Composable
private fun HistoryScreenPreview_EditMode() {
    PeriodVibeTheme {
        val testCycles = generateTestCycles()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("历史记录") },
                    navigationIcon = { }
                )
            }
        ) { paddingValues ->
            HistoryContent(
                cycles = testCycles,
                selectedCycleId = null,
                isEditMode = true,
                selectedCycles = setOf(testCycles.first().cycle.id),
                onCycleClick = { },
                onCycleLongClick = { },
                onRecordEditClick = { },
                onRecordDeleteClick = { },
                avgCycleLength = 30,
                longestCycle = 35,
                shortestCycle = 28,
                avgPeriodLength = 5,
                isDark = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

private fun generateTestCycles(): List<CycleWithRecords> {
    val now = LocalDate.now()
    return listOf(
        // 2025年的周期
        CycleWithRecords(
            cycle = Cycle(
                id = 1,
                startDate = now.minusDays(40),
                endDate = now.minusDays(35),
                cycleLength = 28,
                periodLength = 5,
                averageFlowLevel = FlowLevel.MEDIUM,
                isCompleted = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            records = listOf(
                DailyRecord(
                    id = 101,
                    date = now.minusDays(40),
                    cycleId = 1,
                    isPeriod = true,
                    flowLevel = FlowLevel.MEDIUM
                ),
                DailyRecord(
                    id = 102,
                    date = now.minusDays(39),
                    cycleId = 1,
                    isPeriod = true,
                    flowLevel = FlowLevel.HEAVY
                ),
                DailyRecord(
                    id = 103,
                    date = now.minusDays(38),
                    cycleId = 1,
                    isPeriod = true,
                    flowLevel = FlowLevel.HEAVY
                ),
                DailyRecord(
                    id = 104,
                    date = now.minusDays(37),
                    cycleId = 1,
                    isPeriod = true,
                    flowLevel = FlowLevel.MEDIUM
                ),
                DailyRecord(
                    id = 105,
                    date = now.minusDays(36),
                    cycleId = 1,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                ),
                DailyRecord(
                    id = 106,
                    date = now.minusDays(35),
                    cycleId = 1,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                )
            ),
            calculatedCycleLength = 30
        ),
        CycleWithRecords(
            cycle = Cycle(
                id = 2,
                startDate = now.minusDays(70),
                endDate = now.minusDays(65),
                cycleLength = 30,
                periodLength = 6,
                averageFlowLevel = FlowLevel.LIGHT,
                isCompleted = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            records = listOf(
                DailyRecord(
                    id = 201,
                    date = now.minusDays(70),
                    cycleId = 2,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                ),
                DailyRecord(
                    id = 202,
                    date = now.minusDays(69),
                    cycleId = 2,
                    isPeriod = true,
                    flowLevel = FlowLevel.MEDIUM
                ),
                DailyRecord(
                    id = 203,
                    date = now.minusDays(68),
                    cycleId = 2,
                    isPeriod = true,
                    flowLevel = FlowLevel.MEDIUM
                ),
                DailyRecord(
                    id = 204,
                    date = now.minusDays(67),
                    cycleId = 2,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                ),
                DailyRecord(
                    id = 205,
                    date = now.minusDays(66),
                    cycleId = 2,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                ),
                DailyRecord(
                    id = 206,
                    date = now.minusDays(65),
                    cycleId = 2,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                )
            ),
            calculatedCycleLength = 28
        ),
        // 2024年的周期
        CycleWithRecords(
            cycle = Cycle(
                id = 3,
                startDate = now.minusDays(400),
                endDate = now.minusDays(394),
                cycleLength = 29,
                periodLength = 7,
                averageFlowLevel = FlowLevel.HEAVY,
                isCompleted = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            records = listOf(
                DailyRecord(
                    id = 301,
                    date = now.minusDays(400),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.HEAVY
                ),
                DailyRecord(
                    id = 302,
                    date = now.minusDays(399),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.HEAVY
                ),
                DailyRecord(
                    id = 303,
                    date = now.minusDays(398),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.HEAVY
                ),
                DailyRecord(
                    id = 304,
                    date = now.minusDays(397),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.MEDIUM
                ),
                DailyRecord(
                    id = 305,
                    date = now.minusDays(396),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.MEDIUM
                ),
                DailyRecord(
                    id = 306,
                    date = now.minusDays(395),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                ),
                DailyRecord(
                    id = 307,
                    date = now.minusDays(394),
                    cycleId = 3,
                    isPeriod = true,
                    flowLevel = FlowLevel.LIGHT
                )
            ),
            calculatedCycleLength = 29
        )
    )
}

// endregion

