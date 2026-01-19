package com.example.periodvibe.di

import com.example.periodvibe.domain.usecase.GetCalendarDataUseCase
import com.example.periodvibe.domain.usecase.GetHomeDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetCalendarDataUseCase(
        cycleRepository: com.example.periodvibe.data.repository.CycleRepository,
        settingsRepository: com.example.periodvibe.data.repository.SettingsRepository
    ): GetCalendarDataUseCase {
        return GetCalendarDataUseCase(cycleRepository, settingsRepository)
    }
}
