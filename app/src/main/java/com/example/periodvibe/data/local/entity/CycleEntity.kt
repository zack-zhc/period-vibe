package com.example.periodvibe.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "cycles",
    indices = [
        Index(value = ["start_date"]),
        Index(value = ["end_date"])
    ]
)
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDate?,

    @ColumnInfo(name = "cycle_length")
    val cycleLength: Int?,

    @ColumnInfo(name = "period_length")
    val periodLength: Int?,

    @ColumnInfo(name = "average_flow_level")
    val averageFlowLevel: String?,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime
)
