package com.example.periodvibe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer

/**
 * 创建可保存的多顶部级返回栈导航状态。
 *
 * 当前顶部级路由与每个顶部级返回栈均通过 saveable 机制持久化
 * （rememberSerializable / rememberNavBackStack）：
 * 配置变更与进程死亡后，导航位置与各 tab 的栈内容可完整恢复。
 *
 * @param startKey 起始（退出）顶部级路由，通常是 [Screen.Home]
 */
@Composable
fun rememberPeriodVibeNavState(
    startKey: Screen = Screen.Home
): TopLevelBackStack<Screen> {
    // 当前顶部级路由：可保存（用 rememberSerializable，而非 rememberSaveable）
    val topLevelKey = rememberSerializable(
        startKey, TopLevelScreens.toSet()
    ) { mutableStateOf(startKey) }

    // 每个顶部级路由一个可保存的返回栈。
    // Home 栈初始放入 Loading 占位：首次启动由导航逻辑 replaceWith 到目标页；
    // 若存在上次会话的已保存状态，则恢复后直接使用（Loading 不会出现）。
    val backStacks = TopLevelScreens.associateWith { key ->
        if (key == Screen.Home) {
            rememberNavBackStack<Screen>(Screen.Loading)
        } else {
            rememberNavBackStack(key)
        }
    }

    return remember(startKey, TopLevelScreens) {
        TopLevelBackStack(
            startKey = startKey,
            topLevelKey = topLevelKey,
            backStacks = backStacks
        )
    }
}

// NavKey 子类型的泛型包装（官方 conditional recipe 的写法）：
// 库提供的 Android 重载返回 NavBackStack<NavKey>，这里保持具体子类型，
// 参见 https://issuetracker.google.com/issues/463382671
@Composable
fun <T : NavKey> rememberNavBackStack(vararg elements: T): NavBackStack<T> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) { NavBackStack(*elements) }
}

/**
 * 管理多个顶部级返回栈的导航状态
 *
 * 每个顶部级路由（底部导航栏项）都有自己独立的返回栈，
 * 切换标签时保留各自的导航历史。
 *
 * 组合语义与官方 multiple-backstacks 模式一致：只有"起始栈 + 当前栈"
 * （[stacksInUse]）的条目会被 NavDisplay 组合，其余栈的状态由 NavDisplay
 * 内部的 SaveableStateHolder 保存，切换回时自动恢复。
 *
 * @param startKey 起始（退出）顶部级路由，用户通过它退出应用
 * @param topLevelKey 当前顶部级路由的 [MutableState]
 * @param backStacks 每个顶部级路由对应的返回栈
 */
class TopLevelBackStack<T : NavKey>(
    val startKey: T,
    topLevelKey: MutableState<T>,
    val backStacks: Map<T, NavBackStack<T>>
) {
    var topLevelKey: T by topLevelKey

    /** 当前使用的顶部级路由：起始栈 + 当前栈 */
    private val stacksInUse: List<T>
        get() = if (topLevelKey == startKey) listOf(startKey)
                else listOf(startKey, topLevelKey)

    /**
     * 扁平化的返回栈，供 NavDisplay 使用。
     * 仅包含当前使用的栈；内容变更时 NavDisplay 会响应重组。
     *
     * 起始栈的根元素始终保留（exit-through-home 不变式）：
     * 即使被 [replaceWith] / [restoreCurrentStack] 清空，也会回退为 [startKey]，
     * 保证 NavDisplay 的 backStack 永不为空。
     */
    val backStack: List<T>
        get() = stacksInUse.flatMap { stackKey ->
            val stack = backStacks[stackKey] ?: return@flatMap emptyList<T>()
            if (stack.isEmpty() && stackKey == startKey) listOf(startKey) else stack
        }

    /** 当前顶部级栈的顶部条目（当前页面） */
    val currentDestination: T?
        get() = backStack.lastOrNull()

    /**
     * 切换到指定的顶部级路由。
     *
     * 若目标栈为空（如被 [replaceWith] / [resetTo] / [restoreCurrentStack] 清空过），
     * 则重新放入自身作为根，保证 NavDisplay 始终有内容可渲染。
     */
    fun navigateToTopLevel(key: T) {
        topLevelKey = key
        backStacks[key]?.takeIf { it.isEmpty() }?.add(key)
    }

    /**
     * 导航到子页面（添加到当前顶部级路由的返回栈）
     */
    fun navigateToDetail(key: T) {
        backStacks[topLevelKey]?.add(key)
    }

    /**
     * 返回上一页。
     * 若当前栈只剩顶部级路由，则切回起始栈；若已在起始栈根部则不处理（交给系统退出）。
     * 永不删除最后一个栈，保证 NavDisplay 始终有内容。
     */
    fun goBack() {
        val currentStack = backStacks[topLevelKey] ?: return
        if (currentStack.size > 1) {
            // 当前栈有子页面，只弹出子页面
            currentStack.removeLastOrNull()
        } else if (topLevelKey != startKey) {
            // 位于非起始栈根部：切回起始栈，保留各栈状态
            topLevelKey = startKey
        }
    }

    /**
     * 重置：清空所有栈，仅保留指定页面（非顶部级目标放入起始栈）
     */
    fun resetTo(key: T) {
        backStacks.values.forEach { it.clear() }
        val stack = backStacks[key] ?: backStacks[startKey]
        topLevelKey = if (key in backStacks) key else startKey
        stack?.add(key)
    }

    /**
     * 完全替换：清空所有栈，替换为指定条目（如 AppLock / Onboarding）
     */
    fun replaceWith(vararg keys: T) {
        if (keys.isEmpty()) return
        backStacks.values.forEach { it.clear() }
        val first = keys.first()
        val stack = backStacks[first] ?: backStacks[startKey]
        topLevelKey = if (first in backStacks) first else startKey
        stack?.addAll(keys)
    }

    /**
     * 获取当前顶部级路由的返回栈（含二级页面）
     */
    fun getCurrentStack(): List<T> =
        backStacks[topLevelKey]?.toList() ?: emptyList()

    /**
     * 恢复当前顶部级路由的返回栈（用于解锁后回到锁定前的位置）
     * 首个元素作为顶部级路由，其余作为其二级页面。
     */
    fun restoreCurrentStack(keys: List<T>) {
        if (keys.isEmpty()) return
        backStacks.values.forEach { it.clear() }
        val first = keys.first()
        val stack = backStacks[first] ?: backStacks[startKey]
        topLevelKey = if (first in backStacks) first else startKey
        stack?.addAll(keys)
    }
}
