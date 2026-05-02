package com.example.periodvibe.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * 管理多个顶部级返回栈的导航状态
 *
 * 每个顶部级路由（底部导航栏项）都有自己独立的返回栈，
 * 切换标签时会保留各自的导航历史。
 *
 * @param startKey 初始顶部级路由
 */
class TopLevelBackStack<T : Any>(startKey: T) {

    /**
     * 为每个顶部级路由维护独立的返回栈
     * 使用 LinkedHashMap 保持顺序，最后一个是当前活跃的
     */
    private val topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    /**
     * 当前活跃的顶部级路由
     */
    var topLevelKey by mutableStateOf(startKey)
        private set

    /**
     * 扁平化的返回栈，供 NavDisplay 使用
     * 包含所有活跃的顶部级栈的内容
     */
    val backStack = mutableStateListOf(startKey)

    /**
     * 更新扁平化的返回栈
     */
    private fun updateBackStack() {
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }
    }

    /**
     * 切换到指定的顶部级路由
     * 如果该路由不存在，则创建新的返回栈
     * 如果已存在，则将其移到栈顶（激活）
     */
    fun navigateToTopLevel(key: T, resetStack: Boolean = false) {
        if (topLevelStacks[key] == null || resetStack) {
            // 新的顶部级路由，或需要重置栈，创建干净的返回栈
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            // 已存在的顶部级路由，移到最后（激活状态）
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    /**
     * 导航到子页面（添加到当前顶部级路由的返回栈）
     */
    fun navigateToDetail(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    /**
     * 返回上一页
     * 如果当前栈只剩顶部级路由，则移除该顶部级栈并返回到前一个顶部级路由
     */
    fun goBack() {
        val currentStack = topLevelStacks[topLevelKey] ?: return

        if (currentStack.size > 1) {
            // 当前栈有子页面，只弹出子页面
            currentStack.removeLastOrNull()
        } else {
            // 当前栈只有顶部级路由，移除整个顶部级栈
            topLevelStacks.remove(topLevelKey)
            // 切换到前一个顶部级路由
            topLevelStacks.keys.lastOrNull()?.let {
                topLevelKey = it
            }
        }
        updateBackStack()
    }

    /**
     * 重置到指定的顶部级路由（清空所有其他栈）
     */
    fun resetTo(key: T) {
        topLevelStacks.clear()
        topLevelStacks[key] = mutableStateListOf(key)
        topLevelKey = key
        updateBackStack()
    }

    /**
     * 完全替换返回栈（用于初始导航）
     */
    fun replaceWith(vararg keys: T) {
        topLevelStacks.clear()
        if (keys.isNotEmpty()) {
            val firstKey = keys.first()
            topLevelStacks[firstKey] = mutableStateListOf(*keys)
            topLevelKey = firstKey
        }
        updateBackStack()
    }

    /**
     * 获取当前顶部级路由的返回栈大小
     */
    val currentStackSize: Int
        get() = topLevelStacks[topLevelKey]?.size ?: 0

    /**
     * 导航到指定的顶部级路由并重置其栈，同时保留其他标签的状态
     */
    fun navigateToTopLevelAndReset(key: T) {
        // 重置目标标签的栈
        topLevelStacks[key] = mutableStateListOf(key)
        // 将目标标签移到顶部
        topLevelStacks.apply {
            remove(key)?.let {
                put(key, it)
            }
        }
        topLevelKey = key
        updateBackStack()
    }
}
