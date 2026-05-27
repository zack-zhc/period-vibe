package com.example.periodvibe.data.repository

import com.example.periodvibe.data.local.dao.NotificationDao
import com.example.periodvibe.data.mapper.NotificationMapper
import com.example.periodvibe.domain.model.Notification
import com.example.periodvibe.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val notificationMapper: NotificationMapper
) {

    fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
            notificationMapper.toDomainList(entities)
        }
    }

    suspend fun getNotificationById(id: Long): Notification? {
        val entity = notificationDao.getNotificationById(id)
        return entity?.let { notificationMapper.toDomain(it) }
    }

    fun getUnsentNotifications(): Flow<List<Notification>> {
        return notificationDao.getUnsentNotifications().map { entities ->
            notificationMapper.toDomainList(entities)
        }
    }

    fun getNotificationsByType(type: NotificationType): Flow<List<Notification>> {
        return notificationDao.getNotificationsByType(type.name).map { entities ->
            notificationMapper.toDomainList(entities)
        }
    }

    suspend fun insertNotification(notification: Notification): Long {
        val entity = notificationMapper.toEntity(notification)
        return notificationDao.insertNotification(entity)
    }

    suspend fun updateNotification(notification: Notification) {
        val entity = notificationMapper.toEntity(notification)
        notificationDao.updateNotification(entity)
    }

    suspend fun deleteNotification(notification: Notification) {
        val entity = notificationMapper.toEntity(notification)
        notificationDao.deleteNotification(entity)
    }

    suspend fun deleteAllNotifications() {
        notificationDao.deleteAllNotifications()
    }

    suspend fun insertAllNotifications(notifications: List<Notification>) {
        notifications.forEach { insertNotification(it) }
    }
}
