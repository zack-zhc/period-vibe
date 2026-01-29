package com.example.periodvibe.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime

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
fun CycleParametersSection(
    autoCalculateCycle: Boolean,
    cycleLengthDefault: Int,
    periodLengthDefault: Int,
    cycleLengthRange: IntRange,
    periodLengthRange: IntRange,
    onClick: () -> Unit,
    onAutoCalculateToggle: (Boolean) -> Unit
) {
    SettingsSection(title = "周期参数") {
        SettingItemWithSwitch(
            label = "自动计算周期",
            description = if (autoCalculateCycle) "根据历史数据自动计算" else "使用手动设置的值",
            checked = autoCalculateCycle,
            onCheckedChange = onAutoCalculateToggle
        )
        if (!autoCalculateCycle) {
            SettingItem(
                label = "平均周期长度",
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
fun NotificationSettingsSection(
    enabled: Boolean,
    daysBefore: Int,
    time: LocalTime,
    onDaysBeforeClick: () -> Unit,
    onTimeClick: () -> Unit,
    onEnabledToggle: (Boolean) -> Unit
) {
    SettingsSection(title = "提醒设置") {
        SettingItemWithSwitch(
            label = "经期提醒",
            description = if (enabled) "在经期前提醒您" else "关闭所有提醒",
            checked = enabled,
            onCheckedChange = onEnabledToggle
        )
        if (enabled) {
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

@Composable
fun ThemeSettingsDialog(
    currentThemeMode: com.example.periodvibe.domain.model.Settings.ThemeMode,
    onDismiss: () -> Unit,
    onConfirm: (com.example.periodvibe.domain.model.Settings.ThemeMode) -> Unit
) {
    var selectedTheme by remember {
        mutableIntStateOf(
            when (currentThemeMode) {
                com.example.periodvibe.domain.model.Settings.ThemeMode.LIGHT -> 0
                com.example.periodvibe.domain.model.Settings.ThemeMode.DARK -> 1
                com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM -> 2
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("主题设置") },
        text = {
            Column {
                ThemeOption(
                    text = "浅色模式",
                    selected = selectedTheme == 0,
                    onClick = { selectedTheme = 0 },
                    icon = Icons.Default.LightMode
                )
                ThemeOption(
                    text = "深色模式",
                    selected = selectedTheme == 1,
                    onClick = { selectedTheme = 1 },
                    icon = Icons.Default.DarkMode
                )
                ThemeOption(
                    text = "跟随系统",
                    selected = selectedTheme == 2,
                    onClick = { selectedTheme = 2 },
                    icon = Icons.Default.SettingsBrightness
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val theme = when (selectedTheme) {
                    0 -> com.example.periodvibe.domain.model.Settings.ThemeMode.LIGHT
                    1 -> com.example.periodvibe.domain.model.Settings.ThemeMode.DARK
                    else -> com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM
                }
                onConfirm(theme)
            }) {
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
fun DaysBeforeDialog(
    daysBefore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var daysBeforeValue by remember { mutableIntStateOf(daysBefore) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提前天数设置") },
        text = {
            Column {
                OutlinedTextField(
                    value = daysBeforeValue.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let {
                            if (it in 1..7) {
                                daysBeforeValue = it
                            }
                        }
                    },
                    label = { Text("提前天数 (1-7)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(daysBeforeValue) }) {
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

@Composable
fun ThemeSettingsSection(
    themeMode: com.example.periodvibe.domain.model.Settings.ThemeMode,
    onClick: () -> Unit
) {
    val themeText = when (themeMode) {
        com.example.periodvibe.domain.model.Settings.ThemeMode.LIGHT -> "浅色"
        com.example.periodvibe.domain.model.Settings.ThemeMode.DARK -> "深色"
        com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM -> "跟随系统"
    }

    SettingsSection(
        title = "主题设置",
        onClick = onClick
    ) {
        SettingItem(
            label = "主题模式",
            value = themeText,
            showChevron = true
        )
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
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
    SettingsSection(title = "隐私设置") {
        SettingItemWithSwitch(
            label = "应用锁",
            description = "使用指纹或密码保护应用",
            checked = appLockEnabled,
            onCheckedChange = onAppLockToggle
        )
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
    onClick: () -> Unit
) {
    SettingsSection(
        title = "数据管理",
        onClick = onClick
    ) {
        SettingItem(
            label = "导出数据",
            value = "CSV格式",
            showChevron = true
        )
        SettingItem(
            label = "导入数据",
            value = "从备份恢复",
            showChevron = true
        )
        SettingItem(
            label = "清除数据",
            value = "删除所有记录",
            showChevron = true,
            valueColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun DataManagementDialog(
    onDismiss: () -> Unit,
    onClearData: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据管理") },
        text = {
            Column {
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导出数据")
                }
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导入数据")
                }
                TextButton(
                    onClick = onClearData,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "清除数据",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun AboutSection(
    onAppIntroClick: () -> Unit,
    onDeveloperOptionsClick: () -> Unit
) {
    var clickCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var firstClickTime by androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    SettingsSection(title = "关于") {
        SettingItem(
            label = "应用介绍",
            value = "",
            showChevron = true,
            onClick = onAppIntroClick
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

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                    text = "版本 1.0.0",
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

@Composable
fun SettingsSection(
    title: String,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            onClick = onClick ?: {},
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(
    label: String,
    value: String,
    showChevron: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (onClick != null) {
                    modifier.clickable(onClick = onClick)
                } else {
                    modifier
                }
            }
            .padding(16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
