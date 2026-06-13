package com.example.periodvibe.ui.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.usecase.CycleWithRecords
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import java.time.LocalDate
import java.time.LocalDateTime

// region Preview Data Helpers

private fun generateTestCycles(): List<CycleWithRecords> {
    val now = LocalDate.now()
    return listOf(
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
                DailyRecord(id = 101, date = now.minusDays(40), cycleId = 1, isPeriod = true, flowLevel = FlowLevel.MEDIUM),
                DailyRecord(id = 102, date = now.minusDays(39), cycleId = 1, isPeriod = true, flowLevel = FlowLevel.HEAVY),
                DailyRecord(id = 103, date = now.minusDays(38), cycleId = 1, isPeriod = true, flowLevel = FlowLevel.HEAVY),
                DailyRecord(id = 104, date = now.minusDays(37), cycleId = 1, isPeriod = true, flowLevel = FlowLevel.MEDIUM),
                DailyRecord(id = 105, date = now.minusDays(36), cycleId = 1, isPeriod = true, flowLevel = FlowLevel.LIGHT),
                DailyRecord(id = 106, date = now.minusDays(35), cycleId = 1, isPeriod = true, flowLevel = FlowLevel.LIGHT)
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
                DailyRecord(id = 201, date = now.minusDays(70), cycleId = 2, isPeriod = true, flowLevel = FlowLevel.LIGHT),
                DailyRecord(id = 202, date = now.minusDays(69), cycleId = 2, isPeriod = true, flowLevel = FlowLevel.MEDIUM),
                DailyRecord(id = 203, date = now.minusDays(68), cycleId = 2, isPeriod = true, flowLevel = FlowLevel.MEDIUM),
                DailyRecord(id = 204, date = now.minusDays(67), cycleId = 2, isPeriod = true, flowLevel = FlowLevel.LIGHT),
                DailyRecord(id = 205, date = now.minusDays(66), cycleId = 2, isPeriod = true, flowLevel = FlowLevel.LIGHT),
                DailyRecord(id = 206, date = now.minusDays(65), cycleId = 2, isPeriod = true, flowLevel = FlowLevel.LIGHT)
            ),
            calculatedCycleLength = 28
        ),
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
                DailyRecord(id = 301, date = now.minusDays(400), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.HEAVY),
                DailyRecord(id = 302, date = now.minusDays(399), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.HEAVY),
                DailyRecord(id = 303, date = now.minusDays(398), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.HEAVY),
                DailyRecord(id = 304, date = now.minusDays(397), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.MEDIUM),
                DailyRecord(id = 305, date = now.minusDays(396), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.MEDIUM),
                DailyRecord(id = 306, date = now.minusDays(395), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.LIGHT),
                DailyRecord(id = 307, date = now.minusDays(394), cycleId = 3, isPeriod = true, flowLevel = FlowLevel.LIGHT)
            ),
            calculatedCycleLength = 29
        )
    )
}

// endregion

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

// endregion
