package com.example.periodvibe.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
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
    data object Home : Screen, TopLevelScreen {
        override val icon: ImageVector = Icons.Default.Home
        override val label: String = "首页"
    }

    @Serializable
    data object Calendar : Screen, TopLevelScreen {
        override val icon: ImageVector = Icons.Default.CalendarMonth
        override val label: String = "日历"
    }

    @Serializable
    data object Settings : Screen, TopLevelScreen {
        override val icon: ImageVector = Icons.Default.Settings
        override val label: String = "设置"
    }

    // 子页面（从顶部级路由导航到的页面）
    @Serializable
    data object History : Screen

    @Serializable
    data object DeveloperOptions : Screen
}

/**
 * 顶部级屏幕接口（底部导航栏项目）
 */
interface TopLevelScreen {
    val icon: ImageVector
    val label: String
}

/**
 * 所有顶部级路由的列表
 */
val TopLevelScreens = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Settings
)
