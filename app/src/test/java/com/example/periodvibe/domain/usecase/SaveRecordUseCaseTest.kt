package com.example.periodvibe.domain.usecase

import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.RecordMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SaveRecordUseCaseTest {

    private lateinit var cycleRepository: CycleRepository
    private lateinit var useCase: SaveRecordUseCase

    @Before
    fun setup() {
        cycleRepository = mockk()
        useCase = SaveRecordUseCase(cycleRepository)
        coEvery { cycleRepository.updateDailyRecord(any()) } returns Unit
        coEvery { cycleRepository.saveDailyRecord(any()) } returns 0L
    }

    private val activeCycle = Cycle(
        id = 2,
        startDate = LocalDate.of(2024, 2, 1),
        endDate = null,
        cycleLength = null,
        periodLength = null,
        isCompleted = false
    )

    private val historicalCycle = Cycle(
        id = 1,
        startDate = LocalDate.of(2024, 1, 1),
        endDate = LocalDate.of(2024, 1, 5),
        cycleLength = null,
        periodLength = null,
        isCompleted = true
    )

    @Test
    fun `EDIT updates record by its id and reassigns cycle for changed date`() = runTest {
        val original = DailyRecord(
            id = 5,
            date = LocalDate.of(2024, 1, 3),
            cycleId = 1,
            isPeriod = true,
            flowLevel = FlowLevel.LIGHT
        )
        val newDate = LocalDate.of(2024, 2, 3)
        coEvery {
            cycleRepository.reassignCycleForDate(match { it.id == 5L && it.date == newDate })
        } returns original.copy(id = 5, date = newDate, cycleId = 2, flowLevel = FlowLevel.HEAVY)

        val result = useCase(newDate, RecordMode.EDIT, FlowLevel.HEAVY, original)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.updateDailyRecord(
                match {
                    it.id == 5L && it.date == newDate &&
                        it.flowLevel == FlowLevel.HEAVY && it.cycleId == 2L
                }
            )
        }
        coVerify(exactly = 0) { cycleRepository.saveDailyRecord(any()) }
    }

    @Test
    fun `EDIT fails when no existing record for date`() = runTest {
        coEvery { cycleRepository.getDailyRecordByDate(any()) } returns null

        val result = useCase(
            date = LocalDate.of(2024, 1, 3),
            mode = RecordMode.EDIT,
            flowLevel = FlowLevel.LIGHT
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { cycleRepository.updateDailyRecord(any()) }
    }

    @Test
    fun `AUTO updates existing record at date instead of replacing it`() = runTest {
        val existing = DailyRecord(
            id = 9,
            date = LocalDate.of(2024, 1, 3),
            cycleId = 1,
            isPeriod = true,
            flowLevel = FlowLevel.LIGHT
        )
        coEvery { cycleRepository.getDailyRecordByDate(existing.date) } returns existing
        coEvery {
            cycleRepository.reassignCycleForDate(match { it.id == 9L })
        } returns existing.copy(flowLevel = FlowLevel.MEDIUM)

        val result = useCase(existing.date, RecordMode.AUTO, FlowLevel.MEDIUM)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.updateDailyRecord(
                match { it.id == 9L && it.flowLevel == FlowLevel.MEDIUM && it.cycleId == 1L }
            )
        }
        coVerify(exactly = 0) { cycleRepository.saveDailyRecord(any()) }
    }

    @Test
    fun `AUTO attaches new record to the historical cycle covering the date`() = runTest {
        val date = LocalDate.of(2024, 1, 3)
        coEvery { cycleRepository.getDailyRecordByDate(date) } returns null
        coEvery { cycleRepository.getCycleForDate(date) } returns historicalCycle

        val result = useCase(date, RecordMode.AUTO, FlowLevel.HEAVY)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.saveDailyRecord(
                match {
                    it.date == date && it.cycleId == 1L && it.isPeriod && it.flowLevel == FlowLevel.HEAVY
                }
            )
        }
        coVerify(exactly = 0) { cycleRepository.startNewCycle(any()) }
    }

    @Test
    fun `AUTO attaches new record to active cycle when no completed cycle covers date`() = runTest {
        val date = LocalDate.of(2024, 3, 1)
        coEvery { cycleRepository.getDailyRecordByDate(date) } returns null
        coEvery { cycleRepository.getCycleForDate(date) } returns null
        coEvery { cycleRepository.getActiveCycle() } returns activeCycle

        val result = useCase(date, RecordMode.AUTO, null)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.saveDailyRecord(
                match { it.date == date && it.cycleId == 2L && it.isPeriod }
            )
        }
        coVerify(exactly = 0) { cycleRepository.startNewCycle(any()) }
    }

    @Test
    fun `AUTO starts new cycle when no cycle exists`() = runTest {
        val date = LocalDate.of(2024, 1, 1)
        val newCycle = Cycle(
            id = 11,
            startDate = date,
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false
        )
        coEvery { cycleRepository.getDailyRecordByDate(date) } returns null
        coEvery { cycleRepository.getCycleForDate(date) } returns null
        coEvery { cycleRepository.getActiveCycle() } returns null
        coEvery { cycleRepository.startNewCycle(date) } returns newCycle

        val result = useCase(date, RecordMode.AUTO, FlowLevel.LIGHT)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.saveDailyRecord(
                match { it.date == date && it.cycleId == 11L && it.isPeriod }
            )
        }
    }

    @Test
    fun `NEW_CYCLE updates existing record at date and keeps its id`() = runTest {
        val date = LocalDate.of(2024, 3, 1)
        val newCycle = Cycle(
            id = 12,
            startDate = date,
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false
        )
        val existing = DailyRecord(
            id = 5,
            date = date,
            cycleId = 2,
            isPeriod = true,
            flowLevel = FlowLevel.LIGHT
        )
        coEvery { cycleRepository.startNewCycle(date) } returns newCycle
        coEvery { cycleRepository.getDailyRecordByDate(date) } returns existing
        coEvery {
            cycleRepository.reassignCycleForDate(match { it.id == 5L })
        } returns existing.copy(cycleId = 12, flowLevel = FlowLevel.MEDIUM)

        val result = useCase(date, RecordMode.NEW_CYCLE, FlowLevel.MEDIUM)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.updateDailyRecord(
                match { it.id == 5L && it.cycleId == 12L && it.flowLevel == FlowLevel.MEDIUM }
            )
        }
        coVerify(exactly = 0) { cycleRepository.saveDailyRecord(any()) }
    }

    @Test
    fun `NEW_CYCLE inserts new record when no record at date`() = runTest {
        val date = LocalDate.of(2024, 3, 1)
        val newCycle = Cycle(
            id = 12,
            startDate = date,
            endDate = null,
            cycleLength = null,
            periodLength = null,
            isCompleted = false
        )
        coEvery { cycleRepository.startNewCycle(date) } returns newCycle
        coEvery { cycleRepository.getDailyRecordByDate(date) } returns null

        val result = useCase(date, RecordMode.NEW_CYCLE, FlowLevel.LIGHT)

        assertTrue(result.isSuccess)
        coVerify {
            cycleRepository.saveDailyRecord(
                match { it.date == date && it.cycleId == 12L && it.isPeriod }
            )
        }
        coVerify(exactly = 0) { cycleRepository.updateDailyRecord(any()) }
    }
}
