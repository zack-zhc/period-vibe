package com.example.periodvibe.data.exportimport

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 导出/导入数据的根容器
 */
data class ExportImportData(
    @SerializedName("version")
    val version: Int = 1,

    @SerializedName("exported_at")
    val exportedAt: LocalDateTime = LocalDateTime.now(),

    @SerializedName("cycles")
    val cycles: List<CycleDto> = emptyList(),

    @SerializedName("daily_records")
    val dailyRecords: List<DailyRecordDto> = emptyList()
)

/**
 * 周期数据 DTO（用于导出/导入）
 */
data class CycleDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("start_date")
    val startDate: LocalDate,

    @SerializedName("end_date")
    val endDate: LocalDate? = null,

    @SerializedName("cycle_length")
    val cycleLength: Int? = null,

    @SerializedName("period_length")
    val periodLength: Int? = null,

    @SerializedName("average_flow_level")
    val averageFlowLevel: String? = null,

    @SerializedName("is_completed")
    val isCompleted: Boolean = false,

    @SerializedName("created_at")
    val createdAt: LocalDateTime? = null,

    @SerializedName("updated_at")
    val updatedAt: LocalDateTime? = null
)

/**
 * 日常记录 DTO（用于导出/导入）
 */
data class DailyRecordDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("date")
    val date: LocalDate,

    @SerializedName("cycle_date")
    val cycleDate: LocalDate? = null,

    @SerializedName("is_period")
    val isPeriod: Boolean,

    @SerializedName("flow_level")
    val flowLevel: String? = null,

    @SerializedName("symptoms")
    val symptoms: List<String> = emptyList(),

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("created_at")
    val createdAt: LocalDateTime? = null,

    @SerializedName("updated_at")
    val updatedAt: LocalDateTime? = null
)
