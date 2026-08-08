package com.example.periodvibe.domain.model

/**
 * 记录操作模式（领域层语义）：
 * - [AUTO]：自动归属到当前/所在周期，无周期时新建
 * - [NEW_CYCLE]：以该日期开始一个新周期并记录经期
 * - [EDIT]：编辑已有记录（支持修改日期与经量）
 */
enum class RecordMode {
    AUTO,
    NEW_CYCLE,
    EDIT
}
