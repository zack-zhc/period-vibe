package com.example.periodvibe.ui.setup

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InitialSetupViewModel = hiltViewModel()
) {
    val setupData by viewModel.setupData.collectAsState()
    val onCompleteEvent by viewModel.onComplete.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                return date.isBefore(LocalDate.now()) || date == LocalDate.now()
            }
        }
    )

    LaunchedEffect(onCompleteEvent) {
        if (onCompleteEvent) {
            onComplete()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateLastPeriodStartDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCE4EC))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "初始设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "请填写以下信息，帮助我们更好地为您服务",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF616161),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        SetupCard {
            DatePickerField(
                label = "上次经期开始日期（可选）",
                selectedDate = setupData.lastPeriodStartDate,
                onClick = { showDatePicker = true },
                onClear = { viewModel.clearLastPeriodStartDate() }
            )

            if (setupData.lastPeriodStartDate == null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "暂不选择日期，您可以稍后在首页随时记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NumberInputField(
                label = "平均周期长度（天）",
                value = setupData.cycleLength,
                onValueChange = viewModel::updateCycleLength,
                range = 21..35,
                hint = "请输入21-35之间的数字"
            )

            Spacer(modifier = Modifier.height(24.dp))

            NumberInputField(
                label = "平均经期天数（天）",
                value = setupData.periodLength,
                onValueChange = viewModel::updatePeriodLength,
                range = 3..7,
                hint = "请输入3-7之间的数字"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = viewModel::onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = viewModel.isValid(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.isValid()) Color(0xFFE91E63) else Color(0xFFBDBDBD),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "完成",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SetupCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        content()
    }
}

@Composable
private fun DatePickerField(
    label: String,
    selectedDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3436)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedDate?.format(dateFormatter) ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "点击选择日期（可选）",
                    color = Color(0xFFB2BEC3)
                )
            },
            trailingIcon = {
                Row {
                    if (selectedDate != null && onClear != null) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "清除日期",
                                tint = Color(0xFF757575)
                            )
                        }
                    }
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期",
                            tint = Color(0xFFE91E63)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun NumberInputField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    hint: String,
    modifier: Modifier = Modifier
) {
    var textValue by remember { mutableStateOf(value.toString()) }
    var isError by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3436)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                newValue.toIntOrNull()?.let { intValue ->
                    if (intValue in range) {
                        isError = false
                        onValueChange(intValue)
                    } else {
                        isError = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = {
                Text(
                    text = hint,
                    color = Color(0xFFB2BEC3)
                )
            },
            isError = isError,
            supportingText = if (isError) {
                @Composable {
                    Text(
                        text = "请输入${range.first}-${range.last}之间的数字",
                        color = Color(0xFFE91E63)
                    )
                }
            } else null,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
