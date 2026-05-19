package com.example.periodvibe.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.ui.settings.components.AboutDialog
import com.example.periodvibe.ui.settings.components.ClearDataConfirmationDialog
import com.example.periodvibe.ui.settings.components.CycleParametersDialog
import com.example.periodvibe.ui.settings.components.DisableAppLockConfirmationDialog
import com.example.periodvibe.ui.settings.components.ExportFormatDialog
import com.example.periodvibe.ui.settings.components.ExportResultDialog
import com.example.periodvibe.ui.settings.components.ImportConfirmationDialog
import com.example.periodvibe.ui.settings.components.ImportResultDialog
import com.example.periodvibe.ui.settings.components.NotificationTimeDialog
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.utils.AppUtils
import java.time.LocalDateTime
import java.time.LocalTime

// ==================== 周期参数设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CycleParametersScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CycleParametersContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onToggleAutoCalculateCycle = { viewModel.toggleAutoCalculateCycle(it) },
        onUpdateCycleLength = { viewModel.updateCycleLength(it) },
        onUpdatePeriodLength = { viewModel.updatePeriodLength(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CycleParametersContent(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onToggleAutoCalculateCycle: (Boolean) -> Unit,
    onUpdateCycleLength: (Int) -> Unit,
    onUpdatePeriodLength: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("周期参数") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState
                val items = mutableListOf<@Composable () -> Unit>()
                items.add {
                    SegmentedListItem(
                        onClick = { onToggleAutoCalculateCycle(!state.autoCalculateCycle) },
                        shapes = if (!state.autoCalculateCycle) {
                            ListItemDefaults.segmentedShapes(index = 0, count = 3)
                        } else {
                            ListItemDefaults.segmentedShapes(index = 0, count = 1)
                        },
                        supportingContent = {
                            Text(
                                text = if (state.autoCalculateCycle) "根据历史数据自动计算" else "使用手动设置的值",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.autoCalculateCycle,
                                onCheckedChange = { onToggleAutoCalculateCycle(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Text(
                            text = "自动计算周期",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (!state.autoCalculateCycle) {
                    items.add {
                        SegmentedListItem(
                            onClick = { },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                            supportingContent = {
                                Column {
                                    Text(
                                        text = "${state.cycleLengthDefault} 天",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Slider(
                                        value = state.cycleLengthDefault.toFloat(),
                                        onValueChange = { onUpdateCycleLength(it.toInt()) },
                                        valueRange = state.cycleLengthRange.first.toFloat()..state.cycleLengthRange.last.toFloat(),
                                        steps = state.cycleLengthRange.last - state.cycleLengthRange.first - 1
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = "平均周期天数",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    items.add {
                        SegmentedListItem(
                            onClick = { },
                            shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                            supportingContent = {
                                Column {
                                    Text(
                                        text = "${state.periodLengthDefault} 天",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Slider(
                                        value = state.periodLengthDefault.toFloat(),
                                        onValueChange = { onUpdatePeriodLength(it.toInt()) },
                                        valueRange = state.periodLengthRange.first.toFloat()..state.periodLengthRange.last.toFloat(),
                                        steps = state.periodLengthRange.last - state.periodLengthRange.first - 1
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = "平均经期天数",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    items.forEach { it() }
                }
            }
        }
    }
}

// ==================== 提醒设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showTimeDialog by viewModel.showTimeDialog.collectAsState()

    RemindersContent(
        uiState = uiState,
        showTimeDialog = showTimeDialog,
        onNavigateBack = onNavigateBack,
        onToggleNotificationEnabled = { viewModel.toggleNotificationEnabled(it) },
        onUpdateNotificationDaysBefore = { viewModel.updateNotificationDaysBefore(it) },
        onShowTimeDialog = { viewModel.showTimeDialog() },
        onHideTimeDialog = { viewModel.hideTimeDialog() },
        onUpdateNotificationTime = { viewModel.updateNotificationTime(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RemindersContent(
    uiState: SettingsUiState,
    showTimeDialog: Boolean,
    onNavigateBack: () -> Unit,
    onToggleNotificationEnabled: (Boolean) -> Unit,
    onUpdateNotificationDaysBefore: (Int) -> Unit,
    onShowTimeDialog: () -> Unit,
    onHideTimeDialog: () -> Unit,
    onUpdateNotificationTime: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("提醒设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    SegmentedListItem(
                        onClick = { onToggleNotificationEnabled(!state.notificationEnabled) },
                        shapes = if (state.notificationEnabled) {
                            ListItemDefaults.segmentedShapes(index = 0, count = 3)
                        } else {
                            ListItemDefaults.segmentedShapes(index = 0, count = 1)
                        },
                        supportingContent = {
                            Text(
                                text = if (state.notificationEnabled) "在经期前提醒你" else "关闭所有提醒",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.notificationEnabled,
                                onCheckedChange = { onToggleNotificationEnabled(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Text(
                            text = "经期提醒",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (state.notificationEnabled) {
                        SegmentedListItem(
                            onClick = { },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                            supportingContent = {
                                Column {
                                    Text(
                                        text = "${state.notificationDaysBefore} 天",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Slider(
                                        value = state.notificationDaysBefore.toFloat(),
                                        onValueChange = { onUpdateNotificationDaysBefore(Math.round(it)) },
                                        valueRange = 1f..7f,
                                        steps = 5
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = "提前天数",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        SegmentedListItem(
                            onClick = { onShowTimeDialog() },
                            shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${state.notificationTime.hour.toString().padStart(2, '0')}:${state.notificationTime.minute.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = "提醒时间",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimeDialog && uiState is SettingsUiState.Success) {
        val state = uiState as SettingsUiState.Success
        NotificationTimeDialog(
            time = state.notificationTime,
            onDismiss = { onHideTimeDialog() },
            onConfirm = { time -> onUpdateNotificationTime(time) }
        )
    }
}

// ==================== 主题设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ThemeContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUpdateThemeMode = { viewModel.updateThemeMode(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeContent(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onUpdateThemeMode: (com.example.periodvibe.domain.model.Settings.ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("主题设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState
                Text(
                    text = "选择你的偏好",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val themeOptions = listOf(
                    Triple(com.example.periodvibe.domain.model.Settings.ThemeMode.LIGHT, "浅色", Icons.Default.LightMode),
                    Triple(com.example.periodvibe.domain.model.Settings.ThemeMode.DARK, "深色", Icons.Default.DarkMode),
                    Triple(com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM, "系统", Icons.Default.PhoneAndroid)
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    themeOptions.forEachIndexed { index, (mode, label, icon) ->
                        ToggleButton(
                            checked = state.themeMode == mode,
                            onCheckedChange = { onUpdateThemeMode(mode) },
                            modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                themeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Icon(icon, contentDescription = null)
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 隐私设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPinSetup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDisableAppLockDialog by viewModel.showDisableAppLockDialog.collectAsState()

    PrivacyContent(
        uiState = uiState,
        showDisableAppLockDialog = showDisableAppLockDialog,
        onNavigateBack = onNavigateBack,
        onNavigateToPinSetup = onNavigateToPinSetup,
        onTogglePrivacyMode = { viewModel.togglePrivacyMode(it) },
        onShowDisableAppLockDialog = { viewModel.showDisableAppLockDialog() },
        onHideDisableAppLockDialog = { viewModel.hideDisableAppLockDialog() },
        onToggleAppLock = { viewModel.toggleAppLock(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrivacyContent(
    uiState: SettingsUiState,
    showDisableAppLockDialog: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPinSetup: () -> Unit,
    onTogglePrivacyMode: (Boolean) -> Unit,
    onShowDisableAppLockDialog: () -> Unit,
    onHideDisableAppLockDialog: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("隐私设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState
                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    SegmentedListItem(
                        onClick = {
                            if (state.appLockEnabled) {
                                onShowDisableAppLockDialog()
                            } else {
                                onNavigateToPinSetup()
                            }
                        },
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                        supportingContent = {
                            Text(
                                text = "使用指纹或密码保护应用",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.appLockEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        onNavigateToPinSetup()
                                    } else {
                                        onShowDisableAppLockDialog()
                                    }
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Text(
                            text = "应用锁",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    SegmentedListItem(
                        onClick = { onTogglePrivacyMode(!state.privacyModeEnabled) },
                        shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                        supportingContent = {
                            Text(
                                text = "隐藏通知内容",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.privacyModeEnabled,
                                onCheckedChange = { onTogglePrivacyMode(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Text(
                            text = "隐私模式",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showDisableAppLockDialog) {
        DisableAppLockConfirmationDialog(
            onDismiss = { onHideDisableAppLockDialog() },
            onConfirm = { onToggleAppLock(false) }
        )
    }
}

// ==================== 数据管理页面 ====================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    DataManagementContent(
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DataManagementContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                SegmentedListItem(
                    onClick = { },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
                    supportingContent = {
                        Text(
                            text = "选择格式",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "导出数据",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SegmentedListItem(
                    onClick = { },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                    supportingContent = {
                        Text(
                            text = "从备份恢复",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "导入数据",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SegmentedListItem(
                    onClick = { },
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                    supportingContent = {
                        Text(
                            text = "删除所有记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "清除数据",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ==================== 关于页面 ====================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    AboutContent(
        onNavigateBack = onNavigateBack,
        onNavigateToDeveloperOptions = onNavigateToDeveloperOptions,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutContent(
    onNavigateBack: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember { com.example.periodvibe.utils.AppUtils.getVersionName(context) }

    var showDialog by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(0) }
    var firstClickTime by remember { mutableLongStateOf(0L) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                SegmentedListItem(
                    onClick = { showDialog = true },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
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
                        text = "应用介绍",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SegmentedListItem(
                    onClick = {
                        val currentTime = System.currentTimeMillis()

                        if (firstClickTime == 0L) {
                            firstClickTime = currentTime
                            clickCount = 1
                        } else {
                            val elapsedTime = currentTime - firstClickTime
                            if (elapsedTime > 8000) {
                                clickCount = 1
                                firstClickTime = currentTime
                            } else {
                                clickCount++
                            }
                        }

                        if (clickCount >= 10) {
                            clickCount = 1
                            firstClickTime = currentTime
                            onNavigateToDeveloperOptions()
                        }
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                    supportingContent = {
                        Text(
                            text = "v$versionName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Text(
                        text = "版本信息",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showDialog) {
        AboutDialog(
            onDismiss = { showDialog = false }
        )
    }
}

// ==================== Previews ====================

@Preview(showBackground = true, name = "提醒设置 - 开启")
@Composable
private fun RemindersScreenEnabledPreview() {
    PeriodVibeTheme {
        RemindersContent(
            uiState = SettingsUiState.Success(
                autoCalculateCycle = true,
                cycleLengthDefault = 28,
                periodLengthDefault = 5,
                cycleLengthRange = 21..35,
                periodLengthRange = 3..7,
                notificationEnabled = true,
                notificationDaysBefore = 2,
                notificationTime = LocalTime.of(9, 0),
                themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                appLockEnabled = false,
                privacyModeEnabled = false,
                language = "zh"
            ),
            showTimeDialog = false,
            onNavigateBack = { },
            onToggleNotificationEnabled = { },
            onUpdateNotificationDaysBefore = { },
            onShowTimeDialog = { },
            onHideTimeDialog = { },
            onUpdateNotificationTime = { }
        )
    }
}

@Preview(showBackground = true, name = "提醒设置 - 关闭")
@Composable
private fun RemindersScreenDisabledPreview() {
    PeriodVibeTheme {
        RemindersContent(
            uiState = SettingsUiState.Success(
                autoCalculateCycle = true,
                cycleLengthDefault = 28,
                periodLengthDefault = 5,
                cycleLengthRange = 21..35,
                periodLengthRange = 3..7,
                notificationEnabled = false,
                notificationDaysBefore = 2,
                notificationTime = LocalTime.of(9, 0),
                themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                appLockEnabled = false,
                privacyModeEnabled = false,
                language = "zh"
            ),
            showTimeDialog = false,
            onNavigateBack = { },
            onToggleNotificationEnabled = { },
            onUpdateNotificationDaysBefore = { },
            onShowTimeDialog = { },
            onHideTimeDialog = { },
            onUpdateNotificationTime = { }
        )
    }
}

@Preview(showBackground = true, name = "周期参数 - 自动")
@Composable
private fun CycleParametersScreenAutoPreview() {
    PeriodVibeTheme {
        CycleParametersContent(
            uiState = SettingsUiState.Success(
                autoCalculateCycle = true,
                cycleLengthDefault = 28,
                periodLengthDefault = 5,
                cycleLengthRange = 21..35,
                periodLengthRange = 3..7,
                notificationEnabled = true,
                notificationDaysBefore = 2,
                notificationTime = LocalTime.of(9, 0),
                themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                appLockEnabled = false,
                privacyModeEnabled = false,
                language = "zh"
            ),
            onNavigateBack = { },
            onToggleAutoCalculateCycle = { },
            onUpdateCycleLength = { },
            onUpdatePeriodLength = { }
        )
    }
}

@Preview(showBackground = true, name = "周期参数 - 手动")
@Composable
private fun CycleParametersScreenManualPreview() {
    PeriodVibeTheme {
        CycleParametersContent(
            uiState = SettingsUiState.Success(
                autoCalculateCycle = false,
                cycleLengthDefault = 30,
                periodLengthDefault = 6,
                cycleLengthRange = 21..35,
                periodLengthRange = 3..7,
                notificationEnabled = true,
                notificationDaysBefore = 2,
                notificationTime = LocalTime.of(9, 0),
                themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                appLockEnabled = false,
                privacyModeEnabled = false,
                language = "zh"
            ),
            onNavigateBack = { },
            onToggleAutoCalculateCycle = { },
            onUpdateCycleLength = { },
            onUpdatePeriodLength = { }
        )
    }
}

@Preview(showBackground = true, name = "主题设置")
@Composable
private fun ThemeScreenPreview() {
    PeriodVibeTheme {
        ThemeContent(
            uiState = SettingsUiState.Success(
                autoCalculateCycle = true,
                cycleLengthDefault = 28,
                periodLengthDefault = 5,
                cycleLengthRange = 21..35,
                periodLengthRange = 3..7,
                notificationEnabled = true,
                notificationDaysBefore = 2,
                notificationTime = LocalTime.of(9, 0),
                themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                appLockEnabled = false,
                privacyModeEnabled = false,
                language = "zh"
            ),
            onNavigateBack = { },
            onUpdateThemeMode = { }
        )
    }
}

@Preview(showBackground = true, name = "隐私设置")
@Composable
private fun PrivacyScreenPreview() {
    PeriodVibeTheme {
        PrivacyContent(
            uiState = SettingsUiState.Success(
                autoCalculateCycle = true,
                cycleLengthDefault = 28,
                periodLengthDefault = 5,
                cycleLengthRange = 21..35,
                periodLengthRange = 3..7,
                notificationEnabled = true,
                notificationDaysBefore = 2,
                notificationTime = LocalTime.of(9, 0),
                themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                appLockEnabled = true,
                privacyModeEnabled = true,
                language = "zh"
            ),
            showDisableAppLockDialog = false,
            onNavigateBack = { },
            onNavigateToPinSetup = { },
            onTogglePrivacyMode = { },
            onShowDisableAppLockDialog = { },
            onHideDisableAppLockDialog = { },
            onToggleAppLock = { }
        )
    }
}

@Preview(showBackground = true, name = "数据管理")
@Composable
private fun DataManagementScreenPreview() {
    PeriodVibeTheme {
        DataManagementContent(
            onNavigateBack = { }
        )
    }
}

@Preview(showBackground = true, name = "关于页面")
@Composable
private fun AboutScreenPreview() {
    PeriodVibeTheme {
        AboutContent(
            onNavigateBack = { },
            onNavigateToDeveloperOptions = { }
        )
    }
}
