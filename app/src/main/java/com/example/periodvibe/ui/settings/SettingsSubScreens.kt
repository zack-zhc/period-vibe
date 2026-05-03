package com.example.periodvibe.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.ui.settings.components.AboutDialog
import com.example.periodvibe.ui.settings.components.ClearDataConfirmationDialog
import com.example.periodvibe.ui.settings.components.CycleParametersDialog
import com.example.periodvibe.ui.settings.components.DaysBeforeDialog
import com.example.periodvibe.ui.settings.components.DisableAppLockConfirmationDialog
import com.example.periodvibe.ui.settings.components.ExportFormatDialog
import com.example.periodvibe.ui.settings.components.ExportResultDialog
import com.example.periodvibe.ui.settings.components.ImportConfirmationDialog
import com.example.periodvibe.ui.settings.components.ImportResultDialog
import com.example.periodvibe.ui.settings.components.NotificationTimeDialog
import com.example.periodvibe.ui.settings.components.SettingItem
import com.example.periodvibe.ui.settings.components.SettingItemWithSwitch
import java.time.LocalDateTime

// ==================== 周期参数设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleParametersScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCycleDialog by viewModel.showCycleDialog.collectAsState()

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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState as SettingsUiState.Success
                SettingItemWithSwitch(
                    label = "自动计算周期",
                    description = if (state.autoCalculateCycle) "根据历史数据自动计算" else "使用手动设置的值",
                    checked = state.autoCalculateCycle,
                    onCheckedChange = { viewModel.toggleAutoCalculateCycle(it) }
                )
                if (!state.autoCalculateCycle) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    SettingItem(
                        label = "平均周期天数",
                        value = "${state.cycleLengthDefault} 天",
                        showChevron = true,
                        onClick = { viewModel.showCycleDialog() }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    SettingItem(
                        label = "平均经期天数",
                        value = "${state.periodLengthDefault} 天",
                        showChevron = true,
                        onClick = { viewModel.showCycleDialog() }
                    )
                }
            }
        }
    }

    if (showCycleDialog && uiState is SettingsUiState.Success) {
        val state = uiState as SettingsUiState.Success
        CycleParametersDialog(
            cycleLength = state.cycleLengthDefault,
            periodLength = state.periodLengthDefault,
            cycleLengthRange = state.cycleLengthRange,
            periodLengthRange = state.periodLengthRange,
            onDismiss = { viewModel.hideCycleDialog() },
            onConfirm = { cycleLength, periodLength ->
                viewModel.updateCycleParameters(cycleLength, periodLength)
            }
        )
    }
}

// ==================== 提醒设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDaysBeforeDialog by viewModel.showDaysBeforeDialog.collectAsState()
    val showTimeDialog by viewModel.showTimeDialog.collectAsState()

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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState as SettingsUiState.Success
                SettingItemWithSwitch(
                    label = "经期提醒",
                    description = if (state.notificationEnabled) "在经期前提醒你" else "关闭所有提醒",
                    checked = state.notificationEnabled,
                    onCheckedChange = { viewModel.toggleNotificationEnabled(it) }
                )
                if (state.notificationEnabled) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    SettingItem(
                        label = "提前天数",
                        value = "${state.notificationDaysBefore} 天",
                        showChevron = true,
                        onClick = { viewModel.showDaysBeforeDialog() }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    SettingItem(
                        label = "提醒时间",
                        value = "${state.notificationTime.hour.toString().padStart(2, '0')}:${state.notificationTime.minute.toString().padStart(2, '0')}",
                        showChevron = true,
                        onClick = { viewModel.showTimeDialog() }
                    )
                }
            }
        }
    }

    if (showDaysBeforeDialog && uiState is SettingsUiState.Success) {
        val state = uiState as SettingsUiState.Success
        DaysBeforeDialog(
            initialDaysBefore = state.notificationDaysBefore,
            onDismiss = { viewModel.hideDaysBeforeDialog() },
            onConfirm = { daysBefore -> viewModel.updateNotificationDaysBefore(daysBefore) }
        )
    }

    if (showTimeDialog && uiState is SettingsUiState.Success) {
        val state = uiState as SettingsUiState.Success
        NotificationTimeDialog(
            time = state.notificationTime,
            onDismiss = { viewModel.hideTimeDialog() },
            onConfirm = { time -> viewModel.updateNotificationTime(time) }
        )
    }
}

// ==================== 主题设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                val state = uiState as SettingsUiState.Success
                Text(
                    text = "选择你的偏好",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val themeOptions = listOf(
                        com.example.periodvibe.domain.model.Settings.ThemeMode.LIGHT to "浅色",
                        com.example.periodvibe.domain.model.Settings.ThemeMode.DARK to "深色",
                        com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM to "系统"
                    )
                    themeOptions.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.updateThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = themeOptions.size
                            )
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 隐私设置页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPinSetup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDisableAppLockDialog by viewModel.showDisableAppLockDialog.collectAsState()

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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is SettingsUiState.Success) {
                val state = uiState as SettingsUiState.Success
                SettingItemWithSwitch(
                    label = "应用锁",
                    description = "使用指纹或密码保护应用",
                    checked = state.appLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            onNavigateToPinSetup()
                        } else {
                            viewModel.showDisableAppLockDialog()
                        }
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SettingItemWithSwitch(
                    label = "隐私模式",
                    description = "隐藏通知内容",
                    checked = state.privacyModeEnabled,
                    onCheckedChange = { viewModel.togglePrivacyMode(it) }
                )
            }
        }
    }

    if (showDisableAppLockDialog) {
        DisableAppLockConfirmationDialog(
            onDismiss = { viewModel.hideDisableAppLockDialog() },
            onConfirm = { viewModel.toggleAppLock(false) }
        )
    }
}

// ==================== 数据管理页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showClearDataConfirmationDialog by viewModel.showClearDataConfirmationDialog.collectAsState()
    val showImportConfirmationDialog by viewModel.showImportConfirmationDialog.collectAsState()
    val showImportResultDialog by viewModel.showImportResultDialog.collectAsState()
    val showExportResultDialog by viewModel.showExportResultDialog.collectAsState()
    val showExportFormatDialog by viewModel.showExportFormatDialog.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it)
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.previewImportData(it)
        }
    }

    fun generateExportFileName(): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val timestamp = LocalDateTime.now().format(formatter)
        val extension = viewModel.getSelectedExportFormat().extension
        return "period_vibe_backup_$timestamp.$extension"
    }

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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingItem(
                label = "导出数据",
                value = "选择格式",
                showChevron = true,
                onClick = { viewModel.showExportFormatDialog() }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            SettingItem(
                label = "导入数据",
                value = "从备份恢复",
                showChevron = true,
                onClick = { importFileLauncher.launch(arrayOf("*/*")) }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            SettingItem(
                label = "清除数据",
                value = "删除所有记录",
                showChevron = true,
                onClick = { viewModel.showClearDataConfirmationDialog() }
            )
        }
    }

    if (showClearDataConfirmationDialog) {
        ClearDataConfirmationDialog(
            onDismiss = { viewModel.hideClearDataConfirmationDialog() },
            onConfirm = { viewModel.clearAllData() }
        )
    }

    if (showImportConfirmationDialog) {
        val (cycleCount, recordCount) = viewModel.getPendingImportDataCount()
        ImportConfirmationDialog(
            cycleCount = cycleCount,
            recordCount = recordCount,
            onDismiss = { viewModel.hideImportConfirmationDialog() },
            onConfirm = { viewModel.confirmImportData() }
        )
    }

    if (showImportResultDialog && importResult != null) {
        val result = importResult
        val (success, message) = when (result) {
            is com.example.periodvibe.data.exportimport.ImportResult.Success ->
                Pair(true, "成功导入 ${result.cycles.size} 个周期记录和 ${result.dailyRecords.size} 条日常记录")
            is com.example.periodvibe.data.exportimport.ImportResult.Failure ->
                Pair(false, result.errorMessage)
            else -> Pair(false, "未知错误")
        }
        ImportResultDialog(
            success = success,
            message = message,
            onDismiss = { viewModel.hideImportResultDialog() }
        )
    }

    val exportResultValue = exportResult
    if (showExportResultDialog && exportResultValue != null) {
        val (success, message) = exportResultValue
        ExportResultDialog(
            success = success,
            message = message,
            onDismiss = { viewModel.hideExportResultDialog() }
        )
    }

    if (showExportFormatDialog) {
        ExportFormatDialog(
            onDismiss = { viewModel.hideExportFormatDialog() },
            onFormatSelected = { format ->
                viewModel.setSelectedExportFormat(format)
                exportFileLauncher.launch(generateExportFileName())
            }
        )
    }
}

// ==================== 关于页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingItem(
                label = "应用介绍",
                value = "",
                showChevron = true,
                onClick = { showDialog = true }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            SettingItem(
                label = "版本信息",
                value = "v1.0.0",
                showChevron = false,
                onClick = {
                    val currentTime = System.currentTimeMillis()

                    if (firstClickTime == 0L) {
                        firstClickTime = currentTime
                        clickCount = 1
                    } else {
                        val elapsedTime = currentTime - firstClickTime
                        if (elapsedTime > 8000) {                         clickCount = 1
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
                }
            )
        }
    }

    if (showDialog) {
        AboutDialog(
            onDismiss = { showDialog = false }
        )
    }
}
