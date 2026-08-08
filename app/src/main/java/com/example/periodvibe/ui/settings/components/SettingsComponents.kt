package com.example.periodvibe.ui.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.periodvibe.R
import com.example.periodvibe.ui.applock.PIN_LENGTH
import com.example.periodvibe.ui.applock.PinDotRow
import com.example.periodvibe.ui.applock.PinKeyboard
import com.example.periodvibe.utils.AppUtils
import java.time.LocalTime

// ======================= Dialogs =======================

@Composable
fun DisableAppLockConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlg_confirm_action)) },
        text = { Text(stringResource(R.string.dlg_disable_app_lock_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.set_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_cancel))
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
    var cycleLengthText by rememberSaveable { mutableStateOf(cycleLength.toString()) }
    var periodLengthText by rememberSaveable { mutableStateOf(periodLength.toString()) }
    var cycleLengthError by rememberSaveable { mutableStateOf(false) }
    var periodLengthError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlg_cycle_parameters_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = cycleLengthText,
                    onValueChange = { value ->
                        // 只保留数字，允许任意中间输入，确认时才做范围校验
                        cycleLengthText = value.filter { it.isDigit() }
                        cycleLengthError = false
                    },
                    label = { Text(stringResource(R.string.dlg_cycle_length_label, cycleLengthRange.toString())) },
                    isError = cycleLengthError,
                    supportingText = if (cycleLengthError) {
                        { Text(stringResource(R.string.dlg_range_input_error, cycleLengthRange.toString())) }
                    } else {
                        null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = periodLengthText,
                    onValueChange = { value ->
                        periodLengthText = value.filter { it.isDigit() }
                        periodLengthError = false
                    },
                    label = { Text(stringResource(R.string.dlg_period_length_label, periodLengthRange.toString())) },
                    isError = periodLengthError,
                    supportingText = if (periodLengthError) {
                        { Text(stringResource(R.string.dlg_range_input_error, periodLengthRange.toString())) }
                    } else {
                        null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cycle = cycleLengthText.toIntOrNull()
                    val period = periodLengthText.toIntOrNull()
                    cycleLengthError = cycle == null || cycle !in cycleLengthRange
                    periodLengthError = period == null || period !in periodLengthRange
                    if (!cycleLengthError && !periodLengthError && cycle != null && period != null) {
                        onConfirm(cycle, period)
                    }
                }
            ) {
                Text(stringResource(R.string.set_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_cancel))
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
    val timePickerState = remember {
        TimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlg_notification_time_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TimePicker(
                    state = timePickerState
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text(stringResource(R.string.set_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_cancel))
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
        title = { Text(stringResource(R.string.dlg_clear_data_title)) },
        text = {
            Text(stringResource(R.string.dlg_clear_data_message))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    stringResource(R.string.dlg_confirm_clear),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_cancel))
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
    onConfirm: (com.example.periodvibe.ui.settings.ImportMode) -> Unit
) {
    var selectedMode by rememberSaveable {
        mutableStateOf(com.example.periodvibe.ui.settings.ImportMode.OVERWRITE)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlg_import_confirm_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.dlg_import_summary, cycleCount, recordCount)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.dlg_import_mode_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val modes = listOf(
                        com.example.periodvibe.ui.settings.ImportMode.OVERWRITE to stringResource(R.string.dlg_import_mode_overwrite),
                        com.example.periodvibe.ui.settings.ImportMode.MERGE to stringResource(R.string.dlg_import_mode_merge)
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
                        com.example.periodvibe.ui.settings.ImportMode.OVERWRITE ->
                            stringResource(R.string.dlg_import_overwrite_hint)
                        com.example.periodvibe.ui.settings.ImportMode.MERGE ->
                            stringResource(R.string.dlg_import_merge_hint)
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
                Text(stringResource(R.string.dlg_confirm_import))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_cancel))
            }
        }
    )
}

@Composable
fun ResultDialog(
    success: Boolean,
    message: String,
    successTitle: String,
    failureTitle: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (success) successTitle else failureTitle) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_confirm))
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
    ResultDialog(
        success = success,
        message = message,
        successTitle = stringResource(R.string.dlg_import_success_title),
        failureTitle = stringResource(R.string.dlg_import_failure_title),
        onDismiss = onDismiss
    )
}

@Composable
fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onFormatSelected: (com.example.periodvibe.data.exportimport.ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlg_export_format_title)) },
        text = {
            Column {
                TextButton(
                    onClick = { onFormatSelected(com.example.periodvibe.data.exportimport.ExportFormat.JSON) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dlg_export_format_json), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.dlg_export_format_json_hint),
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
                        Text(stringResource(R.string.dlg_export_format_csv), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.dlg_export_format_csv_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_cancel))
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
    ResultDialog(
        success = success,
        message = message,
        successTitle = stringResource(R.string.dlg_export_success_title),
        failureTitle = stringResource(R.string.dlg_export_failure_title),
        onDismiss = onDismiss
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember { AppUtils.getVersionName(context) }
    // 真实应用图标（自适应图标），与系统"应用信息"页一致的观感
    val appIcon = remember {
        context.packageManager.getApplicationIcon(context.packageName)
            .toBitmap()
            .asImageBitmap()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    bitmap = appIcon,
                    // 图标旁紧邻应用名文本，属装饰性元素，避免无障碍重复朗读
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.set_version_format, versionName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.set_about_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AboutFeatureItem(
                    icon = Icons.Rounded.CalendarMonth,
                    text = stringResource(R.string.set_feature_cycle_prediction)
                )
                AboutFeatureItem(
                    icon = Icons.Rounded.Notifications,
                    text = stringResource(R.string.set_feature_smart_reminders)
                )
                AboutFeatureItem(
                    icon = Icons.Rounded.Lock,
                    text = stringResource(R.string.set_feature_privacy)
                )
                AboutFeatureItem(
                    icon = Icons.Rounded.FileDownload,
                    text = stringResource(R.string.set_feature_data_export)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.set_close))
            }
        }
    )
}

@Composable
private fun AboutFeatureItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ======================= 验证 PIN 对话框 =======================

/**
 * 验证当前 PIN（关闭应用锁场景）。验证通过回调 [onVerified]，失败提示并清空可重试。
 */
@Composable
fun VerifyPinDialog(
    verify: (String) -> Boolean,
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val wrongPinText = stringResource(R.string.applock_wrong_pin)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.set_verify_pin_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.set_verify_pin_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                PinDotRow(entered = pin.length)
                Box(modifier = Modifier.height(20.dp)) {
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                PinKeyboard(
                    onNumberClick = { digit ->
                        if (pin.length < PIN_LENGTH) {
                            pin += digit
                            if (pin.length == PIN_LENGTH) {
                                if (verify(pin)) {
                                    onVerified()
                                } else {
                                    error = wrongPinText
                                    pin = ""
                                }
                            }
                        }
                    },
                    onBackspaceClick = { pin = pin.dropLast(1) }
                )
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.set_cancel))
                }
            }
        }
    }
}

// ======================= Preview =======================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "应用介绍弹窗 - 原生样式", apiLevel = 34)
@Composable
private fun AboutDialogPreview() {
    com.example.periodvibe.ui.theme.PeriodVibeTheme {
        AboutDialog(onDismiss = { })
    }
}
