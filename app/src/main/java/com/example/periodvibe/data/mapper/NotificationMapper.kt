package com.example.periodvibe.data.mapper

import com.example.periodvibe.data.local.entity.NotificationEntity
import com.example.periodvibe.domain.model.Notification
import com.example.periodvibe.domain.model.NotificationType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationMapper @Inject constructor() {

    fun toDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            type = NotificationType.valueOf(entity.type),
            title = entity.title,
            message = entity.message,
            scheduledDate = entity.scheduledDate,
            scheduledTime = entity.scheduledTime,
            isSent = entity.isSent,
            sentAt = entity.sentAt,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: Notification): NotificationEntity {
        return NotificationEntity(
            id = domain.id,
            type = domain.type.name,
            title = domain.title,
            message = domain.message,
            scheduledDate = domain.scheduledDate,
            scheduledTime = domain.scheduledTime,
            isSent = domain.isSent,
            sentAt = domain.sentAt,
            createdAt = domain.createdAt
        )
    }

    fun toDomainList(entities: List<NotificationEntity>): List<Notification> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<Notification>): List<NotificationEntity> {
        return domains.map { toEntity(it) }
    }
}
