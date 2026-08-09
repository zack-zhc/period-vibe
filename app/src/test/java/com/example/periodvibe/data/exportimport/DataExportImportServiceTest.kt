package com.example.periodvibe.data.exportimport

import android.content.Context
import com.example.periodvibe.data.mapper.CycleMapper
import com.example.periodvibe.data.mapper.DailyRecordMapper
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DataExportImportServiceTest {

    private lateinit var service: DataExportImportService

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        every { context.getString(com.example.periodvibe.R.string.import_error_version, *anyVararg()) } returns "unsupported version"
        every { context.getString(com.example.periodvibe.R.string.import_error_cycle_duplicate, *anyVararg()) } returns "duplicate cycle"
        every { context.getString(com.example.periodvibe.R.string.import_error_record_duplicate, *anyVararg()) } returns "duplicate record"
        every { context.getString(com.example.periodvibe.R.string.import_error_invalid_flow_cycle, *anyVararg()) } returns "invalid flow: HUGE"
        every { context.getString(com.example.periodvibe.R.string.import_error_invalid_flow_record, *anyVararg()) } returns "invalid flow: HUGE"
        every { context.getString(com.example.periodvibe.R.string.import_error_parse, *anyVararg()) } returns "parse failed"
        service = DataExportImportService(context, CycleMapper(), DailyRecordMapper())
    }

    @Test
    fun `export and import roundtrip preserves cycles and records with correct cycle associations`() = runTest {
        val cycle1 = Cycle(
            id = 10,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 5),
            cycleLength = 28,
            periodLength = 5,
            averageFlowLevel = FlowLevel.MEDIUM,
            isCompleted = true,
            createdAt = LocalDateTime.of(2024, 1, 1, 8, 0),
            updatedAt = LocalDateTime.of(2024, 1, 6, 8, 0)
        )
        val cycle2 = Cycle(
            id = 20,
            startDate = LocalDate.of(2024, 2, 1),
            endDate = LocalDate.of(2024, 2, 4),
            cycleLength = 31,
            periodLength = 4,
            averageFlowLevel = FlowLevel.LIGHT,
            isCompleted = true,
            createdAt = LocalDateTime.of(2024, 2, 1, 8, 0),
            updatedAt = LocalDateTime.of(2024, 2, 5, 8, 0)
        )
        val recordForCycle1 = DailyRecord(
            id = 100,
            date = LocalDate.of(2024, 1, 3),
            cycleId = 10,
            isPeriod = true,
            flowLevel = FlowLevel.HEAVY,
            createdAt = LocalDateTime.of(2024, 1, 3, 9, 0),
            updatedAt = LocalDateTime.of(2024, 1, 3, 9, 0)
        )
        val recordForCycle2 = DailyRecord(
            id = 101,
            date = LocalDate.of(2024, 2, 3),
            cycleId = 20,
            isPeriod = true,
            flowLevel = FlowLevel.LIGHT,
            createdAt = LocalDateTime.of(2024, 2, 3, 9, 0),
            updatedAt = LocalDateTime.of(2024, 2, 3, 9, 0)
        )
        val recordWithoutCycle = DailyRecord(
            id = 102,
            date = LocalDate.of(2024, 3, 1),
            cycleId = null,
            isPeriod = false,
            flowLevel = null,
            createdAt = LocalDateTime.of(2024, 3, 1, 9, 0),
            updatedAt = LocalDateTime.of(2024, 3, 1, 9, 0)
        )

        val json = service.exportToJson(
            listOf(cycle1, cycle2),
            listOf(recordForCycle1, recordForCycle2, recordWithoutCycle)
        )
        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(2, success.cycles.size)
        assertEquals(3, success.dailyRecords.size)
        assertEquals(0, success.warnings.size)

        // 导入后周期应获得不同的临时 ID（1、2）
        val cycleIds = success.cycles.map { it.id }.toSet()
        assertEquals(setOf(1L, 2L), cycleIds)

        // 关键回归测试：日常记录必须关联到其对应的周期，而不是第一个周期
        val cycleByStartDate = success.cycles.associateBy { it.startDate }
        val recordForC1 = success.dailyRecords.first { it.date == LocalDate.of(2024, 1, 3) }
        val recordForC2 = success.dailyRecords.first { it.date == LocalDate.of(2024, 2, 3) }
        val recordNoCycle = success.dailyRecords.first { it.date == LocalDate.of(2024, 3, 1) }

        assertEquals(cycleByStartDate[LocalDate.of(2024, 1, 1)]?.id, recordForC1.cycleId)
        assertEquals(cycleByStartDate[LocalDate.of(2024, 2, 1)]?.id, recordForC2.cycleId)
        assertTrue(recordForC2.cycleId != recordForC1.cycleId)
        assertEquals(null, recordNoCycle.cycleId)

        // 字段完整性检查
        val importedCycle1 = cycleByStartDate.getValue(LocalDate.of(2024, 1, 1))
        assertEquals(LocalDate.of(2024, 1, 5), importedCycle1.endDate)
        assertEquals(28, importedCycle1.cycleLength)
        assertEquals(5, importedCycle1.periodLength)
        assertEquals(FlowLevel.MEDIUM, importedCycle1.averageFlowLevel)
        assertEquals(true, importedCycle1.isCompleted)
        assertEquals(LocalDateTime.of(2024, 1, 1, 8, 0), importedCycle1.createdAt)
        assertEquals(FlowLevel.HEAVY, recordForC1.flowLevel)
        assertEquals(true, recordForC1.isPeriod)
    }

    @Test
    fun `import rejects unsupported version`() = runTest {
        val json = """{"version":2,"cycles":[],"daily_records":[]}"""

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("unsupported version"))
    }

    @Test
    fun `import rejects duplicate cycle start dates`() = runTest {
        val json = """
            {"version":1,"cycles":[
                {"start_date":"2024-01-01"},
                {"start_date":"2024-01-01"}
            ],"daily_records":[]}
        """.trimIndent()

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("duplicate cycle"))
    }

    @Test
    fun `import rejects duplicate daily record dates`() = runTest {
        val json = """
            {"version":1,"cycles":[],"daily_records":[
                {"date":"2024-01-01","is_period":true},
                {"date":"2024-01-01","is_period":true}
            ]}
        """.trimIndent()

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("duplicate record"))
    }

    @Test
    fun `import rejects invalid flow level in daily record`() = runTest {
        val json = """
            {"version":1,"cycles":[],"daily_records":[
                {"date":"2024-01-01","is_period":true,"flow_level":"HUGE"}
            ]}
        """.trimIndent()

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("invalid flow: HUGE"))
    }

    @Test
    fun `import rejects invalid flow level in cycle`() = runTest {
        val json = """
            {"version":1,"cycles":[
                {"start_date":"2024-01-01","average_flow_level":"HUGE"}
            ],"daily_records":[]}
        """.trimIndent()

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("invalid flow: HUGE"))
    }

    @Test
    fun `import fails on garbage input`() = runTest {
        val result = service.importFromJson("this is not json")

        assertTrue(result is ImportResult.Failure)
    }

    @Test
    fun `import fails with clear message when cycle is missing start_date`() = runTest {
        val json = """
            {"version":1,"cycles":[
                {"end_date":"2024-01-05"}
            ],"daily_records":[]}
        """.trimIndent()

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("parse failed"))
    }

    @Test
    fun `import fails with clear message when record is missing date`() = runTest {
        val json = """
            {"version":1,"cycles":[],"daily_records":[
                {"is_period":true}
            ]}
        """.trimIndent()

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Failure)
        assertTrue((result as ImportResult.Failure).errorMessage.contains("parse failed"))
    }

    @Test
    fun `import succeeds with empty data`() = runTest {
        val json = """{"version":1,"cycles":[],"daily_records":[]}"""

        val result = service.importFromJson(json)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(0, success.cycles.size)
        assertEquals(0, success.dailyRecords.size)
    }
}
