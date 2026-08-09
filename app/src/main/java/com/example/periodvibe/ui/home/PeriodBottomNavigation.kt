package com.example.periodvibe.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarViewMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.periodvibe.navigation.Screen

@Composable
fun PeriodBottomNavigation(
    currentTopLevel: Screen,
    onNavigate: (Screen) -> Unit,
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
            val selected = currentTopLevel == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected)
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
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        screen = Screen.Home,
        labelRes = com.example.periodvibe.R.string.nav_home,
        icon = Icons.Rounded.Home
    )
    object Calendar : BottomNavItem(
        screen = Screen.Calendar,
        labelRes = com.example.periodvibe.R.string.nav_calendar,
        icon = Icons.Rounded.CalendarViewMonth
    )
    object Settings : BottomNavItem(
        screen = Screen.Settings,
        labelRes = com.example.periodvibe.R.string.nav_settings,
        icon = Icons.Rounded.Settings
    )
}
