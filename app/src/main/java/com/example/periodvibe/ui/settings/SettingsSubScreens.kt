package com.example.periodvibe.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.R
import com.example.periodvibe.ui.settings.components.AboutDialog
import com.example.periodvibe.ui.settings.components.ClearDataConfirmationDialog
import com.example.periodvibe.ui.settings.components.CycleParametersDialog
import com.example.periodvibe.ui.settings.components.DisableAppLockConfirmationDialog
import com.example.periodvibe.ui.settings.components.ExportFormatDialog
import com.example.periodvibe.ui.settings.components.ExportResultDialog
import com.example.periodvibe.ui.settings.components.ImportConfirmationDialog
import com.example.periodvibe.ui.settings.components.ImportResultDialog
import com.example.periodvibe.ui.settings.components.NotificationTimeDialog
import com.example.periodvibe.ui.settings.components.VerifyPinDialog
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.util.AppLockGuard
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
                title = { Text(stringResource(R.string.set_cycle_parameters)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.set_back)
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
            if (uiState !is SettingsUiState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
                return@Column
            }
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
                                text = if (state.autoCalculateCycle) {
                                    stringResource(R.string.set_auto_calculate_description)
                                } else {
                                    stringResource(R.string.set_manual_values_description)
                                },
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
                            text = stringResource(R.string.set_auto_calculate_cycle),
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
                                        text = stringResource(R.string.set_days_with_space, state.cycleLengthDefault),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.set_days, state.cycleLengthRange.first),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        var cycleLengthValue by remember(state.cycleLengthDefault) {
                                            mutableFloatStateOf(state.cycleLengthDefault.toFloat())
                                        }
                                        Slider(
                                            value = cycleLengthValue,
                                            onValueChange = { cycleLengthValue = it },
                                            onValueChangeFinished = { onUpdateCycleLength(cycleLengthValue.toInt()) },
                                            valueRange = state.cycleLengthRange.first.toFloat()..state.cycleLengthRange.last.toFloat(),
                                            steps = (state.cycleLengthRange.last - state.cycleLengthRange.first - 1).coerceAtLeast(0),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.set_days, state.cycleLengthRange.last),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.set_avg_cycle_days),
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
                                        text = stringResource(R.string.set_days_with_space, state.periodLengthDefault),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.set_days, state.periodLengthRange.first),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        var periodLengthValue by remember(state.periodLengthDefault) {
                                            mutableFloatStateOf(state.periodLengthDefault.toFloat())
                                        }
                                        Slider(
                                            value = periodLengthValue,
                                            onValueChange = { periodLengthValue = it },
                                            onValueChangeFinished = { onUpdatePeriodLength(periodLengthValue.toInt()) },
                                            valueRange = state.periodLengthRange.first.toFloat()..state.periodLengthRange.last.toFloat(),
                                            steps = (state.periodLengthRange.last - state.periodLengthRange.first - 1).coerceAtLeast(0),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.set_days, state.periodLengthRange.last),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.set_avg_period_days),
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

    val context = LocalContext.current
    val alarmManager = remember {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    val canScheduleExactAlarms = remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        )
    }
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 用户从系统设置返回后重新检查权限并安排通知，避免提示条陈旧
        canScheduleExactAlarms.value =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        viewModel.rescheduleNotifications()
    }

    RemindersContent(
        uiState = uiState,
        showTimeDialog = showTimeDialog,
        onNavigateBack = onNavigateBack,
        onToggleNotificationEnabled = { viewModel.toggleNotificationEnabled(it) },
        onUpdateNotificationDaysBefore = { viewModel.updateNotificationDaysBefore(it) },
        onShowTimeDialog = { viewModel.showTimeDialog() },
        onHideTimeDialog = { viewModel.hideTimeDialog() },
        onUpdateNotificationTime = { viewModel.updateNotificationTime(it) },
        onTogglePeriodNotification = { viewModel.togglePeriodNotification(it) },
        onToggleOvulationNotification = { viewModel.toggleOvulationNotification(it) },
        onUpdateOvulationDaysBefore = { viewModel.updateOvulationDaysBefore(it) },
        showExactAlarmHint = !canScheduleExactAlarms.value,
        onRequestExactAlarmPermission = {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            exactAlarmLauncher.launch(intent)
        },
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
    onTogglePeriodNotification: (Boolean) -> Unit,
    onToggleOvulationNotification: (Boolean) -> Unit,
    onUpdateOvulationDaysBefore: (Int) -> Unit,
    showExactAlarmHint: Boolean = false,
    onRequestExactAlarmPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.set_reminders)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.set_back)
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
            if (uiState !is SettingsUiState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
                return@Column
            }
            if (uiState is SettingsUiState.Success) {
                val state = uiState

                // 分段列表的实际条目数与索引随开关组合动态变化
                val periodDaysShown = state.notificationEnabled && state.periodNotificationEnabled
                val ovulationDaysShown = state.notificationEnabled && state.ovulationNotificationEnabled
                val remindersTotalCount = if (state.notificationEnabled) {
                    6 - (if (periodDaysShown) 0 else 1) - (if (ovulationDaysShown) 0 else 1)
                } else {
                    1
                }
                val ovulationToggleIndex = if (periodDaysShown) 3 else 2
                val ovulationDaysIndex = if (periodDaysShown) 4 else 3

                if (showExactAlarmHint) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.set_exact_alarm_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.set_exact_alarm_message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            TextButton(onClick = onRequestExactAlarmPermission) {
                                Text(stringResource(R.string.set_exact_alarm_action))
                            }
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    SegmentedListItem(
                        onClick = { onToggleNotificationEnabled(!state.notificationEnabled) },
                        shapes = if (state.notificationEnabled) {
                            ListItemDefaults.segmentedShapes(index = 0, count = remindersTotalCount)
                        } else {
                            ListItemDefaults.segmentedShapes(index = 0, count = 1)
                        },
                        supportingContent = {
                            Text(
                                text = if (state.notificationEnabled) {
                                    stringResource(R.string.set_period_reminder_description)
                                } else {
                                    stringResource(R.string.set_all_notifications_off)
                                },
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
                            text = stringResource(R.string.set_period_reminder),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (state.notificationEnabled) {
                        SegmentedListItem(
                            onClick = { onTogglePeriodNotification(!state.periodNotificationEnabled) },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = remindersTotalCount),
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.set_period_start_reminder_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = state.periodNotificationEnabled,
                                    onCheckedChange = { onTogglePeriodNotification(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.set_period_start_reminder),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (state.periodNotificationEnabled) {
                            SegmentedListItem(
                                onClick = { },
                                shapes = ListItemDefaults.segmentedShapes(index = 2, count = remindersTotalCount),
                                supportingContent = {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.set_days_with_space, state.notificationDaysBefore),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = stringResource(R.string.set_days, 1),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            var daysBeforeValue by remember(state.notificationDaysBefore) {
                                                mutableFloatStateOf(state.notificationDaysBefore.toFloat())
                                            }
                                            Slider(
                                                value = daysBeforeValue,
                                                onValueChange = { daysBeforeValue = it },
                                                onValueChangeFinished = { onUpdateNotificationDaysBefore(daysBeforeValue.toInt()) },
                                                valueRange = 1f..7f,
                                                steps = 5,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.set_days, 7),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.set_days_before),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        SegmentedListItem(
                            onClick = { onToggleOvulationNotification(!state.ovulationNotificationEnabled) },
                            shapes = if (state.ovulationNotificationEnabled) {
                                ListItemDefaults.segmentedShapes(index = ovulationToggleIndex, count = remindersTotalCount)
                            } else {
                                ListItemDefaults.segmentedShapes(index = ovulationToggleIndex, count = remindersTotalCount)
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.set_ovulation_reminder_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = state.ovulationNotificationEnabled,
                                    onCheckedChange = { onToggleOvulationNotification(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.set_ovulation_reminder),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (state.ovulationNotificationEnabled) {
                            SegmentedListItem(
                                onClick = { },
                                shapes = ListItemDefaults.segmentedShapes(index = ovulationDaysIndex, count = remindersTotalCount),
                                supportingContent = {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.set_days_with_space, state.ovulationNotificationDaysBefore),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = stringResource(R.string.set_days, 1),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            var ovulationDaysValue by remember(state.ovulationNotificationDaysBefore) {
                                                mutableFloatStateOf(state.ovulationNotificationDaysBefore.toFloat())
                                            }
                                            Slider(
                                                value = ovulationDaysValue,
                                                onValueChange = { ovulationDaysValue = it },
                                                onValueChangeFinished = { onUpdateOvulationDaysBefore(ovulationDaysValue.toInt()) },
                                                valueRange = 1f..7f,
                                                steps = 5,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.set_days, 7),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.set_days_before),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        SegmentedListItem(
                            onClick = { onShowTimeDialog() },
                            shapes = ListItemDefaults.segmentedShapes(index = remindersTotalCount - 1, count = remindersTotalCount),
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
                                text = stringResource(R.string.set_reminder_time),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    val state = uiState
    if (showTimeDialog && state is SettingsUiState.Success) {
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
                title = { Text(stringResource(R.string.set_theme)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.set_back)
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
            if (uiState !is SettingsUiState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
                return@Column
            }
            if (uiState is SettingsUiState.Success) {
                val state = uiState
                Text(
                    text = stringResource(R.string.set_theme_preference_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val themeOptions = listOf(
                    Triple(com.example.periodvibe.domain.model.Settings.ThemeMode.LIGHT, stringResource(R.string.set_theme_light), Icons.Default.LightMode),
                    Triple(com.example.periodvibe.domain.model.Settings.ThemeMode.DARK, stringResource(R.string.set_theme_dark), Icons.Default.DarkMode),
                    Triple(com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM, stringResource(R.string.set_theme_system), Icons.Default.PhoneAndroid)
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    themeOptions.forEachIndexed { index, (mode, label, icon) ->
                        ToggleButton(
                            checked = state.themeMode == mode,
                            onCheckedChange = { onUpdateThemeMode(mode) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .semantics { role = Role.RadioButton },
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
                // 动态取色（Material You）仅 Android 12+ 可用，单独一行避免 4 个选项挤在一行放不下
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleButton(
                        checked = state.themeMode == com.example.periodvibe.domain.model.Settings.ThemeMode.DYNAMIC,
                        onCheckedChange = { onUpdateThemeMode(com.example.periodvibe.domain.model.Settings.ThemeMode.DYNAMIC) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .semantics { role = Role.RadioButton }
                    ) {
                        Icon(Icons.Default.Brush, contentDescription = null)
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.set_theme_dynamic))
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
    onNavigateToPinSetup: (com.example.periodvibe.ui.applock.PinSetupMode) -> Unit,
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
        onUpdateAppLockDelay = { viewModel.updateAppLockDelay(it) },
        onVerifyCurrentPin = { viewModel.verifyCurrentPin(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrivacyContent(
    uiState: SettingsUiState,
    showDisableAppLockDialog: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPinSetup: (com.example.periodvibe.ui.applock.PinSetupMode) -> Unit,
    onTogglePrivacyMode: (Boolean) -> Unit,
    onShowDisableAppLockDialog: () -> Unit,
    onHideDisableAppLockDialog: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onUpdateAppLockDelay: (Int) -> Unit,
    onVerifyCurrentPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.set_privacy)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.set_back)
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
            if (uiState !is SettingsUiState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
                return@Column
            }
            if (uiState is SettingsUiState.Success) {
                val state = uiState

                // 分段列表的实际条目数与索引随应用锁开关动态变化
                val totalCount = 2 + (if (state.appLockEnabled) 2 else 0)
                val privacyModeIndex = if (state.appLockEnabled) 2 else 1
                val changePinIndex = if (state.appLockEnabled) 3 else -1

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    SegmentedListItem(
                        onClick = {
                            if (state.appLockEnabled) {
                                onShowDisableAppLockDialog()
                            } else {
                                onNavigateToPinSetup(com.example.periodvibe.ui.applock.PinSetupMode.SETUP)
                            }
                        },
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = totalCount),
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.set_app_lock_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.appLockEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        onNavigateToPinSetup(com.example.periodvibe.ui.applock.PinSetupMode.SETUP)
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
                            text = stringResource(R.string.set_app_lock),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 自动锁定时机（仅应用锁开启时显示）
                    if (state.appLockEnabled) {
                        SegmentedListItem(
                            onClick = { },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = totalCount),
                            supportingContent = {
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val delayOptions = listOf(
                                        0 to stringResource(R.string.set_app_lock_delay_immediate),
                                        1 to stringResource(R.string.set_app_lock_delay_1min),
                                        5 to stringResource(R.string.set_app_lock_delay_5min)
                                    )
                                    delayOptions.forEachIndexed { index, (minutes, label) ->
                                        SegmentedButton(
                                            selected = state.appLockDelayMinutes == minutes,
                                            onClick = { onUpdateAppLockDelay(minutes) },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = delayOptions.size
                                            )
                                        ) {
                                            Text(label)
                                        }
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.set_app_lock_delay),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    SegmentedListItem(
                        onClick = { onTogglePrivacyMode(!state.privacyModeEnabled) },
                        shapes = ListItemDefaults.segmentedShapes(index = privacyModeIndex, count = totalCount),
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.set_privacy_mode_description),
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
                            text = stringResource(R.string.set_privacy_mode),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 修改 PIN（仅应用锁开启时显示）
                    if (state.appLockEnabled) {
                        SegmentedListItem(
                            onClick = { onNavigateToPinSetup(com.example.periodvibe.ui.applock.PinSetupMode.CHANGE) },
                            shapes = ListItemDefaults.segmentedShapes(index = changePinIndex, count = totalCount),
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
                                text = stringResource(R.string.set_change_pin),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    var showVerifyPinDialog by remember { mutableStateOf(false) }

    if (showDisableAppLockDialog) {
        DisableAppLockConfirmationDialog(
            onDismiss = { onHideDisableAppLockDialog() },
            onConfirm = {
                onHideDisableAppLockDialog()
                // 关闭应用锁前需验证当前 PIN
                showVerifyPinDialog = true
            }
        )
    }

    if (showVerifyPinDialog) {
        VerifyPinDialog(
            verify = onVerifyCurrentPin,
            onVerified = {
                showVerifyPinDialog = false
                onToggleAppLock(false)
            },
            onDismiss = { showVerifyPinDialog = false }
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
    val context = LocalContext.current
    val showClearDataConfirmationDialog by viewModel.showClearDataConfirmationDialog.collectAsState()
    val showImportConfirmationDialog by viewModel.showImportConfirmationDialog.collectAsState()
    val showImportResultDialog by viewModel.showImportResultDialog.collectAsState()
    val showExportResultDialog by viewModel.showExportResultDialog.collectAsState()
    val showExportFormatDialog by viewModel.showExportFormatDialog.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val errorMessageRes by viewModel.errorMessage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessageText = errorMessageRes?.let { stringResource(it) }

    // 清除数据失败等操作通过 Snackbar 反馈
    LaunchedEffect(errorMessageText) {
        errorMessageText?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    // 创建导出文件的 launcher
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        // 返回时清除豁免标记（ON_START 已清除，这里作为双保险）
        AppLockGuard.isSystemPickerActive = false
        uri?.let {
            viewModel.exportData(it)
        }
    }

    // 选择导入文件的 launcher
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        // 返回时清除豁免标记（ON_START 已清除，这里作为双保险）
        AppLockGuard.isSystemPickerActive = false
        uri?.let {
            // 获取持久化权限（部分 provider 不支持时忽略，仅影响下次直接读取）
            try {
                val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            viewModel.previewImportData(it)
        }
    }

    // 生成导出文件名
    fun generateExportFileName(): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val timestamp = LocalDateTime.now().format(formatter)
        val extension = viewModel.getSelectedExportFormat().extension
        return "period_vibe_backup_$timestamp.$extension"
    }

    DataManagementContent(
        onNavigateBack = onNavigateBack,
        onExportDataClick = { viewModel.showExportFormatDialog() },
        onImportDataClick = {
            // 豁免自动锁定：系统文件选择器打开期间切回 app 不要求重新解锁
            AppLockGuard.isSystemPickerActive = true
            importFileLauncher.launch(arrayOf("*/*"))
        },
        onClearDataClick = { viewModel.showClearDataConfirmationDialog() },
        isProcessing = isProcessing,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )

    if (showClearDataConfirmationDialog) {
        com.example.periodvibe.ui.settings.components.ClearDataConfirmationDialog(
            onDismiss = { viewModel.hideClearDataConfirmationDialog() },
            onConfirm = { viewModel.clearAllData() }
        )
    }

    if (showImportConfirmationDialog) {
        val (cycleCount, recordCount) = viewModel.getPendingImportDataCount()
        com.example.periodvibe.ui.settings.components.ImportConfirmationDialog(
            cycleCount = cycleCount,
            recordCount = recordCount,
            onDismiss = { viewModel.hideImportConfirmationDialog() },
            onConfirm = { mode ->
                viewModel.setImportMode(mode)
                viewModel.confirmImportData()
            }
        )
    }

    if (showImportResultDialog && importResult != null) {
        val result = importResult
        val (success, message) = when (result) {
            is com.example.periodvibe.data.exportimport.ImportResult.Success -> {
                val baseMessage = stringResource(R.string.dlg_import_success_message, result.cycles.size, result.dailyRecords.size)
                val fullMessage = if (result.warnings.isNotEmpty()) {
                    baseMessage + "\n" + stringResource(R.string.set_import_warnings, result.warnings.joinToString("\n"))
                } else {
                    baseMessage
                }
                Pair(true, fullMessage)
            }
            is com.example.periodvibe.data.exportimport.ImportResult.Failure -> Pair(false, result.errorMessage)
            null -> Pair(false, stringResource(R.string.dlg_unknown_error))
        }
        com.example.periodvibe.ui.settings.components.ImportResultDialog(
            success = success,
            message = message,
            onDismiss = { viewModel.hideImportResultDialog() }
        )
    }

    if (showExportResultDialog && exportResult != null) {
        val (success, message) = exportResult ?: Pair(false, stringResource(R.string.dlg_unknown_error))
        com.example.periodvibe.ui.settings.components.ExportResultDialog(
            success = success,
            message = message,
            onDismiss = { viewModel.hideExportResultDialog() }
        )
    }

    if (showExportFormatDialog) {
        com.example.periodvibe.ui.settings.components.ExportFormatDialog(
            onDismiss = { viewModel.hideExportFormatDialog() },
            onFormatSelected = { format ->
                viewModel.setSelectedExportFormat(format)
                // 豁免自动锁定：系统文件选择器打开期间切回 app 不要求重新解锁
                AppLockGuard.isSystemPickerActive = true
                exportFileLauncher.launch(generateExportFileName())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DataManagementContent(
    onNavigateBack: () -> Unit,
    onExportDataClick: () -> Unit,
    onImportDataClick: () -> Unit,
    onClearDataClick: () -> Unit,
    isProcessing: Boolean = false,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.set_data_management)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.set_back)
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
                    onClick = onExportDataClick,
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.set_export_format_hint),
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
                        text = stringResource(R.string.set_export_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SegmentedListItem(
                    onClick = onImportDataClick,
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.set_import_description),
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
                        text = stringResource(R.string.set_import_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SegmentedListItem(
                    onClick = onClearDataClick,
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.set_clear_data_description),
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
                        text = stringResource(R.string.set_clear_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    }

    // 导入/导出/清除数据进行中的悬浮进度指示
    if (isProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadingIndicator()
                    Text(
                        text = stringResource(R.string.set_processing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                title = { Text(stringResource(R.string.set_about)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.set_back)
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
                // 应用介绍
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
                        text = stringResource(R.string.set_app_intro),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 版本信息
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
                            text = stringResource(R.string.set_version_prefix, versionName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.set_version_info),
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
                appLockDelayMinutes = 0,
                privacyModeEnabled = false,

                periodNotificationEnabled = true,
                ovulationNotificationEnabled = true,
                ovulationNotificationDaysBefore = 1
            ),
            showTimeDialog = false,
            onNavigateBack = { },
            onToggleNotificationEnabled = { },
            onUpdateNotificationDaysBefore = { },
            onShowTimeDialog = { },
            onHideTimeDialog = { },
            onUpdateNotificationTime = { },
            onTogglePeriodNotification = { },
            onToggleOvulationNotification = { },
            onUpdateOvulationDaysBefore = { }
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
                appLockDelayMinutes = 0,
                privacyModeEnabled = false,

                periodNotificationEnabled = true,
                ovulationNotificationEnabled = true,
                ovulationNotificationDaysBefore = 1
            ),
            showTimeDialog = false,
            onNavigateBack = { },
            onToggleNotificationEnabled = { },
            onUpdateNotificationDaysBefore = { },
            onShowTimeDialog = { },
            onHideTimeDialog = { },
            onUpdateNotificationTime = { },
            onTogglePeriodNotification = { },
            onToggleOvulationNotification = { },
            onUpdateOvulationDaysBefore = { }
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
                appLockDelayMinutes = 0,
                privacyModeEnabled = false,

                periodNotificationEnabled = true,
                ovulationNotificationEnabled = true,
                ovulationNotificationDaysBefore = 1
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
                appLockDelayMinutes = 0,
                privacyModeEnabled = false,

                periodNotificationEnabled = true,
                ovulationNotificationEnabled = true,
                ovulationNotificationDaysBefore = 1
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
                appLockDelayMinutes = 0,
                privacyModeEnabled = false,

                periodNotificationEnabled = true,
                ovulationNotificationEnabled = true,
                ovulationNotificationDaysBefore = 1
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
                appLockDelayMinutes = 0,
                privacyModeEnabled = true,

                periodNotificationEnabled = true,
                ovulationNotificationEnabled = true,
                ovulationNotificationDaysBefore = 1
            ),
            showDisableAppLockDialog = false,
            onNavigateBack = { },
            onNavigateToPinSetup = { },
            onTogglePrivacyMode = { },
            onShowDisableAppLockDialog = { },
            onHideDisableAppLockDialog = { },
            onToggleAppLock = { },
            onUpdateAppLockDelay = { },
            onVerifyCurrentPin = { false }
        )
    }
}

@Preview(showBackground = true, name = "数据管理")
@Composable
private fun DataManagementScreenPreview() {
    PeriodVibeTheme {
        DataManagementContent(
            onNavigateBack = { },
            onExportDataClick = { },
            onImportDataClick = { },
            onClearDataClick = { },
            snackbarHostState = remember { SnackbarHostState() }
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
