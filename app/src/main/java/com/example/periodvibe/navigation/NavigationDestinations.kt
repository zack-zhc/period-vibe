package com.example.periodvibe.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// 导航路由定义 - 使用 @Serializable 和 NavKey
@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Loading : Screen

    @Serializable
    data object AppLock : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object InitialSetup : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object History : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object DeveloperOptions : Screen
}
