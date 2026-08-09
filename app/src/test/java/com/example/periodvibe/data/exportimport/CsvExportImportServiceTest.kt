package com.example.periodvibe.data.exportimport

import android.content.Context
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

class CsvExportImportServiceTest {

    private lateinit var service: CsvExportImportService

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        every { context.getString(com.example.periodvibe.R.string.csv_error_empty) } returns "CSV file is empty"
        every { context.getString(com.example.periodvibe.R.string.csv_error_row_parse, *anyVararg()) } returns "row parse failed"
        service = CsvExportImportService(context)
    }

    @Test
    fun `cycles csv export and import roundtrip preserves all fields`() = runTest {
        val cycles = listOf(
            Cycle(
                id = 1,
                startDate = LocalDate.of(2024, 1, 1),
                endDate = LocalDate.of(2024, 1, 5),
                cycleLength = 28,
                periodLength = 5,
                averageFlowLevel = FlowLevel.MEDIUM,
                isCompleted = true,
                createdAt = LocalDateTime.of(2024, 1, 1, 8, 30),
                updatedAt = LocalDateTime.of(2024, 1, 6, 8, 30)
            ),
            Cycle(
                id = 2,
                startDate = LocalDate.of(2024, 2, 1),
                endDate = null,
                cycleLength = null,
                periodLength = null,
                averageFlowLevel = null,
                isCompleted = false,
                createdAt = LocalDateTime.of(2024, 2, 1, 8, 30),
                updatedAt = LocalDateTime.of(2024, 2, 1, 8, 30)
            )
        )

        val csv = service.exportCyclesToCsv(cycles)
        val result = service.importCyclesFromCsv(csv)

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(0, success.warnings.size)
        assertEquals(2, success.data.size)

        val c1 = success.data.first { it.startDate == LocalDate.of(2024, 1, 1) }
        assertEquals(LocalDate.of(2024, 1, 5), c1.endDate)
        assertEquals(28, c1.cycleLength)
        assertEquals(5, c1.periodLength)
        assertEquals(FlowLevel.MEDIUM, c1.averageFlowLevel)
        assertEquals(true, c1.isCompleted)
        assertEquals(LocalDateTime.of(2024, 1, 1, 8, 30), c1.createdAt)
        assertEquals(LocalDateTime.of(2024, 1, 6, 8, 30), c1.updatedAt)

        val c2 = success.data.first { it.startDate == LocalDate.of(2024, 2, 1) }
        assertEquals(null, c2.endDate)
        assertEquals(null, c2.cycleLength)
        assertEquals(null, c2.averageFlowLevel)
        assertEquals(false, c2.isCompleted)
    }

    @Test
    fun `daily records csv preserves correct cycle associations with multiple cycles`() = runTest {
        val cycles = listOf(
            Cycle(id = 10, startDate = LocalDate.of(2024, 1, 1), endDate = null, cycleLength = null, periodLength = null),
            Cycle(id = 20, startDate = LocalDate.of(2024, 2, 1), endDate = null, cycleLength = null, periodLength = null)
        )
        val records = listOf(
            DailyRecord(
                id = 1,
                date = LocalDate.of(2024, 1, 3),
                cycleId = 10,
                isPeriod = true,
                flowLevel = FlowLevel.HEAVY
            ),
            DailyRecord(
                id = 2,
                date = LocalDate.of(2024, 2, 5),
                cycleId = 20,
                isPeriod = true,
                flowLevel = FlowLevel.LIGHT
            ),
            DailyRecord(
                id = 3,
                date = LocalDate.of(2024, 3, 1),
                cycleId = null,
                isPeriod = false,
                flowLevel = null
            )
        )

        val csv = service.exportDailyRecordsToCsv(records, cycles)

        // 模拟导入流程：周期来自 importCyclesFromCsv（id 全为 0），先赋临时唯一 ID 再解析记录
        val tempCycles = listOf(
            Cycle(id = 1, startDate = LocalDate.of(2024, 1, 1), endDate = null, cycleLength = null, periodLength = null),
            Cycle(id = 2, startDate = LocalDate.of(2024, 2, 1), endDate = null, cycleLength = null, periodLength = null)
        )
        val result = service.importDailyRecordsFromCsv(csv, tempCycles)

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(0, success.warnings.size)
        assertEquals(3, success.data.size)

        val recordForCycle1 = success.data.first { it.date == LocalDate.of(2024, 1, 3) }
        val recordForCycle2 = success.data.first { it.date == LocalDate.of(2024, 2, 5) }
        val recordNoCycle = success.data.first { it.date == LocalDate.of(2024, 3, 1) }

        // 关键回归测试：记录必须关联到正确的周期
        assertEquals(1L, recordForCycle1.cycleId)
        assertEquals(2L, recordForCycle2.cycleId)
        assertEquals(null, recordNoCycle.cycleId)
        assertEquals(FlowLevel.HEAVY, recordForCycle1.flowLevel)
        assertEquals(true, recordForCycle1.isPeriod)
    }

    @Test
    fun `detectFileType detects json`() = runTest {
        assertEquals(
            CsvExportImportService.FileType.JSON,
            service.detectFileType("""{"version":1,"cycles":[]}""")
        )
    }

    @Test
    fun `detectFileType detects json with BOM prefix`() = runTest {
        assertEquals(
            CsvExportImportService.FileType.JSON,
            service.detectFileType("\uFEFF{\"version\":1,\"cycles\":[]}")
        )
    }

    @Test
    fun `detectFileType detects combined csv`() = runTest {
        val csv = "=== CYCLES ===\n" +
            "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at\n" +
            "=== DAILY_RECORDS ===\n" +
            "id,date,cycle_start_date,is_period,flow_level,created_at,updated_at"

        assertEquals(
            CsvExportImportService.FileType.COMBINED_CSV,
            service.detectFileType(csv)
        )
    }

    @Test
    fun `detectFileType detects cycles only csv`() = runTest {
        val csv = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at\n" +
            ",2024-01-01,2024-01-05,3,5,LIGHT,true,2024-01-01T00:00:00,2024-01-05T00:00:00"

        assertEquals(
            CsvExportImportService.FileType.CYCLES_CSV,
            service.detectFileType(csv)
        )
    }

    @Test
    fun `detectFileType detects daily records only csv`() = runTest {
        val csv = "id,date,cycle_start_date,is_period,flow_level,created_at,updated_at\n" +
            ",2024-01-03,2024-01-01,true,HEAVY,2024-01-03T00:00:00,2024-01-03T00:00:00"

        assertEquals(
            CsvExportImportService.FileType.DAILY_RECORDS_CSV,
            service.detectFileType(csv)
        )
    }

    @Test
    fun `detectFileType does not misclassify json as csv`() = runTest {
        // 此前 JSON 内容（即使解析失败）会因包含 start_date 字段被误判为 CYCLES_CSV
        val json = """{"version":1,"cycles":[{"start_date":"2024-01-01"}]}"""

        assertEquals(
            CsvExportImportService.FileType.JSON,
            service.detectFileType(json)
        )
    }

    @Test
    fun `detectFileType returns unknown for unrecognized content`() = runTest {
        assertEquals(
            CsvExportImportService.FileType.UNKNOWN,
            service.detectFileType("hello world")
        )
    }

    @Test
    fun `import cycles from header only csv returns empty success`() = runTest {
        val csv = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at"

        val result = service.importCyclesFromCsv(csv)

        assertTrue(result is CsvImportResult.Success)
        assertEquals(0, (result as CsvImportResult.Success).data.size)
    }

    @Test
    fun `import cycles from empty string returns failure`() = runTest {
        val result = service.importCyclesFromCsv("")

        assertTrue(result is CsvImportResult.Failure)
    }

    @Test
    fun `partially invalid cycle rows produce warnings`() = runTest {
        val csv = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at\n" +
            ",2024-01-01,2024-01-05,3,5,LIGHT,true,2024-01-01T00:00:00,2024-01-05T00:00:00\n" +
            "this is not a valid row"

        val result = service.importCyclesFromCsv(csv)

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(1, success.data.size)
        assertEquals(1, success.warnings.size)
        assertTrue(success.warnings[0].contains("row parse failed"))
    }

    @Test
    fun `cycle row with invalid flow level produces warning`() = runTest {
        val csv = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at\n" +
            ",2024-01-01,2024-01-05,3,5,LIGHT,true,2024-01-01T00:00:00,2024-01-05T00:00:00\n" +
            ",2024-02-01,2024-02-05,3,5,HUGE,true,2024-02-01T00:00:00,2024-02-05T00:00:00"

        val result = service.importCyclesFromCsv(csv)

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(1, success.data.size)
        assertEquals(1, success.warnings.size)
    }

    @Test
    fun `daily record row with invalid flow level produces warning`() = runTest {
        val csv = "id,date,cycle_start_date,is_period,flow_level,created_at,updated_at\n" +
            ",2024-01-03,2024-01-01,true,HUGE,2024-01-03T00:00:00,2024-01-03T00:00:00"

        val result = service.importDailyRecordsFromCsv(csv, emptyList())

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(0, success.data.size)
        assertEquals(1, success.warnings.size)
    }

    @Test
    fun `quoted csv fields are parsed correctly`() = runTest {
        val csv = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at\n" +
            ",2024-01-01,2024-01-05,3,5,LIGHT,\"true\",\"2024-01-01T10:00:00\",\"2024-01-05T10:00:00\""

        val result = service.importCyclesFromCsv(csv)

        assertTrue(result is CsvImportResult.Success)
        val cycle = (result as CsvImportResult.Success).data.single()
        assertEquals(true, cycle.isCompleted)
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), cycle.createdAt)
        assertEquals(LocalDateTime.of(2024, 1, 5, 10, 0), cycle.updatedAt)
    }

    @Test
    fun `import cycles from combined file with section markers works end to end`() = runTest {
        val cyclesCsv = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at\n" +
            ",2024-01-01,2024-01-05,3,5,LIGHT,true,2024-01-01T00:00:00,2024-01-05T00:00:00"
        val recordsCsv = "id,date,cycle_start_date,is_period,flow_level,created_at,updated_at\n" +
            ",2024-01-03,2024-01-01,true,HEAVY,2024-01-03T00:00:00,2024-01-03T00:00:00"

        val combined = "=== CYCLES ===\n$cyclesCsv\n=== DAILY_RECORDS ===\n$recordsCsv"

        assertEquals(
            CsvExportImportService.FileType.COMBINED_CSV,
            service.detectFileType(combined)
        )

        val cyclesResult = service.importCyclesFromCsv(cyclesCsv)
        assertTrue(cyclesResult is CsvImportResult.Success)
        val cycles = (cyclesResult as CsvImportResult.Success).data
            .mapIndexed { index, cycle -> cycle.copy(id = index + 1L) }

        val recordsResult = service.importDailyRecordsFromCsv(recordsCsv, cycles)
        assertTrue(recordsResult is CsvImportResult.Success)
        val record = (recordsResult as CsvImportResult.Success).data.single()
        assertEquals(1L, record.cycleId)
        assertEquals(FlowLevel.HEAVY, record.flowLevel)
    }
}
