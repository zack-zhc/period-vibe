package com.example.periodvibe.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun CycleParametersSection(
    cycleLengthDefault: Int,
    periodLengthDefault: Int,
    cycleLengthRange: IntRange,
    periodLengthRange: IntRange,
    onClick: () -> Unit
) {
    SettingsSection(
        title = "周期参数",
        onClick = onClick
    ) {
        SettingItem(
            label = "平均周期长度",
            value = "$cycleLengthDefault 天",
            showChevron = true
        )
        SettingItem(
            label = "平均经期天数",
            value = "$periodLengthDefault 天",
            showChevron = true
        )
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

@Composable
fun NotificationSettingsSection(
    enabled: Boolean,
    daysBefore: Int,
    time: LocalTime,
    onClick: () -> Unit
) {
    SettingsSection(
        title = "提醒设置",
        onClick = onClick
    ) {
        SettingItem(
            label = "经期提醒",
            value = if (enabled) "开启" else "关闭",
            showChevron = true
        )
        SettingItem(
            label = "提前天数",
            value = "$daysBefore 天",
            showChevron = true
        )
        SettingItem(
            label = "提醒时间",
            value = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
            showChevron = true
        )
    }
}

@Composable
fun NotificationSettingsDialog(
    enabled: Boolean,
    daysBefore: Int,
    time: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Int, LocalTime) -> Unit
) {
    var enabledValue by remember { mutableIntStateOf(if (enabled) 1 else 0) }
    var daysBeforeValue by remember { mutableIntStateOf(daysBefore) }
    var timeValue by remember { mutableIntStateOf(time.hour * 60 + time.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提醒设置") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "启用提醒",
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.Switch(
                        checked = enabledValue == 1,
                        onCheckedChange = { enabledValue = if (it) 1 else 0 }
                    )
                }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = enabledValue == 1
                )

                OutlinedTextField(
                    value = "${(timeValue / 60).toString().padStart(2, '0')}:${(timeValue % 60).toString().padStart(2, '0')}",
                    onValueChange = { value ->
                        val parts = value.split(":")
                        if (parts.size == 2) {
                            val hour = parts[0].toIntOrNull()
                            val minute = parts[1].toIntOrNull()
                            if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
                                timeValue = hour * 60 + minute
                            }
                        }
                    },
                    label = { Text("提醒时间 (HH:MM)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = enabledValue == 1
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    enabledValue == 1,
                    daysBeforeValue,
                    LocalTime.of(timeValue / 60, timeValue % 60)
                )
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
    onClick: () -> Unit
) {
    SettingsSection(
        title = "关于",
        onClick = onClick
    ) {
        SettingItem(
            label = "版本信息",
            value = "v1.0.0",
            showChevron = true
        )
        SettingItem(
            label = "隐私政策",
            value = "",
            showChevron = true
        )
        SettingItem(
            label = "用户协议",
            value = "",
            showChevron = true
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "关于",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
