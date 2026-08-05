package com.example.periodvibe.ui.settings

/**
 * 导入模式
 */
enum class ImportMode {
    MERGE,  // 合并模式：跳过已存在的日期
    OVERWRITE  // 覆盖模式：删除所有现有数据后导入
}
