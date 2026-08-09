package com.example.periodvibe.ui

import com.example.periodvibe.R
import com.example.periodvibe.data.exportimport.ExportFormat
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.domain.model.FlowLevel

/** 领域模型展示名的资源映射（domain 层保持数据/颜色，文案由 UI 层按语言解析） */

fun CyclePhase.displayNameRes(): Int {
    return when (this) {
        CyclePhase.MENSTRATION -> R.string.phase_menstruation
        CyclePhase.FOLLICULAR -> R.string.phase_follicular
        CyclePhase.OVULATION -> R.string.phase_ovulation
        CyclePhase.LUTEAL -> R.string.phase_luteal
        CyclePhase.FERTILE -> R.string.phase_fertile
        CyclePhase.SAFE -> R.string.phase_safe
    }
}

/** 记录弹窗/日历的经量名（经量少/中/大） */
fun FlowLevel.displayNameRes(): Int {
    return when (this) {
        FlowLevel.LIGHT -> R.string.flow_label_light
        FlowLevel.MEDIUM -> R.string.flow_label_medium
        FlowLevel.HEAVY -> R.string.flow_label_heavy
    }
}

/** 历史记录行的经量名（少量/中等/大量） */
fun FlowLevel.historyDisplayNameRes(): Int {
    return when (this) {
        FlowLevel.LIGHT -> R.string.history_flow_light
        FlowLevel.MEDIUM -> R.string.history_flow_medium
        FlowLevel.HEAVY -> R.string.history_flow_heavy
    }
}

fun ExportFormat.displayNameRes(): Int {
    return when (this) {
        ExportFormat.JSON -> R.string.dlg_export_format_json
        ExportFormat.CSV -> R.string.dlg_export_format_csv
    }
}
