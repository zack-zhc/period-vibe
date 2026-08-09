package com.example.periodvibe.data.exportimport

import android.content.Context
import android.net.Uri
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 从 Uri 获取文件扩展名
 */
fun Uri.getFileExtension(context: Context): String? {
    return lastPathSegment?.substringAfterLast('.', "")?.lowercase()
}

/**
 * CSV 导出/导入格式枚举
 */
enum class ExportFormat(val displayName: String, val mimeType: String, val extension: String) {
    JSON("JSON", "application/json", "json"),
    CSV("CSV", "text/csv", "csv")
}

/**
 * CSV 导出/导入服务
 */
@Singleton
class CsvExportImportService @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    companion object {
        // CSV 表头
        private const val CYCLE_CSV_HEADER = "id,start_date,end_date,cycle_length,period_length,average_flow_level,is_completed,created_at,updated_at"
        private const val DAILY_RECORD_CSV_HEADER = "id,date,cycle_start_date,is_period,flow_level,created_at,updated_at"
    }

    /**
     * 导出周期数据为 CSV 字符串
     */
    suspend fun exportCyclesToCsv(cycles: List<Cycle>): String {
        return withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            sb.appendLine(CYCLE_CSV_HEADER)

            cycles.forEach { cycle ->
                sb.appendLine(cycleToCsvLine(cycle))
            }

            sb.toString()
        }
    }

    /**
     * 导出日常记录数据为 CSV 字符串
     */
    suspend fun exportDailyRecordsToCsv(records: List<DailyRecord>, cycles: List<Cycle>): String {
        return withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            sb.appendLine(DAILY_RECORD_CSV_HEADER)

            // 创建 cycleId 到 startDate 的映射
            val cycleIdToStartDate = cycles.associate { it.id to it.startDate }

            records.forEach { record ->
                sb.appendLine(dailyRecordToCsvLine(record, cycleIdToStartDate))
            }

            sb.toString()
        }
    }

    /**
     * 检测文件内容类型
     */
    enum class FileType {
        JSON,
        COMBINED_CSV,      // 带有 === CYCLES === 标记的合并 CSV
        CYCLES_CSV,        // 只有周期数据的 CSV
        DAILY_RECORDS_CSV, // 只有日常记录数据的 CSV
        UNKNOWN
    }

    /**
     * 检测文件类型
     */
    suspend fun detectFileType(content: String): FileType {
        return withContext(Dispatchers.IO) {
            val trimmed = content.removePrefix("\uFEFF").trim()
            val firstLine = trimmed.lines().firstOrNull()
            when {
                trimmed.startsWith("{") -> FileType.JSON
                trimmed.contains("=== CYCLES ===") -> FileType.COMBINED_CSV
                firstLine?.let { it.contains("start_date") && it.contains("end_date") } == true ->
                    FileType.CYCLES_CSV
                firstLine?.contains("is_period") == true -> FileType.DAILY_RECORDS_CSV
                else -> FileType.UNKNOWN
            }
        }
    }

    /**
     * 从 CSV 字符串导入周期数据（更宽松的检测）
     */
    suspend fun importCyclesFromCsv(csvContent: String): CsvImportResult<List<Cycle>> {
        return withContext(Dispatchers.IO) {
            val lines = csvContent.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (lines.isEmpty()) {
                return@withContext CsvImportResult.Failure(context.getString(com.example.periodvibe.R.string.csv_error_empty))
            }

            // 找到表头行
            val headerIndex = lines.indexOfFirst { it.contains("start_date") && it.contains("end_date") }
            val dataLines = if (headerIndex >= 0) {
                lines.drop(headerIndex + 1)
            } else {
                // 如果没有明确的表头，尝试直接解析
                lines
            }

            if (dataLines.isEmpty()) {
                // 只有表头没有数据（例如导出了空数据），视为成功但无数据
                return@withContext CsvImportResult.Success(emptyList())
            }

            val cycles = mutableListOf<Cycle>()
            val errors = mutableListOf<String>()

            dataLines.forEach { line ->
                try {
                    val cycle = parseCycleCsvLine(line)
                    cycles.add(cycle)
                } catch (e: Exception) {
                    errors.add(context.getString(com.example.periodvibe.R.string.csv_error_row_parse, cycles.size + errors.size + 1, e.message))
                }
            }

            if (cycles.isEmpty() && errors.isNotEmpty()) {
                CsvImportResult.Failure(errors.joinToString("\n"))
            } else {
                CsvImportResult.Success(cycles, warnings = errors)
            }
        }
    }

    /**
     * 从 CSV 字符串导入日常记录数据
     */
    suspend fun importDailyRecordsFromCsv(
        csvContent: String,
        cycles: List<Cycle>
    ): CsvImportResult<List<DailyRecord>> {
        return withContext(Dispatchers.IO) {
            val lines = csvContent.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (lines.isEmpty()) {
                return@withContext CsvImportResult.Success(emptyList())
            }

            // 找到表头行
            val headerIndex = lines.indexOfFirst { it.contains("date") && it.contains("is_period") }
            val dataLines = if (headerIndex >= 0) {
                lines.drop(headerIndex + 1)
            } else {
                lines
            }

            if (dataLines.isEmpty()) {
                return@withContext CsvImportResult.Success(emptyList())
            }

            // 创建 startDate 到 cycle 的映射
            val startDateToCycle = cycles.associateBy { it.startDate }

            val records = mutableListOf<DailyRecord>()
            val errors = mutableListOf<String>()

            dataLines.forEach { line ->
                try {
                    val record = parseDailyRecordCsvLine(line, startDateToCycle)
                    records.add(record)
                } catch (e: Exception) {
                    errors.add(context.getString(com.example.periodvibe.R.string.csv_error_row_parse, records.size + errors.size + 1, e.message))
                }
            }

            CsvImportResult.Success(records, warnings = errors)
        }
    }

    /**
     * 写入 CSV 到文件
     */
    suspend fun writeCsvToFile(
        context: Context,
        uri: Uri,
        cyclesCsv: String,
        recordsCsv: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val combinedContent = buildString {
                    appendLine("=== CYCLES ===")
                    append(cyclesCsv)
                    appendLine()
                    appendLine("=== DAILY_RECORDS ===")
                    append(recordsCsv)
                }

                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: return@withContext false
                outputStream.use { stream ->
                    stream.write(combinedContent.toByteArray(Charsets.UTF_8))
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * 从文件读取 CSV - 简化版本，直接返回全部内容
     */
    suspend fun readFileContent(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val content = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.lineSequence().forEach { line ->
                            content.appendLine(line)
                        }
                    }
                }
                content.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 从文件读取 CSV - 解析为周期和记录两部分
     */
    suspend fun readCsvFromFile(context: Context, uri: Uri): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                var cyclesCsv: String? = null
                var recordsCsv: String? = null
                var currentSection: String? = null
                val cyclesLines = mutableListOf<String>()
                val recordsLines = mutableListOf<String>()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.lineSequence().forEach { line ->
                            when {
                                line.trim() == "=== CYCLES ===" -> {
                                    currentSection = "CYCLES"
                                }
                                line.trim() == "=== DAILY_RECORDS ===" -> {
                                    currentSection = "DAILY_RECORDS"
                                }
                                line.isNotBlank() -> {
                                    when (currentSection) {
                                        "CYCLES" -> cyclesLines.add(line)
                                        "DAILY_RECORDS" -> recordsLines.add(line)
                                        null -> {
                                            // 没有标记时，根据表头判断所属数据段
                                            when {
                                                line.contains("start_date") && line.contains("end_date") -> {
                                                    currentSection = "CYCLES"
                                                    cyclesLines.add(line)
                                                }
                                                line.contains("is_period") -> {
                                                    currentSection = "DAILY_RECORDS"
                                                    recordsLines.add(line)
                                                }
                                                else -> {
                                                    currentSection = "CYCLES"
                                                    cyclesLines.add(line)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (cyclesLines.isNotEmpty()) {
                    cyclesCsv = cyclesLines.joinToString("\n")
                }
                if (recordsLines.isNotEmpty()) {
                    recordsCsv = recordsLines.joinToString("\n")
                }

                Pair(cyclesCsv, recordsCsv)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, null)
            }
        }
    }

    private fun cycleToCsvLine(cycle: Cycle): String {
        return listOf(
            "", // ID 留空，导入时重新生成
            cycle.startDate.format(dateFormatter),
            cycle.endDate?.format(dateFormatter) ?: "",
            cycle.cycleLength?.toString() ?: "",
            cycle.periodLength?.toString() ?: "",
            cycle.averageFlowLevel?.name ?: "",
            cycle.isCompleted.toString(),
            cycle.createdAt.format(dateTimeFormatter),
            cycle.updatedAt.format(dateTimeFormatter)
        ).joinToString(",") { escapeCsvField(it) }
    }

    private fun dailyRecordToCsvLine(
        record: DailyRecord,
        cycleIdToStartDate: Map<Long, LocalDate>
    ): String {
        val cycleStartDate = record.cycleId?.let { cycleIdToStartDate[it] }
        return listOf(
            "", // ID 留空
            record.date.format(dateFormatter),
            cycleStartDate?.format(dateFormatter) ?: "",
            record.isPeriod.toString(),
            record.flowLevel?.name ?: "",
            record.createdAt.format(dateTimeFormatter),
            record.updatedAt.format(dateTimeFormatter)
        ).joinToString(",") { escapeCsvField(it) }
    }

    private fun parseCycleCsvLine(line: String): Cycle {
        val fields = parseCsvLine(line)
        require(fields.size >= 2) { "CSV must contain start_date field" }

        // 尝试找到 start_date 字段
        var startDateStr = ""
        var endDateStr: String? = null
        var cycleLengthStr: String? = null
        var periodLengthStr: String? = null
        var flowLevelStr: String? = null
        var isCompletedStr = "false"
        var createdAtStr: String? = null
        var updatedAtStr: String? = null

        // 如果是标准格式（9个字段）
        if (fields.size >= 9) {
            startDateStr = fields[1]
            endDateStr = fields[2].ifBlank { null }
            cycleLengthStr = fields[3].ifBlank { null }
            periodLengthStr = fields[4].ifBlank { null }
            flowLevelStr = fields[5].ifBlank { null }
            isCompletedStr = fields[6]
            createdAtStr = fields[7].ifBlank { null }
            updatedAtStr = fields[8].ifBlank { null }
        } else {
            // 尝试简化格式 - 第一个非空字段是 start_date
            startDateStr = fields.firstOrNull { it.isNotBlank() } ?: ""
        }

        return Cycle(
            id = 0,
            startDate = LocalDate.parse(startDateStr, dateFormatter),
            endDate = endDateStr?.let { LocalDate.parse(it, dateFormatter) },
            cycleLength = cycleLengthStr?.toIntOrNull(),
            periodLength = periodLengthStr?.toIntOrNull(),
            averageFlowLevel = flowLevelStr?.takeIf { it.isNotBlank() }?.let { FlowLevel.valueOf(it) },
            isCompleted = isCompletedStr.toBoolean(),
            createdAt = createdAtStr?.let { LocalDateTime.parse(it, dateTimeFormatter) } ?: LocalDateTime.now(),
            updatedAt = updatedAtStr?.let { LocalDateTime.parse(it, dateTimeFormatter) } ?: LocalDateTime.now()
        )
    }

    private fun parseDailyRecordCsvLine(
        line: String,
        startDateToCycle: Map<LocalDate, Cycle>
    ): DailyRecord {
        val fields = parseCsvLine(line)
        require(fields.size >= 2) { "CSV must contain date field" }

        var dateStr = ""
        var cycleStartDateStr: String? = null
        var isPeriodStr = "false"
        var flowLevelStr: String? = null
        var createdAtStr: String? = null
        var updatedAtStr: String? = null

        if (fields.size >= 7) {
            dateStr = fields[1]
            cycleStartDateStr = fields[2].ifBlank { null }
            isPeriodStr = fields[3]
            flowLevelStr = fields[4].ifBlank { null }
            createdAtStr = fields[5].ifBlank { null }
            updatedAtStr = fields[6].ifBlank { null }
        } else {
            dateStr = fields.firstOrNull { it.isNotBlank() } ?: ""
        }

        val cycleStartDate = cycleStartDateStr?.let { LocalDate.parse(it, dateFormatter) }
        val cycle = cycleStartDate?.let { startDateToCycle[it] }

        return DailyRecord(
            id = 0,
            date = LocalDate.parse(dateStr, dateFormatter),
            cycleId = cycle?.id,
            isPeriod = isPeriodStr.toBoolean(),
            flowLevel = flowLevelStr?.takeIf { it.isNotBlank() }?.let { FlowLevel.valueOf(it) },
            createdAt = createdAtStr?.let { LocalDateTime.parse(it, dateTimeFormatter) } ?: LocalDateTime.now(),
            updatedAt = updatedAtStr?.let { LocalDateTime.parse(it, dateTimeFormatter) } ?: LocalDateTime.now()
        )
    }

    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            when (val c = line[i]) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',' -> {
                    if (inQuotes) {
                        current.append(c)
                    } else {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

/**
 * CSV 导入结果
 */
sealed class CsvImportResult<out T> {
    data class Success<out T>(
        val data: T,
        val warnings: List<String> = emptyList()
    ) : CsvImportResult<T>()

    data class Failure(val errorMessage: String) : CsvImportResult<Nothing>()
}
