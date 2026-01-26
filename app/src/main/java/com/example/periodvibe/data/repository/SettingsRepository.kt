package com.example.periodvibe.data.repository

import com.example.periodvibe.data.local.dao.SettingsDao
import com.example.periodvibe.data.mapper.SettingsMapper
import com.example.periodvibe.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val settingsMapper: SettingsMapper
) {

    fun getSettings(): Flow<Settings?> {
        return settingsDao.getSettings().map { entity ->
            entity?.let { settingsMapper.toDomain(it) }
        }
    }

    suspend fun getSettingsSync(): Settings? {
        val entity = settingsDao.getSettingsSync()
        return entity?.let { settingsMapper.toDomain(it) }
    }

    suspend fun insertSettings(settings: Settings) {
        val entity = settingsMapper.toEntity(settings)
        settingsDao.insertSettings(entity)
    }

    suspend fun updateSettings(settings: Settings) {
        val entity = settingsMapper.toEntity(settings)
        settingsDao.updateSettings(entity)
    }

    suspend fun saveInitialSettings(
        cycleLength: Int,
        periodLength: Int,
        autoCalculateCycle: Boolean = true
    ) {
        val settings = Settings(
            autoCalculateCycle = autoCalculateCycle,
            cycleLengthDefault = cycleLength,
            periodLengthDefault = periodLength
        )
        insertSettings(settings)
    }

    suspend fun updateOnboardingVersion(version: Int) {
        val settings = getSettingsSync()
        settings?.let {
            updateSettings(it.copy(onboardingVersion = version))
        }
    }

    suspend fun setAutoCalculateCycle(enabled: Boolean) {
        val settings = getSettingsSync()
        settings?.let {
            updateSettings(it.copy(autoCalculateCycle = enabled))
        }
    }
}