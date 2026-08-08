package com.example.periodvibe.ui.widget

import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.usecase.GetHomeDataUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getHomeDataUseCase(): GetHomeDataUseCase
    fun settingsRepository(): SettingsRepository
}
