package com.example.periodvibe.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["scheduled_date"]),
        Index(value = ["type"]),
        Index(value = ["is_sent"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "scheduled_date")
    val scheduledDate: LocalDate,

    @ColumnInfo(name = "scheduled_time")
    val scheduledTime: String,

    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false,

    @ColumnInfo(name = "sent_at")
    val sentAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
