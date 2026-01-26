package com.example.periodvibe.util

import com.example.periodvibe.data.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    companion object {
        const val CURRENT_ONBOARDING_VERSION = 1
    }

    suspend fun shouldShowOnboarding(): Boolean {
        val settings = settingsRepository.getSettingsSync()
        return settings == null || settings.onboardingVersion < CURRENT_ONBOARDING_VERSION
    }

    suspend fun markOnboardingCompleted() {
        settingsRepository.updateOnboardingVersion(CURRENT_ONBOARDING_VERSION)
    }

    suspend fun resetOnboarding() {
        settingsRepository.updateOnboardingVersion(0)
    }

    suspend fun getOnboardingVersion(): Int {
        val settings = settingsRepository.getSettingsSync()
        return settings?.onboardingVersion ?: 0
    }
}
