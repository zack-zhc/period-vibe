package com.example.periodvibe.util

/**
 * 应用锁豁免守卫：
 * 系统文件选择器（SAF）打开期间不触发自动锁定，返回后停留在原页面，无需重新输入 PIN。
 *
 * 标记仅用于豁免"选择器打开导致的 ON_STOP"，进程被杀后自动重置为 false，无安全残留。
 */
object AppLockGuard {

    @Volatile
    var isSystemPickerActive: Boolean = false

    /**
     * 判断 ON_STOP 时是否需要立即锁定。
     * 文件选择器打开期间（isSystemPickerActive == true）跳过锁定，
     * 避免从文件管理返回时要求重新解锁。
     */
    fun shouldLockOnStop(isUnlocked: Boolean, appLockEnabled: Boolean): Boolean {
        return isUnlocked && appLockEnabled && !isSystemPickerActive
    }
}
