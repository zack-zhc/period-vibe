package com.example.periodvibe.navigation

import android.os.SystemClock
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.ui.applock.AppLockScreen
import com.example.periodvibe.ui.applock.PinSetupMode
import com.example.periodvibe.ui.applock.PinSetupScreen
import com.example.periodvibe.ui.applock.PinSetupViewModel
import com.example.periodvibe.ui.calendar.CalendarScreen
import com.example.periodvibe.ui.calendar.LegendDialog
import com.example.periodvibe.ui.home.HomeScreen
import com.example.periodvibe.ui.home.PeriodBottomNavigation
import com.example.periodvibe.ui.history.HistoryScreen
import com.example.periodvibe.ui.onboarding.OnboardingScreen
import com.example.periodvibe.ui.settings.AboutScreen
import com.example.periodvibe.ui.settings.CycleParametersScreen
import com.example.periodvibe.ui.settings.DataManagementScreen
import com.example.periodvibe.ui.settings.DeveloperOptionsScreen
import com.example.periodvibe.ui.settings.PrivacyScreen
import com.example.periodvibe.ui.settings.RemindersScreen
import com.example.periodvibe.ui.settings.LanguageScreen
import com.example.periodvibe.ui.settings.SettingsScreen
import com.example.periodvibe.ui.settings.ThemeScreen
import com.example.periodvibe.ui.setup.InitialSetupScreen
import com.example.periodvibe.ui.viewmodel.MainViewModel
import com.example.periodvibe.util.AppLockGuard
import kotlinx.coroutines.launch

/**
 * 动画时长（毫秒）
 */
private const val ANIMATION_DURATION = 300

/**
 * 二级页面的滑动动画 metadata
 */
private val SlideInFromRightMetadata = metadata {
    // 进入动画：新页面从右侧滑入，旧页面向左滑出一点
    put(NavDisplay.TransitionKey) {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }

    // 返回动画：旧页面从右侧滑出，新页面从左侧滑入
    put(NavDisplay.PopTransitionKey) {
        slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }

    // 预测性返回动画
    put(NavDisplay.PredictivePopTransitionKey) {
        slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
}

/**
 * 应用锁页面的淡入淡出过渡：锁定淡入锁屏，解锁淡出到正常页面
 */
private val LockTransitionMetadata = metadata {
    // 锁定：锁屏淡入，旧页面淡出
    put(NavDisplay.TransitionKey) {
        fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }

    // 解锁：锁屏淡出，正常页面淡入
    put(NavDisplay.PopTransitionKey) {
        fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }

    // 预测性返回
    put(NavDisplay.PredictivePopTransitionKey) {
        fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
}

/**
 * 创建并记住 TopLevelBackStack 实例（状态可跨配置变更与进程死亡保存）
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PeriodVibeNavHost(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    val settings by mainViewModel.getSettings().collectAsStateWithLifecycle(initialValue = null)

    var themeMode by remember { mutableStateOf(Settings.ThemeMode.SYSTEM) }

    var showPinSetupSheet by remember { mutableStateOf(false) }
    var pinSetupMode by remember { mutableStateOf(PinSetupMode.SETUP) }
    // 锁定发生前的当前返回栈（含二级页面），解锁后恢复到原位置
    var preLockBackStack by remember { mutableStateOf<List<Screen>?>(null) }
    var showLegendDialog by remember { mutableStateOf(false) }
    var showRecordSheetOnHome by remember { mutableStateOf(false) }
    val pinSetupViewModel: PinSetupViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // 使用多返回栈导航状态
    val navState = rememberPeriodVibeNavState()
    val navStateUpdated by androidx.compose.runtime.rememberUpdatedState(navState)
    val showOnboardingUpdated by androidx.compose.runtime.rememberUpdatedState(showOnboarding)
    val settingsUpdated by androidx.compose.runtime.rememberUpdatedState(settings)

    // ==================== 应用锁状态（组合内管理，避免跨组合传播时序问题） ====================
    // 冷启动默认未解锁（false），应用锁开启时进入 AppLock
    var isUnlocked by remember { mutableStateOf(false) }
    // 本次锁定是否由自动锁定（切后台）触发：
    // 自动锁定不自动弹生物识别（设备刚解锁时 DEVICE_CREDENTIAL 会秒过，锁屏一闪而过），直接显示 PIN 页
    var autoLocked by remember { mutableStateOf(false) }
    // 上次离开前台的时间（elapsedRealtime）
    var lastStoppedAt by remember { mutableStateOf(0L) }
    // 上次解锁的时间（elapsedRealtime），用于自动锁定宽限期判断
    var lastUnlockedAt by remember { mutableStateOf(0L) }

    // 解锁：恢复导航到原位置（冷启动锁定则回首页）。
    // FLAG_SECURE 由 MainActivity 按应用锁开关常驻管理（不做动态切换，避免 recents 预览失效）
    val unlockAndRestore: () -> Unit = {
        isUnlocked = true
        autoLocked = false
        lastUnlockedAt = SystemClock.elapsedRealtime()
        lastStoppedAt = 0L
        val restoreStack = preLockBackStack
        preLockBackStack = null
        if (showOnboardingUpdated == true) {
            navState.resetTo(Screen.Onboarding)
        } else {
            navState.restoreCurrentStack(restoreStack ?: listOf(Screen.Home))
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    lastStoppedAt = SystemClock.elapsedRealtime()
                    // 切后台立即锁定：导航在后台即切换到锁屏，
                    // 回前台时锁屏已就位，避免"先闪旧页面再出锁屏"。
                    // 系统文件选择器打开期间豁免（返回后不要求重新解锁）
                    if (AppLockGuard.shouldLockOnStop(isUnlocked, settingsUpdated?.appLockEnabled == true)) {
                        isUnlocked = false
                        autoLocked = true
                    }
                }
                Lifecycle.Event.ON_START -> {
                    // 从文件选择器返回时清除豁免标记（launcher 回调清除作为双保险），
                    // 确保选择器打开期间再次切后台仍会正常锁定
                    AppLockGuard.isSystemPickerActive = false

                    // 自动锁定宽限期：解锁后 delay 分钟内切回，自动恢复解锁（免重新输入）
                    val delayMinutes = settingsUpdated?.appLockDelayMinutes ?: 0
                    val elapsedSinceUnlock = SystemClock.elapsedRealtime() - lastUnlockedAt
                    if (!isUnlocked && lastUnlockedAt > 0L &&
                        elapsedSinceUnlock < delayMinutes * 60_000L
                    ) {
                        unlockAndRestore()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val darkTheme = when (themeMode) {
        Settings.ThemeMode.LIGHT -> false
        Settings.ThemeMode.DARK -> true
        Settings.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        Settings.ThemeMode.DYNAMIC -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    // 更新主题模式
    LaunchedEffect(settings) {
        settings?.let {
            themeMode = it.themeMode
        }
    }

    // 关键安全修复：导航逻辑 - 修复竞态条件
    LaunchedEffect(showOnboarding, settings, isUnlocked) {
        val currentSettings = settingsUpdated ?: return@LaunchedEffect
        if (showOnboardingUpdated != null) {
            val currentDest = navStateUpdated.currentDestination

            // 计算是否需要 AppLock
            val needsAppLock = currentSettings.appLockEnabled && !isUnlocked

            // 计算目标页面
            val targetDestination = when {
                needsAppLock -> Screen.AppLock
                showOnboardingUpdated == true -> Screen.Onboarding
                else -> Screen.Home
            }

            when {
                currentDest == Screen.Loading -> {
                    // 初始加载：直接去目标页面
                    navStateUpdated.replaceWith(targetDestination)
                }
                needsAppLock && currentDest != Screen.AppLock -> {
                    // 需要 AppLock 但不在 AppLock 页面：强制去 AppLock，并记住锁定前的当前返回栈（含二级页面）
                    preLockBackStack = navStateUpdated.getCurrentStack()
                    navStateUpdated.replaceWith(Screen.AppLock)
                }
                !needsAppLock && currentDest == Screen.AppLock -> {
                    // 已解锁或锁已关闭，但页面仍停在锁屏：跳到目标页，避免停在锁屏
                    navStateUpdated.replaceWith(targetDestination)
                }
            }
        }
    }

    // 导航到子页面
    val navigateToDetail: (Screen) -> Unit = { screen ->
        navState.navigateToDetail(screen)
    }

    // 底部导航栏切换
    val navigateToTopLevel: (Screen) -> Unit = { screen ->
        navState.navigateToTopLevel(screen)
    }

    // 返回上一页
    val goBack: () -> Unit = {
        navState.goBack()
    }

    // 重置到指定页面（用于完成引导等场景）
    val resetTo: (Screen) -> Unit = { screen ->
        navState.resetTo(screen)
    }

    // 使用 entryProvider DSL 定义目的地
    val entryProvider = entryProvider<Screen> {
        // 初始/引导页面
        entry<Screen.Loading> {
            // 加载页面 - 简单的占位
        }

        entry<Screen.AppLock>(metadata = LockTransitionMetadata) {
            // 与 PIN 设置页一致的全屏 Modal 样式
            // 锁定页不可滑动手势/点外部/返回键关闭，必须验证 PIN 或生物识别才能解锁
            val lockSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
            ModalBottomSheet(
                onDismissRequest = { /* 禁止关闭：锁定页必须验证 PIN 才能解除 */ },
                sheetState = lockSheetState,
                sheetGesturesEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                AppLockScreen(
                    autoPromptBiometric = !autoLocked,
                    onUnlock = unlockAndRestore
                )
            }
        }

        entry<Screen.Onboarding> {
            OnboardingScreen(
                onGetStarted = {
                    mainViewModel.markOnboardingCompleted()
                },
                onComplete = {
                    resetTo(Screen.InitialSetup)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        entry<Screen.InitialSetup> {
            InitialSetupScreen(
                onComplete = {
                    resetTo(Screen.Home)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 顶部级路由 - 首页
        entry<Screen.Home> {
            // 当进入首页时，检查是否需要显示记录弹窗，然后重置状态
            LaunchedEffect(Unit) {
                showRecordSheetOnHome = false
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    PeriodBottomNavigation(
                        currentTopLevel = navState.topLevelKey,
                        onNavigate = navigateToTopLevel,
                        modifier = Modifier.shadow(8.dp)
                    )
                }
            ) { innerPadding ->
                HomeScreen(
                    onCalendarClick = { navigateToTopLevel(Screen.Calendar) },
                    onHistoryClick = { navigateToDetail(Screen.History) },
                    onSettingsClick = { navigateToTopLevel(Screen.Settings) },
                    showRecordSheetOnStart = showRecordSheetOnHome,
                    darkTheme = darkTheme,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }

        // 顶部级路由 - 日历
        entry<Screen.Calendar> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(com.example.periodvibe.R.string.cal_title)) },
                        // 顶栏不单独染色：静止与滚动均与页面背景同色
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        actions = {
                            IconButton(onClick = { navigateToDetail(Screen.History) }) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = stringResource(com.example.periodvibe.R.string.history_title)
                                )
                            }
                            IconButton(onClick = { showLegendDialog = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = stringResource(com.example.periodvibe.R.string.cal_legend_title)
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    PeriodBottomNavigation(
                        currentTopLevel = navState.topLevelKey,
                        onNavigate = navigateToTopLevel,
                        modifier = Modifier.shadow(8.dp)
                    )
                }
            ) { paddingValues ->
                CalendarScreen(
                    onNavigateToHome = { navigateToTopLevel(Screen.Home) },
                    onNavigateToHistory = { navigateToDetail(Screen.History) },
                    onNavigateToSettings = { navigateToTopLevel(Screen.Settings) },
                    onDateClick = {},
                    darkTheme = darkTheme,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            if (showLegendDialog) {
                LegendDialog(
                    onDismiss = { showLegendDialog = false },
                    darkTheme = darkTheme
                )
            }
        }

        // 顶部级路由 - 设置
        entry<Screen.Settings> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(com.example.periodvibe.R.string.set_title)) },
                        // 顶栏不单独染色：静止与滚动均与页面背景同色
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    PeriodBottomNavigation(
                        currentTopLevel = navState.topLevelKey,
                        onNavigate = navigateToTopLevel,
                        modifier = Modifier.shadow(8.dp)
                    )
                }
            ) { paddingValues ->
                SettingsScreen(
                    onNavigateToCycleParameters = { navigateToDetail(Screen.CycleParameters) },
                    onNavigateToReminders = { navigateToDetail(Screen.Reminders) },
                    onNavigateToTheme = { navigateToDetail(Screen.Theme) },
                    onNavigateToLanguage = { navigateToDetail(Screen.Language) },
                    onNavigateToPrivacy = { navigateToDetail(Screen.Privacy) },
                    onNavigateToDataManagement = { navigateToDetail(Screen.DataManagement) },
                    onNavigateToAbout = { navigateToDetail(Screen.About) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }

        // 子页面 - 历史记录（带滑动动画）
        entry<Screen.History>(metadata = SlideInFromRightMetadata) {
            HistoryScreen(
                onNavigateBack = goBack,
                onNavigateHomeToRecord = {
                    showRecordSheetOnHome = true
                    // 最简单直接的方案：重置到 Home
                    resetTo(Screen.Home)
                },
                darkTheme = darkTheme
            )
        }

        // 子页面 - 开发者选项（带滑动动画）
        entry<Screen.DeveloperOptions>(metadata = SlideInFromRightMetadata) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    androidx.compose.material3.CenterAlignedTopAppBar(
                        title = { Text(stringResource(com.example.periodvibe.R.string.set_dev_options)) },
                        navigationIcon = {
                            IconButton(onClick = goBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(com.example.periodvibe.R.string.set_back)
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                DeveloperOptionsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onResetOnboarding = {
                        mainViewModel.resetOnboarding {
                            resetTo(Screen.Onboarding)
                        }
                    }
                )
            }
        }

        // 子页面 - 周期参数设置（带滑动动画）
        entry<Screen.CycleParameters>(metadata = SlideInFromRightMetadata) {
            CycleParametersScreen(
                onNavigateBack = goBack
            )
        }

        // 子页面 - 提醒设置（带滑动动画）
        entry<Screen.Reminders>(metadata = SlideInFromRightMetadata) {
            RemindersScreen(
                onNavigateBack = goBack
            )
        }

        // 子页面 - 主题设置（带滑动动画）
        entry<Screen.Theme>(metadata = SlideInFromRightMetadata) {
            ThemeScreen(
                onNavigateBack = goBack
            )
        }

        // 子页面 - 语言设置（带滑动动画）
        entry<Screen.Language>(metadata = SlideInFromRightMetadata) {
            LanguageScreen(
                onNavigateBack = goBack
            )
        }

        // 子页面 - 隐私设置（带滑动动画）
        entry<Screen.Privacy>(metadata = SlideInFromRightMetadata) {
            PrivacyScreen(
                onNavigateBack = goBack,
                onNavigateToPinSetup = { mode ->
                    pinSetupMode = mode
                    showPinSetupSheet = true
                }
            )
        }

        // 子页面 - 数据管理（带滑动动画）
        entry<Screen.DataManagement>(metadata = SlideInFromRightMetadata) {
            DataManagementScreen(
                onNavigateBack = goBack
            )
        }

        // 子页面 - 关于（带滑动动画）
        entry<Screen.About>(metadata = SlideInFromRightMetadata) {
            AboutScreen(
                onNavigateBack = goBack,
                onNavigateToDeveloperOptions = { navigateToDetail(Screen.DeveloperOptions) }
            )
        }
    }

    NavDisplay(
        backStack = navState.backStack,
        entryProvider = entryProvider,
        onBack = goBack,
        // 显式提供装饰器（传了 entryDecorators 就必须包含默认的 SaveableStateHolder）：
        // - rememberSaveableStateHolderNavEntryDecorator：保留各条目的组合状态（切 tab / 进程恢复）
        // - rememberViewModelStoreNavEntryDecorator：为每个 NavEntry 提供独立的 ViewModelStore，
        //   使 hiltViewModel() 按目的地作用域（push 时创建，pop 时清除），而非 Activity 全局单例
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        // 默认没有动画，子页面通过 metadata 设置自己的动画
        transitionSpec = {
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        popTransitionSpec = {
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        predictivePopTransitionSpec = {
            ContentTransform(EnterTransition.None, ExitTransition.None)
        },
        modifier = modifier
    )

    if (showPinSetupSheet) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            onDismissRequest = {
                pinSetupViewModel.resetPin()
                showPinSetupSheet = false
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxSize()
        ) {
            PinSetupScreen(
                onPinSet = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showPinSetupSheet = false
                        }
                    }
                },
                viewModel = pinSetupViewModel,
                mode = pinSetupMode
            )
        }
    }
}
