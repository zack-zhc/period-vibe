package com.example.periodvibe.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.ui.settings.components.CycleParametersSection
import com.example.periodvibe.ui.settings.components.DataManagementSection
import com.example.periodvibe.ui.settings.components.NotificationSettingsSection
import com.example.periodvibe.ui.settings.components.PrivacySettingsSection
import com.example.periodvibe.ui.settings.components.ThemeSettingsSection
import com.example.periodvibe.ui.settings.components.AboutSection
import com.example.periodvibe.ui.settings.components.AboutDialog
import com.example.periodvibe.ui.settings.components.CycleParametersDialog
import com.example.periodvibe.ui.settings.components.DataManagementDialog
import com.example.periodvibe.ui.settings.components.NotificationSettingsDialog
import com.example.periodvibe.ui.settings.components.ThemeSettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCycleDialog by viewModel.showCycleDialog.collectAsState()
    val showNotificationDialog by viewModel.showNotificationDialog.collectAsState()
    val showDataManagementDialog by viewModel.showDataManagementDialog.collectAsState()
    val showThemeDialog by viewModel.showThemeDialog.collectAsState()
    val showPrivacyDialog by viewModel.showPrivacyDialog.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("设置") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState is SettingsUiState.Success) {
                val state = uiState as SettingsUiState.Success

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
                    onClick = { viewModel.showNotificationDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThemeSettingsSection(
                    themeMode = state.themeMode,
                    onClick = { viewModel.showThemeDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrivacySettingsSection(
                    appLockEnabled = state.appLockEnabled,
                    privacyModeEnabled = state.privacyModeEnabled,
                    onAppLockToggle = { viewModel.toggleAppLock(it) },
                    onPrivacyModeToggle = { viewModel.togglePrivacyMode(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DataManagementSection(
                    onClick = { viewModel.showDataManagementDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AboutSection(
                    onClick = { viewModel.showAboutDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))
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

    if (showNotificationDialog && uiState is SettingsUiState.Success) {
        val state = uiState as SettingsUiState.Success
        NotificationSettingsDialog(
            enabled = state.notificationEnabled,
            daysBefore = state.notificationDaysBefore,
            time = state.notificationTime,
            onDismiss = { viewModel.hideNotificationDialog() },
            onConfirm = { enabled, daysBefore, time ->
                viewModel.updateNotificationSettings(enabled, daysBefore, time)
            }
        )
    }

    if (showDataManagementDialog) {
        DataManagementDialog(
            onDismiss = { viewModel.hideDataManagementDialog() },
            onClearData = { viewModel.clearAllData() }
        )
    }

    if (showThemeDialog && uiState is SettingsUiState.Success) {
        val state = uiState as SettingsUiState.Success
        ThemeSettingsDialog(
            currentThemeMode = state.themeMode,
            onDismiss = { viewModel.hideThemeDialog() },
            onConfirm = { mode -> viewModel.updateThemeMode(mode) }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.hideAboutDialog() }
        )
    }
}
