package com.example.periodvibe.domain.model

enum class NotificationType(
    val displayName: String,
    val defaultEnabled: Boolean,
    val description: String
) {
    PERIOD_START("经期开始提醒", true, "经期开始前提醒"),
    PERIOD_END("经期结束提醒", true, "经期结束时提醒"),
    OVULATION("排卵期提醒", true, "排卵期开始前提醒"),
    FERTILE("易孕期提醒", true, "易孕期开始前提醒"),
    DAILY_RECORD("每日记录提醒", true, "每日提醒记录状态"),
    CYCLE_SUMMARY("周期总结", false, "周期结束后发送总结")
}
