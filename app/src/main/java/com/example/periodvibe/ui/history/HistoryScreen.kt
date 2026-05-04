package com.example.periodvibe.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.usecase.CycleWithRecords
import com.example.periodvibe.ui.history.components.DailyRecordRow
import com.example.periodvibe.ui.history.components.EditModeBottomBar
import com.example.periodvibe.ui.history.components.MiniTimeline
import com.example.periodvibe.ui.home.RecordBottomSheetContent
import com.example.periodvibe.ui.home.RecordMode
import com.example.periodvibe.ui.theme.CalendarPeriodDark
import com.example.periodvibe.ui.theme.CalendarPeriodLight
import kotlinx.coroutines.launch
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
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val selectedCycles by viewModel.selectedCycles.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF1F1A1B)

    val cycleToDelete = remember { mutableStateOf<Long?>(null) }

    // 检查是否有数据
    val hasData = when (val state = historyData) {
        is HistoryUiState.Success -> state.hasData || state.unassociatedRecords.isNotEmpty()
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
                    if (hasData && !isEditMode) {
                        TextButton(onClick = { viewModel.toggleEditMode() }) {
                            Text("编辑")
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
                    onDeleteClick = { viewModel.deleteSelectedCycles() },
                    onCancelClick = { viewModel.toggleEditMode() }
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
                    if (state.hasData || state.unassociatedRecords.isNotEmpty()) {
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
                            onDeleteClick = { cycleId ->
                                cycleToDelete.value = cycleId
                                viewModel.showDeleteDialog(cycleId)
                            },
                            onRecordEditClick = { record ->
                                viewModel.showEditDialog(record)
                            },
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

    if (showDeleteDialog != null && cycleToDelete.value != null) {
        DeleteConfirmDialog(
            onConfirm = {
                cycleToDelete.value?.let { viewModel.deleteCycle(it) }
                cycleToDelete.value = null
            },
            onDismiss = {
                viewModel.hideDeleteDialog()
                cycleToDelete.value = null
            }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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
    onDeleteClick: (Long) -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            TimelineHeader(totalCycles = cycles.size, isDark = isDark)
        }

        items(cycles, key = { it.cycle.id }) { cycleWithRecords ->
            TimelineCycleCard(
                cycleWithRecords = cycleWithRecords,
                isExpanded = selectedCycleId == cycleWithRecords.cycle.id,
                isEditMode = isEditMode,
                isSelected = selectedCycles.contains(cycleWithRecords.cycle.id),
                onClick = { onCycleClick(cycleWithRecords.cycle.id) },
                onDeleteClick = { onDeleteClick(cycleWithRecords.cycle.id) },
                onRecordEditClick = onRecordEditClick,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun TimelineHeader(
    totalCycles: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500),
        label = "header_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = periodColor.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = periodColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "已记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$totalCycles 个周期",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TimelineCycleCard(
    cycleWithRecords: CycleWithRecords,
    isExpanded: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val cardElevation by animateDpAsState(
        targetValue = if (isExpanded) 2.dp else 0.dp,
        label = "card_elevation"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) periodColor else periodColor.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            ) {}
            Spacer(modifier = Modifier.weight(1f))
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            color = when {
                isSelected -> periodColor.copy(alpha = 0.1f)
                isExpanded -> periodColor.copy(alpha = 0.05f)
                else -> MaterialTheme.colorScheme.surface
            },
            tonalElevation = cardElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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

                    Text(
                        text = cycleWithRecords.startDateFormatted,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (!isEditMode) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                MiniTimeline(
                    cycleLengthDays = cycleWithRecords.cycleLengthDays,
                    periodDays = cycleWithRecords.periodDaysCount,
                    isDark = isDark
                )

                AnimatedVisibility(
                    visible = isExpanded && !isEditMode,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

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
                                isDark = isDark
                            )
                        }
                    }
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
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "还没有记录",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "去首页开始记录吧",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateHomeToRecord) {
            Text("去记录")
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "确认删除",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "确定要删除这个周期记录吗？此操作无法撤销。",
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

