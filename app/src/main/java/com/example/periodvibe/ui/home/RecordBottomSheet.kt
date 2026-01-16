package com.example.periodvibe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.Symptom
import com.example.periodvibe.ui.home.RecordMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordBottomSheet(
    initialDate: LocalDate = LocalDate.now(),
    initialFlowLevel: FlowLevel? = null,
    initialSymptoms: List<Symptom> = emptyList(),
    initialNotes: String? = null,
    recordMode: RecordMode = RecordMode.AUTO,
    hasCurrentCycle: Boolean = false,
    existingRecord: com.example.periodvibe.domain.model.DailyRecord? = null,
    onDismiss: () -> Unit,
    onSave: (flowLevel: FlowLevel?, symptoms: List<Symptom>, notes: String?) -> Unit
) {
    var selectedDate by remember { mutableStateOf(existingRecord?.date ?: initialDate) }
    var flowLevel by remember { mutableStateOf(existingRecord?.flowLevel ?: initialFlowLevel) }
    val selectedSymptoms = remember { mutableStateListOf<Symptom>().apply { addAll(existingRecord?.symptoms ?: initialSymptoms) } }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: initialNotes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(recordMode) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月dd日") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingRecord != null) {
                            "编辑记录"
                        } else when {
                            hasCurrentCycle -> "记录今日状态"
                            selectedMode == RecordMode.NEW_CYCLE -> "开始新周期"
                            selectedMode == RecordMode.SYMPTOM_ONLY -> "记录症状"
                            else -> "选择记录类型"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasCurrentCycle && selectedMode == RecordMode.AUTO) {
                    RecordModeSelector(
                        selectedMode = selectedMode,
                        onModeSelected = { selectedMode = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (!hasCurrentCycle && selectedMode == RecordMode.NEW_CYCLE) {
                    DateSelector(
                        selectedDate = selectedDate,
                        dateFormatter = dateFormatter,
                        onClick = { showDatePicker = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FlowLevelSelector(
                        selectedFlowLevel = flowLevel,
                        onFlowLevelSelected = { flowLevel = it }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SymptomSelector(
                        selectedSymptoms = selectedSymptoms,
                        onSymptomToggle = { symptom ->
                            if (symptom in selectedSymptoms) {
                                selectedSymptoms.remove(symptom)
                            } else {
                                selectedSymptoms.add(symptom)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NotesInput(
                        notes = notes,
                        onNotesChange = { notes = it }
                    )
                } else if (!hasCurrentCycle && selectedMode == RecordMode.SYMPTOM_ONLY) {
                    DateSelector(
                        selectedDate = selectedDate,
                        dateFormatter = dateFormatter,
                        onClick = { showDatePicker = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SymptomSelector(
                        selectedSymptoms = selectedSymptoms,
                        onSymptomToggle = { symptom ->
                            if (symptom in selectedSymptoms) {
                                selectedSymptoms.remove(symptom)
                            } else {
                                selectedSymptoms.add(symptom)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NotesInput(
                        notes = notes,
                        onNotesChange = { notes = it }
                    )
                } else {
                    DateSelector(
                        selectedDate = selectedDate,
                        dateFormatter = dateFormatter,
                        onClick = { showDatePicker = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FlowLevelSelector(
                        selectedFlowLevel = flowLevel,
                        onFlowLevelSelected = { flowLevel = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SymptomSelector(
                        selectedSymptoms = selectedSymptoms,
                        onSymptomToggle = { symptom ->
                            if (symptom in selectedSymptoms) {
                                selectedSymptoms.remove(symptom)
                            } else {
                                selectedSymptoms.add(symptom)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NotesInput(
                        notes = notes,
                        onNotesChange = { notes = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("取消", fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            val finalIsPeriod = when (selectedMode) {
                                RecordMode.NEW_CYCLE -> true
                                RecordMode.SYMPTOM_ONLY -> false
                                RecordMode.AUTO -> true
                            }
                            onSave(
                                if (finalIsPeriod) flowLevel else null,
                                selectedSymptoms.toList(),
                                notes.takeIf { it.isNotBlank() }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val threeMonthsAgo = java.time.Instant.now()
            .minus(java.time.Duration.ofDays(90))
            .toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in threeMonthsAgo..System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
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
}

@Composable
private fun RecordModeSelector(
    selectedMode: RecordMode,
    onModeSelected: (RecordMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "请选择记录类型",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecordModeOption(
                mode = RecordMode.NEW_CYCLE,
                title = "开始新周期",
                description = "记录经期开始",
                isSelected = selectedMode == RecordMode.NEW_CYCLE,
                onClick = { onModeSelected(RecordMode.NEW_CYCLE) },
                modifier = Modifier.weight(1f)
            )

            RecordModeOption(
                mode = RecordMode.SYMPTOM_ONLY,
                title = "记录症状",
                description = "仅记录身体症状",
                isSelected = selectedMode == RecordMode.SYMPTOM_ONLY,
                onClick = { onModeSelected(RecordMode.SYMPTOM_ONLY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecordModeOption(
    mode: RecordMode,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "日期",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedDate.format(dateFormatter),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "选择日期",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun FlowLevelSelector(
    selectedFlowLevel: FlowLevel?,
    onFlowLevelSelected: (FlowLevel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "经量",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowLevel.values().forEach { level ->
                FlowLevelChip(
                    level = level,
                    isSelected = selectedFlowLevel == level,
                    onClick = { onFlowLevelSelected(level) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FlowLevelChip(
    level: FlowLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Color(android.graphics.Color.parseColor(level.color))
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomSelector(
    selectedSymptoms: List<Symptom>,
    onSymptomToggle: (Symptom) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "症状（可选）",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val commonSymptoms = listOf(
                Symptom.ABDOMINAL_PAIN,
                Symptom.LOWER_BACK_PAIN,
                Symptom.BREAST_TENDERNESS,
                Symptom.HEADACHE,
                Symptom.FATIGUE,
                Symptom.MOOD_SWINGS,
                Symptom.BLOATING,
                Symptom.ACNE
            )

            commonSymptoms.forEach { symptom ->
                SymptomChip(
                    symptom = symptom,
                    isSelected = symptom in selectedSymptoms,
                    onClick = { onSymptomToggle(symptom) }
                )
            }
        }
    }
}

@Composable
private fun SymptomChip(
    symptom: Symptom,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = symptom.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Composable
private fun NotesInput(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "备注（可选）",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        androidx.compose.material3.OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "添加备注...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
