package com.example.periodvibe.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.ui.theme.PeriodVibeTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateToCycleParameters: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onNavigateToCycleParameters = onNavigateToCycleParameters,
        onNavigateToReminders = onNavigateToReminders,
        onNavigateToTheme = onNavigateToTheme,
        onNavigateToPrivacy = onNavigateToPrivacy,
        onNavigateToDataManagement = onNavigateToDataManagement,
        onNavigateToAbout = onNavigateToAbout,
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onNavigateToCycleParameters: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is SettingsUiState.Loading -> {
            // Loading state - nothing to show for preview
        }
        is SettingsUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .let { if (scrollBehavior != null) it.nestedScroll(scrollBehavior.nestedScrollConnection) else it }
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                val settingsItems = listOf(
                    Icons.Default.DateRange to "周期参数",
                    Icons.Default.Notifications to "提醒设置",
                    Icons.Default.Palette to "主题设置",
                    Icons.Default.Lock to "隐私设置",
                    Icons.Default.Folder to "数据管理",
                    Icons.Default.Info to "关于"
                )

                val onClicks = listOf(
                    onNavigateToCycleParameters,
                    onNavigateToReminders,
                    onNavigateToTheme,
                    onNavigateToPrivacy,
                    onNavigateToDataManagement,
                    onNavigateToAbout
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    settingsItems.forEachIndexed { index, item ->
                        SegmentedListItem(
                            onClick = onClicks[index],
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = settingsItems.size),
                            leadingContent = {
                                Icon(
                                    imageVector = item.first,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = item.second,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "所有数据都存储在您的设备本地，最大限度保护您的隐私。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "设置页面")
@Composable
private fun SettingsScreenPreview() {
    PeriodVibeTheme {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            androidx.compose.material3.rememberTopAppBarState()
        )
        androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                androidx.compose.material3.MediumTopAppBar(
                    title = { Text("设置") },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            SettingsContent(
                uiState = SettingsUiState.Success(
                    autoCalculateCycle = true,
                    cycleLengthDefault = 28,
                    periodLengthDefault = 5,
                    cycleLengthRange = 21..35,
                    periodLengthRange = 3..7,
                    notificationEnabled = true,
                    notificationDaysBefore = 2,
                    notificationTime = java.time.LocalTime.of(9, 0),
                    themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                    appLockEnabled = false,
                    privacyModeEnabled = false,
                    language = "zh",
                    periodNotificationEnabled = true,
                    ovulationNotificationEnabled = true,
                    ovulationNotificationDaysBefore = 1
                ),
                onNavigateToCycleParameters = { },
                onNavigateToReminders = { },
                onNavigateToTheme = { },
                onNavigateToPrivacy = { },
                onNavigateToDataManagement = { },
                onNavigateToAbout = { },
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}
