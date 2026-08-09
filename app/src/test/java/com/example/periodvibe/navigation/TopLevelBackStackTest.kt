package com.example.periodvibe.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * TopLevelBackStack 纯逻辑测试（不涉及组合）：
 * 验证 tab 切换、返回、锁定/解锁恢复等导航语义。
 */
class TopLevelBackStackTest {

    private fun createState(
        startKey: Screen = Screen.Home
    ): TopLevelBackStack<Screen> {
        val topLevelKey = mutableStateOf(startKey)
        val backStacks = mapOf(
            Screen.Home to NavBackStack<Screen>(Screen.Loading),
            Screen.Calendar to NavBackStack<Screen>(Screen.Calendar),
            Screen.Settings to NavBackStack<Screen>(Screen.Settings),
        )
        return TopLevelBackStack(
            startKey = startKey,
            topLevelKey = topLevelKey,
            backStacks = backStacks
        )
    }

    @Test
    fun `初始为 Loading 占位，替换后进入 Home`() {
        val state = createState()
        assertEquals(listOf(Screen.Loading), state.backStack)
        assertEquals(Screen.Loading, state.currentDestination)

        state.replaceWith(Screen.Home)
        assertEquals(listOf(Screen.Home), state.backStack)
        assertEquals(Screen.Home, state.topLevelKey)
    }

    @Test
    fun `切换 tab 只更新当前栈且保留起始栈`() {
        val state = createState()
        state.replaceWith(Screen.Home)

        state.navigateToTopLevel(Screen.Calendar)
        assertEquals(Screen.Calendar, state.topLevelKey)
        // 起始栈 + 当前栈
        assertEquals(listOf(Screen.Home, Screen.Calendar), state.backStack)

        state.navigateToTopLevel(Screen.Settings)
        assertEquals(listOf(Screen.Home, Screen.Settings), state.backStack)

        state.navigateToTopLevel(Screen.Home)
        assertEquals(listOf(Screen.Home), state.backStack)
    }

    @Test
    fun `清空过其他栈后切换 tab 仍能渲染目标页`() {
        // 回归：replaceWith/restoreCurrentStack 会清空其他栈，
        // 之后切换 tab 时目标栈为空会无法渲染页面（选中态变化但页面不动）
        val state = createState()
        state.replaceWith(Screen.Home)
        state.navigateToTopLevel(Screen.Calendar)
        assertEquals(listOf(Screen.Home, Screen.Calendar), state.backStack)

        state.replaceWith(Screen.AppLock)
        assertEquals(listOf(Screen.AppLock), state.backStack)

        state.restoreCurrentStack(listOf(Screen.Home))
        state.navigateToTopLevel(Screen.Calendar)
        assertEquals(listOf(Screen.Home, Screen.Calendar), state.backStack)
    }

    @Test
    fun `子页面加入当前栈，返回时先弹子页面`() {
        val state = createState()
        state.replaceWith(Screen.Home)
        state.navigateToTopLevel(Screen.Calendar)

        state.navigateToDetail(Screen.History)
        assertEquals(listOf(Screen.Home, Screen.Calendar, Screen.History), state.backStack)

        state.goBack()
        assertEquals(listOf(Screen.Home, Screen.Calendar), state.backStack)

        // 子页面只进入当前 tab 的栈，不影响其他 tab
        state.navigateToTopLevel(Screen.Home)
        state.navigateToDetail(Screen.About)
        assertEquals(listOf(Screen.Home, Screen.About), state.backStack)
    }

    @Test
    fun `非起始栈根部返回切回起始栈，起始栈根部返回不处理`() {
        val state = createState()
        state.replaceWith(Screen.Home)
        state.navigateToTopLevel(Screen.Calendar)
        state.navigateToDetail(Screen.History)

        // 弹出子页面回到 Calendar 根部
        state.goBack()
        assertEquals(Screen.Calendar, state.topLevelKey)
        assertEquals(listOf(Screen.Home, Screen.Calendar), state.backStack)

        // Calendar 根部返回 -> Home
        state.goBack()
        assertEquals(Screen.Home, state.topLevelKey)
        assertEquals(listOf(Screen.Home), state.backStack)

        // Home 根部返回：不处理（交给系统退出），返回栈不为空
        state.goBack()
        assertEquals(Screen.Home, state.topLevelKey)
        assertEquals(listOf(Screen.Home), state.backStack)
    }

    @Test
    fun `锁定与解锁恢复：锁定前栈含二级页面，解锁后恢复`() {
        val state = createState()
        state.replaceWith(Screen.Home)
        state.navigateToTopLevel(Screen.Settings)
        state.navigateToDetail(Screen.Privacy)

        // 锁定：保存当前栈，替换为 AppLock
        val preLockStack = state.getCurrentStack()
        assertEquals(listOf(Screen.Settings, Screen.Privacy), preLockStack)
        state.replaceWith(Screen.AppLock)
        assertEquals(listOf(Screen.AppLock), state.backStack)

        // 解锁：恢复到锁定前的位置
        state.restoreCurrentStack(preLockStack)
        assertEquals(Screen.Settings, state.topLevelKey)
        assertEquals(listOf(Screen.Home, Screen.Settings, Screen.Privacy), state.backStack)
    }

    @Test
    fun `resetTo 清空所有栈只保留目标页`() {
        val state = createState()
        state.replaceWith(Screen.Home)
        state.navigateToTopLevel(Screen.Calendar)
        state.navigateToDetail(Screen.History)

        state.resetTo(Screen.InitialSetup)
        assertEquals(listOf(Screen.InitialSetup), state.backStack)
        assertEquals(Screen.InitialSetup, state.currentDestination)

        state.resetTo(Screen.Home)
        assertEquals(listOf(Screen.Home), state.backStack)
    }

    @Test
    fun `锁定后冷启动：preLock 为 null 时恢复回 Home`() {
        val state = createState()
        state.replaceWith(Screen.AppLock)
        assertEquals(listOf(Screen.AppLock), state.backStack)

        state.restoreCurrentStack(listOf(Screen.Home))
        assertEquals(Screen.Home, state.topLevelKey)
        assertEquals(listOf(Screen.Home), state.backStack)
    }

    @Test
    fun `currentDestination 始终为当前页面`() {
        val state = createState()
        assertNull(state.currentDestination.takeUnless { it == Screen.Loading })

        state.replaceWith(Screen.Home)
        assertEquals(Screen.Home, state.currentDestination)

        state.navigateToTopLevel(Screen.Calendar)
        state.navigateToDetail(Screen.History)
        assertEquals(Screen.History, state.currentDestination)
    }
}
