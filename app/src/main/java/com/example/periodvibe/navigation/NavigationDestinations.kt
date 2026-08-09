package com.example.periodvibe.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// 导航路由定义 - 使用 @Serializable 和 NavKey
@Serializable
sealed interface Screen : NavKey {
    // 初始/引导页面
    @Serializable
    data object Loading : Screen

    @Serializable
    data object AppLock : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object InitialSetup : Screen

    // 顶部级路由（底部导航栏）
    @Serializable
    data object Home : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object Settings : Screen

    // 子页面（从顶部级路由导航到的页面）
    @Serializable
    data object History : Screen

    @Serializable
    data object DeveloperOptions : Screen

    // 设置子页面
    @Serializable
    data object CycleParameters : Screen

    @Serializable
    data object Reminders : Screen

    @Serializable
    data object Theme : Screen

    @Serializable
    data object Language : Screen

    @Serializable
    data object Privacy : Screen

    @Serializable
    data object DataManagement : Screen

    @Serializable
    data object About : Screen
}

/**
 * 所有顶部级路由的列表（底部导航栏项目）
 */
val TopLevelScreens = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Settings
)
