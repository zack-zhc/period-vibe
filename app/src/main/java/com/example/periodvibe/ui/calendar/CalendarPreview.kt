package com.example.periodvibe.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.usecase.CalendarDay
import com.example.periodvibe.domain.usecase.CalendarDayType
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import java.time.LocalDate
import java.time.YearMonth

// region Preview Data Helpers

private data class TestDayData(
    val dayType: CalendarDayType,
    val record: DailyRecord?,
    val phase: CyclePhase,
    val isPredictedPeriod: Boolean,
    val isPredictedOvulation: Boolean,
    val isPredictedFertile: Boolean
)

private fun generateTestCalendarDays(yearMonth: YearMonth): List<CalendarDay> {
    return generateTestCalendarDaysWithOptions(yearMonth)
}

private fun generateTestCalendarDaysWithOptions(
    yearMonth: YearMonth,
    periodRecordWithoutFlowLevelDays: List<Int> = listOf(10, 11)
): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val monthStart = yearMonth.atDay(1)
    val monthEnd = yearMonth.atEndOfMonth()
    val firstDayOfWeek = monthStart.dayOfWeek.value % 7
    val today = LocalDate.now()

    for (i in 0 until firstDayOfWeek) {
        days.add(CalendarDay.Empty)
    }

    for (day in 1..monthEnd.dayOfMonth) {
        val date = monthStart.withDayOfMonth(day)
        val dayOfMonth = day
        val isToday = date == today

        val dayData = when {
            day in 5..8 -> TestDayData(
                dayType = CalendarDayType.PERIOD,
                record = generateTestDailyRecord(date, FlowLevel.MEDIUM),
                phase = CyclePhase.MENSTRATION,
                isPredictedPeriod = false,
                isPredictedOvulation = false,
                isPredictedFertile = false
            )
            day in periodRecordWithoutFlowLevelDays -> TestDayData(
                dayType = CalendarDayType.PERIOD,
                record = generateTestDailyRecordWithoutFlowLevel(date),
                phase = CyclePhase.MENSTRATION,
                isPredictedPeriod = false,
                isPredictedOvulation = false,
                isPredictedFertile = false
            )
            day in 18..20 -> TestDayData(
                dayType = CalendarDayType.FERTILE,
                record = null,
                phase = CyclePhase.FERTILE,
                isPredictedPeriod = false,
                isPredictedOvulation = false,
                isPredictedFertile = false
            )
            day == 16 -> TestDayData(
                dayType = CalendarDayType.OVULATION,
                record = null,
                phase = CyclePhase.OVULATION,
                isPredictedPeriod = false,
                isPredictedOvulation = false,
                isPredictedFertile = false
            )
            day in 25..29 -> TestDayData(
                dayType = CalendarDayType.PREDICTED_PERIOD,
                record = null,
                phase = CyclePhase.MENSTRATION,
                isPredictedPeriod = true,
                isPredictedOvulation = false,
                isPredictedFertile = false
            )
            else -> TestDayData(
                dayType = CalendarDayType.NORMAL,
                record = null,
                phase = CyclePhase.SAFE,
                isPredictedPeriod = false,
                isPredictedOvulation = false,
                isPredictedFertile = false
            )
        }

        days.add(
            CalendarDay.Data(
                date = date,
                dayOfMonth = dayOfMonth,
                record = dayData.record,
                phase = dayData.phase,
                dayType = dayData.dayType,
                isToday = isToday,
                isPredictedPeriod = dayData.isPredictedPeriod,
                isPredictedOvulation = dayData.isPredictedOvulation,
                isPredictedFertile = dayData.isPredictedFertile
            )
        )
    }

    val remainingDays = 7 - (days.size % 7)
    if (remainingDays < 7) {
        for (i in 0 until remainingDays) {
            days.add(CalendarDay.Empty)
        }
    }

    return days
}

private fun generateTestDailyRecord(date: LocalDate, flowLevel: FlowLevel): DailyRecord {
    return DailyRecord(
        id = 1,
        date = date,
        cycleId = 1,
        isPeriod = true,
        flowLevel = flowLevel
    )
}

private fun generateTestDailyRecordWithoutFlowLevel(date: LocalDate): DailyRecord {
    return DailyRecord(
        id = 1,
        date = date,
        cycleId = 1,
        isPeriod = true,
        flowLevel = null
    )
}

private fun generateTestCycle(): Cycle {
    return Cycle(
        id = 1,
        startDate = LocalDate.now().minusDays(10),
        endDate = null,
        cycleLength = null,
        periodLength = null,
        averageFlowLevel = FlowLevel.MEDIUM,
        isCompleted = false
    )
}

// endregion

// region Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 默认状态")
@Composable
private fun CalendarScreenPreview_Default() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDays(testYearMonth)
        val selectedDate = LocalDate.now()

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = selectedDate,
                    onDateClick = {}
                )

                CalendarLegend()

                val selectedDay = testCalendarDays
                    .filterIsInstance<CalendarDay.Data>()
                    .find { it.date == selectedDate }
                if (selectedDay != null) {
                    SmartActionCard(
                        day = selectedDay,
                        activeCycle = generateTestCycle(),
                        onRecordClick = {},
                        onEndCycleClick = {},
                        onNewCycleClick = {},
                        onEditClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 空选择状态")
@Composable
private fun CalendarScreenPreview_EmptySelection() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDays(testYearMonth)

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = null,
                    onDateClick = {}
                )

                CalendarLegend()

                EmptySelectionCard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 显示今天FAB")
@Composable
private fun CalendarScreenPreview_WithFAB() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now().plusMonths(1)
        val testCalendarDays = generateTestCalendarDays(testYearMonth)
        val today = LocalDate.now()
        val currentPageYearMonth = testYearMonth
        val isCurrentMonth = currentPageYearMonth == YearMonth.now()

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalendarMonthHeader(
                        yearMonth = testYearMonth,
                        onPreviousMonth = {},
                        onNextMonth = {}
                    )

                    CalendarGrid(
                        yearMonth = testYearMonth,
                        days = testCalendarDays,
                        selectedDate = today,
                        onDateClick = {}
                    )

                    CalendarLegend()

                    val selectedDay = testCalendarDays
                        .filterIsInstance<CalendarDay.Data>()
                        .find { it.date == today }
                    if (selectedDay != null) {
                        SmartActionCard(
                            day = selectedDay,
                            activeCycle = generateTestCycle(),
                            onRecordClick = {},
                            onEndCycleClick = {},
                            onNewCycleClick = {},
                            onEditClick = {}
                        )
                    }
                }

                if (!isCurrentMonth) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "今",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 选中有完整经期记录的日期")
@Composable
private fun CalendarScreenPreview_PeriodRecordWithFlowLevel() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDaysWithOptions(testYearMonth)
        val selectedDate = testYearMonth.atDay(6)

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = selectedDate,
                    onDateClick = {}
                )

                CalendarLegend()

                val selectedDay = testCalendarDays
                    .filterIsInstance<CalendarDay.Data>()
                    .find { it.date == selectedDate }
                if (selectedDay != null) {
                    SmartActionCard(
                        day = selectedDay,
                        activeCycle = null,
                        onRecordClick = {},
                        onEndCycleClick = {},
                        onNewCycleClick = {},
                        onEditClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 选中有经期记录但无flowLevel的日期")
@Composable
private fun CalendarScreenPreview_PeriodRecordWithoutFlowLevel() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDaysWithOptions(testYearMonth)
        val selectedDate = testYearMonth.atDay(10)

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = selectedDate,
                    onDateClick = {}
                )

                CalendarLegend()

                val selectedDay = testCalendarDays
                    .filterIsInstance<CalendarDay.Data>()
                    .find { it.date == selectedDate }
                if (selectedDay != null) {
                    SmartActionCard(
                        day = selectedDay,
                        activeCycle = null,
                        onRecordClick = {},
                        onEndCycleClick = {},
                        onNewCycleClick = {},
                        onEditClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 选中在活跃周期内且有记录的日期")
@Composable
private fun CalendarScreenPreview_InCycleWithRecord() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDaysWithOptions(testYearMonth)
        val selectedDate = testYearMonth.atDay(6)
        val activeCycle = generateTestCycle()

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = selectedDate,
                    onDateClick = {}
                )

                CalendarLegend()

                val selectedDay = testCalendarDays
                    .filterIsInstance<CalendarDay.Data>()
                    .find { it.date == selectedDate }
                if (selectedDay != null) {
                    SmartActionCard(
                        day = selectedDay,
                        activeCycle = activeCycle,
                        onRecordClick = {},
                        onEndCycleClick = {},
                        onNewCycleClick = {},
                        onEditClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 选中在活跃周期内但无记录的日期")
@Composable
private fun CalendarScreenPreview_InCycleWithoutRecord() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDaysWithOptions(testYearMonth)
        val selectedDate = testYearMonth.atDay(12)
        val activeCycle = generateTestCycle()

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = selectedDate,
                    onDateClick = {}
                )

                CalendarLegend()

                val selectedDay = testCalendarDays
                    .filterIsInstance<CalendarDay.Data>()
                    .find { it.date == selectedDate }
                if (selectedDay != null) {
                    SmartActionCard(
                        day = selectedDay,
                        activeCycle = activeCycle,
                        onRecordClick = {},
                        onEndCycleClick = {},
                        onNewCycleClick = {},
                        onEditClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "日历页面 - 选中有经期记录的日期")
@Composable
private fun CalendarScreenPreview_WithPeriodRecord() {
    PeriodVibeTheme {
        val testYearMonth = YearMonth.now()
        val testCalendarDays = generateTestCalendarDays(testYearMonth)
        val selectedDate = testYearMonth.atDay(6)

        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarMonthHeader(
                    yearMonth = testYearMonth,
                    onPreviousMonth = {},
                    onNextMonth = {}
                )

                CalendarGrid(
                    yearMonth = testYearMonth,
                    days = testCalendarDays,
                    selectedDate = selectedDate,
                    onDateClick = {}
                )

                CalendarLegend()

                val selectedDay = testCalendarDays
                    .filterIsInstance<CalendarDay.Data>()
                    .find { it.date == selectedDate }
                if (selectedDay != null) {
                    SmartActionCard(
                        day = selectedDay,
                        activeCycle = generateTestCycle(),
                        onRecordClick = {},
                        onEndCycleClick = {},
                        onNewCycleClick = {},
                        onEditClick = {}
                    )
                }
            }
        }
    }
}

// endregion
