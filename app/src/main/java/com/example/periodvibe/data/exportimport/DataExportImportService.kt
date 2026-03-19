package com.example.periodvibe.data.exportimport

import android.content.Context
import android.net.Uri
import com.example.periodvibe.data.local.entity.CycleEntity
import com.example.periodvibe.data.local.entity.DailyRecordEntity
import com.example.periodvibe.data.mapper.CycleMapper
import com.example.periodvibe.data.mapper.DailyRecordMapper
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.Symptom
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LocalDate 序列化器
 */
private class LocalDateSerializer : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    override fun serialize(
        src: LocalDate?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate {
        return LocalDate.parse(json?.asString)
    }
}

/**
 * LocalDateTime 序列化器
 */
private class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        return LocalDateTime.parse(json?.asString)
    }
}

/**
 * 数据导出导入服务
 */
@Singleton
class DataExportImportService @Inject constructor(
    private val cycleMapper: CycleMapper,
    private val dailyRecordMapper: DailyRecordMapper
) {
    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateSerializer())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            .setPrettyPrinting()
            .create()
    }

    /**
     * 将周期和日常记录数据导出为 JSON 字符串
     */
    fun exportToJson(cycles: List<Cycle>, dailyRecords: List<DailyRecord>): String {
        val cycleDtos = cycles.map { cycleToDto(it) }
        val recordDtos = dailyRecords.map { dailyRecordToDto(it, cycles) }

        val data = ExportImportData(
            cycles = cycleDtos,
            dailyRecords = recordDtos
        )

        return gson.toJson(data)
    }

    /**
     * 从 JSON 字符串导入数据
     */
    fun importFromJson(jsonString: String): ImportResult {
        return try {
            val data = gson.fromJson(jsonString, ExportImportData::class.java)

            // 验证数据
            val validationErrors = validateData(data)
            if (validationErrors.isNotEmpty()) {
                return ImportResult.Failure(validationErrors.joinToString("\n"))
            }

            // 转换为实体
            val cycles = data.cycles.map { dtoToCycle(it) }
            val dailyRecords = data.dailyRecords.map { dtoToDailyRecord(it, cycles) }

            ImportResult.Success(cycles, dailyRecords)
        } catch (e: Exception) {
            ImportResult.Failure("数据解析失败: ${e.message}")
        }
    }

    /**
     * 将数据写入文件
     */
    suspend fun writeToFile(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从文件读取数据
     */
    suspend fun readFromFile(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun cycleToDto(cycle: Cycle): CycleDto {
        return CycleDto(
            id = null, // 导出时不包含 ID，导入时重新生成
            startDate = cycle.startDate,
            endDate = cycle.endDate,
            cycleLength = cycle.cycleLength,
            periodLength = cycle.periodLength,
            averageFlowLevel = cycle.averageFlowLevel?.name,
            isCompleted = cycle.isCompleted,
            createdAt = cycle.createdAt,
            updatedAt = cycle.updatedAt
        )
    }

    private fun dailyRecordToDto(record: DailyRecord, cycles: List<Cycle>): DailyRecordDto {
        // 找到对应的周期，记录周期的开始日期而不是 ID
        val cycleDate = record.cycleId?.let { cycleId ->
            cycles.find { it.id == cycleId }?.startDate
        }

        return DailyRecordDto(
            id = null,
            date = record.date,
            cycleDate = cycleDate,
            isPeriod = record.isPeriod,
            flowLevel = record.flowLevel?.name,
            symptoms = record.symptoms.map { it.name },
            notes = record.notes,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt
        )
    }

    private fun dtoToCycle(dto: CycleDto): Cycle {
        return Cycle(
            id = 0, // 导入时使用 0，让数据库自动生成 ID
            startDate = dto.startDate,
            endDate = dto.endDate,
            cycleLength = dto.cycleLength,
            periodLength = dto.periodLength,
            averageFlowLevel = dto.averageFlowLevel?.let { FlowLevel.valueOf(it) },
            isCompleted = dto.isCompleted,
            createdAt = dto.createdAt ?: LocalDateTime.now(),
            updatedAt = dto.updatedAt ?: LocalDateTime.now()
        )
    }

    private fun dtoToDailyRecord(dto: DailyRecordDto, cycles: List<Cycle>): DailyRecord {
        // 根据周期开始日期找到对应的周期 ID（导入后生成的新 ID）
        val cycleId = dto.cycleDate?.let { cycleDate ->
            cycles.find { it.startDate == cycleDate }?.id
        }

        return DailyRecord(
            id = 0,
            date = dto.date,
            cycleId = cycleId,
            isPeriod = dto.isPeriod,
            flowLevel = dto.flowLevel?.let { FlowLevel.valueOf(it) },
            symptoms = dto.symptoms.mapNotNull { symptomName ->
                Symptom.values().find { it.name == symptomName }
            },
            notes = dto.notes,
            createdAt = dto.createdAt ?: LocalDateTime.now(),
            updatedAt = dto.updatedAt ?: LocalDateTime.now()
        )
    }

    private fun validateData(data: ExportImportData): List<String> {
        val errors = mutableListOf<String>()

        // 验证版本
        if (data.version != 1) {
            errors.add("不支持的数据格式版本: ${data.version}")
        }

        // 验证周期数据
        val cycleDates = mutableSetOf<LocalDate>()
        data.cycles.forEachIndexed { index, cycle ->
            if (cycleDates.contains(cycle.startDate)) {
                errors.add("周期数据重复: ${cycle.startDate}")
            }
            cycleDates.add(cycle.startDate)
        }

        // 验证日常记录数据
        val recordDates = mutableSetOf<LocalDate>()
        data.dailyRecords.forEachIndexed { index, record ->
            if (recordDates.contains(record.date)) {
                errors.add("日常记录数据重复: ${record.date}")
            }
            recordDates.add(record.date)

            // 验证 FlowLevel
            record.flowLevel?.let { flowLevelName ->
                try {
                    FlowLevel.valueOf(flowLevelName)
                } catch (e: IllegalArgumentException) {
                    errors.add("无效的经量值: $flowLevelName (记录日期: ${record.date})")
                }
            }

            // 验证 Symptoms
            record.symptoms.forEach { symptomName ->
                try {
                    Symptom.valueOf(symptomName)
                } catch (e: IllegalArgumentException) {
                    errors.add("无效的症状值: $symptomName (记录日期: ${record.date})")
                }
            }
        }

        return errors
    }
}

/**
 * 导入结果
 */
sealed class ImportResult {
    data class Success(
        val cycles: List<Cycle>,
        val dailyRecords: List<DailyRecord>
    ) : ImportResult()

    data class Failure(val errorMessage: String) : ImportResult()
}
