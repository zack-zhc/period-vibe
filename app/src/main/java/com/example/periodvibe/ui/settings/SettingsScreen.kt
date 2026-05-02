package com.example.periodvibe.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.data.exportimport.ImportResult
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.ui.settings.components.AboutDialog
import com.example.periodvibe.ui.settings.components.AboutSection
import com.example.periodvibe.ui.settings.components.ClearDataConfirmationDialog
import com.example.periodvibe.ui.settings.components.CycleParametersDialog
import com.example.periodvibe.ui.settings.components.CycleParametersSection
import com.example.periodvibe.ui.settings.components.DataManagementSection
import com.example.periodvibe.ui.settings.components.DaysBeforeDialog
import com.example.periodvibe.ui.settings.components.DisableAppLockConfirmationDialog
import com.example.periodvibe.ui.settings.components.ExportFormatDialog
import com.example.periodvibe.ui.settings.components.ExportResultDialog
import com.example.periodvibe.ui.settings.components.ImportConfirmationDialog
import com.example.periodvibe.ui.settings.components.ImportResultDialog
import com.example.periodvibe.ui.settings.components.NotificationSettingsSection
import com.example.periodvibe.ui.settings.components.NotificationTimeDialog
import com.example.periodvibe.ui.settings.components.PrivacySettingsSection
import com.example.periodvibe.ui.settings.components.ThemeSettingsSection
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    onNavigateToPinSetup: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showCycleDialog by viewModel.showCycleDialog.collectAsState()
    val showDisableAppLockDialog by viewModel.showDisableAppLockDialog.collectAsState()
    val showDaysBeforeDialog by viewModel.showDaysBeforeDialog.collectAsState()
    val showTimeDialog by viewModel.showTimeDialog.collectAsState()
    val showPrivacyDialog by viewModel.showPrivacyDialog.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()
    val showClearDataConfirmationDialog by viewModel.showClearDataConfirmationDialog.collectAsState()
    val showImportConfirmationDialog by viewModel.showImportConfirmationDialog.collectAsState()
    val showImportResultDialog by viewModel.showImportResultDialog.collectAsState()
    val showExportResultDialog by viewModel.showExportResultDialog.collectAsState()
    val showExportFormatDialog by viewModel.showExportFormatDialog.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()

    // 创建导出文件的 launcher
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it)
        }
    }

    // 选择导入文件的 launcher
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // 获取持久化权限
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.previewImportData(it)
        }
    }

    // 生成导出文件名
    fun generateExportFileName(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val timestamp = LocalDateTime.now().format(formatter)
        val extension = viewModel.getSelectedExportFormat().extension
        return "period_vibe_backup_$timestamp.$extension"
    }

    when (uiState) {
        is SettingsUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SettingsUiState.Success -> {
            val state = uiState as SettingsUiState.Success
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .let { if (scrollBehavior != null) it.nestedScroll(scrollBehavior.nestedScrollConnection) else it }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                CycleParametersSection(
                    autoCalculateCycle = state.autoCalculateCycle,
                    cycleLengthDefault = state.cycleLengthDefault,
                    periodLengthDefault = state.periodLengthDefault,
                    cycleLengthRange = state.cycleLengthRange,
                    periodLengthRange = state.periodLengthRange,
                    onClick = { viewModel.showCycleDialog() },
                    onAutoCalculateToggle = { viewModel.toggleAutoCalculateCycle(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotificationSettingsSection(
                    enabled = state.notificationEnabled,
                    daysBefore = state.notificationDaysBefore,
                    time = state.notificationTime,
                    onDaysBeforeClick = { viewModel.showDaysBeforeDialog() },
                    onTimeClick = { viewModel.showTimeDialog() },
                    onEnabledToggle = { viewModel.toggleNotificationEnabled(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThemeSettingsSection(
                    themeMode = state.themeMode,
                    onThemeModeChange = { viewModel.updateThemeMode(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrivacySettingsSection(
                    appLockEnabled = state.appLockEnabled,
                    privacyModeEnabled = state.privacyModeEnabled,
                    onAppLockToggle = { enabled ->
                        if (enabled) {
                            onNavigateToPinSetup()
                        } else {
                            viewModel.showDisableAppLockDialog()
                        }
                    },
                    onPrivacyModeToggle = { viewModel.togglePrivacyMode(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DataManagementSection(
                    onExportDataClick = {
                        viewModel.showExportFormatDialog()
                    },
                    onImportDataClick = {
                        // 使用 */* 来显示所有文件，然后在代码中检测类型
                        importFileLauncher.launch(arrayOf("*/*"))
                    },
                    onClearDataClick = { viewModel.showClearDataConfirmationDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AboutSection(
                    onAppIntroClick = { viewModel.showAboutDialog() },
                    onDeveloperOptionsClick = onNavigateToDeveloperOptions
                )

                Spacer(modifier = Modifier.height(80.dp))
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

    if (showDisableAppLockDialog) {
        DisableAppLockConfirmationDialog(
            onDismiss = { viewModel.hideDisableAppLockDialog() },
            onConfirm = { viewModel.toggleAppLock(false) }
        )
    }

    if (showClearDataConfirmationDialog) {
        ClearDataConfirmationDialog(
            onDismiss = { viewModel.hideClearDataConfirmationDialog() },
            onConfirm = { viewModel.clearAllData() }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.hideAboutDialog() }
        )
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
            onConfirm = { time: LocalTime ->
                viewModel.updateNotificationTime(time)
            }
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
            is ImportResult.Success -> Pair(true, "成功导入 ${result.cycles.size} 个周期记录和 ${result.dailyRecords.size} 条日常记录")
            is ImportResult.Failure -> Pair(false, result.errorMessage)
            null -> Pair(false, "未知错误")
        }
        ImportResultDialog(
            success = success,
            message = message,
            onDismiss = { viewModel.hideImportResultDialog() }
        )
    }

    if (showExportResultDialog && exportResult != null) {
        val (success, message) = exportResult ?: Pair(false, "未知错误")
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
