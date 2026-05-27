package com.example.periodvibe.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.periodvibe.utils.AppUtils
import java.time.LocalTime

// ======================= 基础组件 =======================

@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingItem(
    label: String,
    value: String = "",
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SettingItemWithSwitch(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

// ======================= 业务组件 =======================

@Composable
fun CycleParametersSection(
    autoCalculateCycle: Boolean,
    cycleLengthDefault: Int,
    periodLengthDefault: Int,
    cycleLengthRange: IntRange,
    periodLengthRange: IntRange,
    onClick: () -> Unit,
    onAutoCalculateToggle: (Boolean) -> Unit
) {
    SettingsGroup(title = "周期参数") {
        SettingItemWithSwitch(
            label = "自动计算周期",
            description = if (autoCalculateCycle) "根据历史数据自动计算" else "使用手动设置的值",
            checked = autoCalculateCycle,
            onCheckedChange = onAutoCalculateToggle
        )
        if (!autoCalculateCycle) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(4.dp))
            SettingItem(
                label = "平均周期天数",
                value = "$cycleLengthDefault 天",
                showChevron = true,
                onClick = onClick
            )
            SettingItem(
                label = "平均经期天数",
                value = "$periodLengthDefault 天",
                showChevron = true,
                onClick = onClick
            )
        }
    }
}

@Composable
fun NotificationSettingsSection(
    enabled: Boolean,
    daysBefore: Int,
    time: LocalTime,
    onDaysBeforeClick: () -> Unit,
    onTimeClick: () -> Unit,
    onEnabledToggle: (Boolean) -> Unit
) {
    SettingsGroup(title = "提醒设置") {
        SettingItemWithSwitch(
            label = "经期提醒",
            description = if (enabled) "在经期前提醒你" else "关闭所有提醒",
            checked = enabled,
            onCheckedChange = onEnabledToggle
        )
        if (enabled) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(4.dp))
            SettingItem(
                label = "提前天数",
                value = "$daysBefore 天",
                showChevron = true,
                onClick = onDaysBeforeClick
            )
            SettingItem(
                label = "提醒时间",
                value = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                showChevron = true,
                onClick = onTimeClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsSection(
    themeMode: com.example.periodvibe.domain.model.Settings.ThemeMode,
    onThemeModeChange: (com.example.periodvibe.domain.model.Settings.ThemeMode) -> Unit
) {
    SettingsGroup(title = "主题设置") {
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
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
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

@Composable
fun PrivacySettingsSection(
    appLockEnabled: Boolean,
    privacyModeEnabled: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    onPrivacyModeToggle: (Boolean) -> Unit
) {
    SettingsGroup(title = "隐私设置") {
        SettingItemWithSwitch(
            label = "应用锁",
            description = "使用指纹或密码保护应用",
            checked = appLockEnabled,
            onCheckedChange = onAppLockToggle
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(4.dp))
        SettingItemWithSwitch(
            label = "隐私模式",
            description = "隐藏通知内容",
            checked = privacyModeEnabled,
            onCheckedChange = onPrivacyModeToggle
        )
    }
}

@Composable
fun DataManagementSection(
    onExportDataClick: () -> Unit,
    onImportDataClick: () -> Unit,
    onClearDataClick: () -> Unit
) {
    SettingsGroup(title = "数据管理") {
        SettingItem(
            label = "导出数据",
            value = "选择格式",
            showChevron = true,
            onClick = onExportDataClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(4.dp))
        SettingItem(
            label = "导入数据",
            value = "从备份恢复",
            showChevron = true,
            onClick = onImportDataClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(4.dp))
        SettingItem(
            label = "清除数据",
            value = "删除所有记录",
            showChevron = true,
            onClick = onClearDataClick
        )
    }
}

@Composable
fun AboutSection(
    onAppIntroClick: () -> Unit,
    onDeveloperOptionsClick: () -> Unit
) {
    var clickCount by remember { mutableIntStateOf(0) }
    var firstClickTime by remember { mutableLongStateOf(0L) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember { AppUtils.getVersionName(context) }

    SettingsGroup(title = "关于") {
        SettingItem(
            label = "应用介绍",
            value = "",
            showChevron = true,
            onClick = onAppIntroClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(4.dp))
        SettingItem(
            label = "版本信息",
            value = "v$versionName",
            showChevron = false,
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
                    onDeveloperOptionsClick()
                }
            }
        )
    }
}

// ======================= Dialogs =======================

@Composable
fun DisableAppLockConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认操作") },
        text = { Text("确定要关闭应用锁吗？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun CycleParametersDialog(
    cycleLength: Int,
    periodLength: Int,
    cycleLengthRange: IntRange,
    periodLengthRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var cycleLengthValue by remember { mutableIntStateOf(cycleLength) }
    var periodLengthValue by remember { mutableIntStateOf(periodLength) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("周期参数设置") },
        text = {
            Column {
                OutlinedTextField(
                    value = cycleLengthValue.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let {
                            if (it in cycleLengthRange) {
                                cycleLengthValue = it
                            }
                        }
                    },
                    label = { Text("平均周期长度 ($cycleLengthRange)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = periodLengthValue.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let {
                            if (it in periodLengthRange) {
                                periodLengthValue = it
                            }
                        }
                    },
                    label = { Text("平均经期天数 ($periodLengthRange)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(cycleLengthValue, periodLengthValue) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysBeforeDialog(
    initialDaysBefore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val options = (1..7).map { it }
    var expanded by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableIntStateOf(initialDaysBefore) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提前天数设置") },
        text = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "$selectedDays 天",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("提前天数") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { days ->
                        DropdownMenuItem(
                            text = { Text("$days 天") },
                            onClick = {
                                selectedDays = days
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDays) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTimeDialog(
    time: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = remember { TimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提醒时间设置") },
        text = {
            TimePicker(
                state = timePickerState
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ClearDataConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认清除数据") },
        text = {
            Text("此操作将永久删除所有周期记录和日常数据，且无法恢复。确定要继续吗？")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    "确定清除",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportConfirmationDialog(
    cycleCount: Int,
    recordCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (com.example.periodvibe.ui.settings.SettingsViewModel.ImportMode) -> Unit
) {
    var selectedMode by remember {
        mutableStateOf(com.example.periodvibe.ui.settings.SettingsViewModel.ImportMode.OVERWRITE)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认导入数据") },
        text = {
            Column {
                Text(
                    "即将导入：\n" +
                        "• $cycleCount 个周期记录\n" +
                        "• $recordCount 条日常记录\n"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "导入模式",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val modes = listOf(
                        com.example.periodvibe.ui.settings.SettingsViewModel.ImportMode.OVERWRITE to "覆盖",
                        com.example.periodvibe.ui.settings.SettingsViewModel.ImportMode.MERGE to "合并"
                    )
                    modes.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = modes.size
                            )
                        ) {
                            Text(label)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    when (selectedMode) {
                        com.example.periodvibe.ui.settings.SettingsViewModel.ImportMode.OVERWRITE ->
                            "将删除所有现有数据后导入，请确保已备份。"
                        com.example.periodvibe.ui.settings.SettingsViewModel.ImportMode.MERGE ->
                            "将跳过已存在的日期，只添加新数据。"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedMode) }
            ) {
                Text("确认导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ImportResultDialog(
    success: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (success) "导入成功" else "导入失败") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onFormatSelected: (com.example.periodvibe.data.exportimport.ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择导出格式") },
        text = {
            Column {
                TextButton(
                    onClick = { onFormatSelected(com.example.periodvibe.data.exportimport.ExportFormat.JSON) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("JSON 格式", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "推荐格式，完整保留所有数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = { onFormatSelected(com.example.periodvibe.data.exportimport.ExportFormat.CSV) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("CSV 格式", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "可在 Excel 中打开，便于查看",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ExportResultDialog(
    success: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (success) "导出成功" else "导出失败") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember { AppUtils.getVersionName(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Period Vibe",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "版本 $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Period Vibe 是一款轻量化的生理期记录与管理应用，专注于为女性用户提供简洁、直观、易用的生理周期追踪体验。",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("隐私政策")
                }

                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("用户协议")
                }
            }
        }
    }
}
