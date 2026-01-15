package com.example.periodvibe.domain.model

import java.time.LocalDate

data class Notification(
    val id: Long = 0,
    val type: NotificationType,
    val title: String,
    val message: String,
    val scheduledDate: LocalDate,
    val scheduledTime: String,
    val isSent: Boolean = false,
    val sentAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
