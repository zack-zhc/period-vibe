package com.example.periodvibe.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PeriodBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Material 3 Expressive 风格底部导航栏
    // 使用更高的容器颜色和更大的图标/文字
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Calendar,
            BottomNavItem.Settings
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentRoute == item.route)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        label = "首页",
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    object Calendar : BottomNavItem(
        route = "calendar",
        label = "日历",
        unselectedIcon = Icons.Outlined.CalendarViewMonth,
        selectedIcon = Icons.Filled.CalendarViewMonth
    )
    object History : BottomNavItem(
        route = "history",
        label = "历史",
        unselectedIcon = Icons.Outlined.History,
        selectedIcon = Icons.Filled.History
    )
    object Settings : BottomNavItem(
        route = "settings",
        label = "设置",
        unselectedIcon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )
}
