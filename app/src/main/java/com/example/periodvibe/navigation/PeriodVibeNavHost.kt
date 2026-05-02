package com.example.periodvibe.navigation

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.ui.applock.AppLockScreen
import com.example.periodvibe.ui.applock.PinSetupScreen
import com.example.periodvibe.ui.applock.PinSetupViewModel
import com.example.periodvibe.ui.calendar.CalendarScreen
import com.example.periodvibe.ui.calendar.LegendDialog
import com.example.periodvibe.ui.home.HomeScreen
import com.example.periodvibe.ui.home.PeriodBottomNavigation
import com.example.periodvibe.ui.history.HistoryScreen
import com.example.periodvibe.ui.onboarding.OnboardingScreen
import com.example.periodvibe.ui.settings.DeveloperOptionsScreen
import com.example.periodvibe.ui.settings.SettingsScreen
import com.example.periodvibe.ui.setup.InitialSetupScreen
import com.example.periodvibe.ui.viewmodel.MainViewModel
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
 * 创建并记住 TopLevelBackStack 实例
 */
@Composable
fun rememberPeriodVibeNavState(
    startKey: Screen = Screen.Loading
): TopLevelBackStack<Screen> {
    return remember { TopLevelBackStack(startKey) }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PeriodVibeNavHost(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    var themeMode by remember { mutableStateOf(Settings.ThemeMode.SYSTEM) }
    var appLockEnabled by remember { mutableStateOf(false) }

    var showPinSetupSheet by remember { mutableStateOf(false) }
    var showLegendDialog by remember { mutableStateOf(false) }
    val pinSetupViewModel: PinSetupViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // 使用多返回栈导航状态
    val navState = rememberPeriodVibeNavState()

    LaunchedEffect(Unit) {
        mainViewModel.getSettings().collect { settings ->
            settings?.let {
                themeMode = it.themeMode
                appLockEnabled = it.appLockEnabled
            }
        }
    }

    // 处理初始导航
    LaunchedEffect(showOnboarding, appLockEnabled) {
        if (showOnboarding != null) {
            val destination = when {
                appLockEnabled -> Screen.AppLock
                showOnboarding == true -> Screen.Onboarding
                else -> Screen.Home
            }
            if (navState.backStack.firstOrNull() == Screen.Loading) {
                navState.replaceWith(destination)
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

        entry<Screen.AppLock> {
            AppLockScreen(
                onUnlock = {
                    val destination = if (showOnboarding == true) {
                        Screen.Onboarding
                    } else {
                        Screen.Home
                    }
                    resetTo(destination)
                }
            )
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
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    PeriodBottomNavigationBar(
                        currentTopLevel = navState.topLevelKey,
                        onNavigateToTopLevel = navigateToTopLevel
                    )
                }
            ) { innerPadding ->
                HomeScreen(
                    onRecordClick = { },
                    onCalendarClick = { navigateToTopLevel(Screen.Calendar) },
                    onHistoryClick = { navigateToDetail(Screen.History) },
                    onSettingsClick = { navigateToTopLevel(Screen.Settings) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }

        // 顶部级路由 - 日历
        entry<Screen.Calendar> {
            val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                androidx.compose.material3.rememberTopAppBarState()
            )
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    androidx.compose.material3.MediumTopAppBar(
                        title = { Text("日历") },
                        actions = {
                            IconButton(onClick = { navigateToDetail(Screen.History) }) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "历史记录"
                                )
                            }
                            IconButton(onClick = { showLegendDialog = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "图例"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                bottomBar = {
                    PeriodBottomNavigationBar(
                        currentTopLevel = navState.topLevelKey,
                        onNavigateToTopLevel = navigateToTopLevel
                    )
                }
            ) { paddingValues ->
                CalendarScreen(
                    onNavigateToHome = { navigateToTopLevel(Screen.Home) },
                    onNavigateToHistory = { navigateToDetail(Screen.History) },
                    onNavigateToSettings = { navigateToTopLevel(Screen.Settings) },
                    onDateClick = {},
                    scrollBehavior = scrollBehavior,
                    onLegendClick = { showLegendDialog = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            if (showLegendDialog) {
                LegendDialog(onDismiss = { showLegendDialog = false })
            }
        }

        // 顶部级路由 - 设置
        entry<Screen.Settings> {
            val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                androidx.compose.material3.rememberTopAppBarState()
            )
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    androidx.compose.material3.MediumTopAppBar(
                        title = { Text("设置") },
                        scrollBehavior = scrollBehavior
                    )
                },
                bottomBar = {
                    PeriodBottomNavigationBar(
                        currentTopLevel = navState.topLevelKey,
                        onNavigateToTopLevel = navigateToTopLevel
                    )
                }
            ) { paddingValues ->
                SettingsScreen(
                    onNavigateToHome = { navigateToTopLevel(Screen.Home) },
                    onNavigateToCalendar = { navigateToTopLevel(Screen.Calendar) },
                    onNavigateToHistory = { navigateToDetail(Screen.History) },
                    onNavigateToDeveloperOptions = { navigateToDetail(Screen.DeveloperOptions) },
                    onNavigateToPinSetup = { showPinSetupSheet = true },
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

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
                        viewModel = pinSetupViewModel
                    )
                }
            }
        }

        // 子页面 - 历史记录（带滑动动画）
        entry<Screen.History>(metadata = SlideInFromRightMetadata) {
            HistoryScreen(
                onNavigateBack = goBack,
                onNavigateToCalendar = { navigateToTopLevel(Screen.Calendar) }
            )
        }

        // 子页面 - 开发者选项（带滑动动画）
        entry<Screen.DeveloperOptions>(metadata = SlideInFromRightMetadata) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    androidx.compose.material3.CenterAlignedTopAppBar(
                        title = { Text("开发者选项") },
                        navigationIcon = {
                            IconButton(onClick = goBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回"
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
    }

    NavDisplay(
        backStack = navState.backStack,
        entryProvider = entryProvider,
        onBack = goBack,
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
}

/**
 * 底部导航栏组件
 */
@Composable
private fun PeriodBottomNavigationBar(
    currentTopLevel: Screen,
    onNavigateToTopLevel: (Screen) -> Unit
) {
    val currentRoute = when (currentTopLevel) {
        Screen.Home -> "home"
        Screen.Calendar -> "calendar"
        Screen.Settings -> "settings"
        else -> "home"
    }

    PeriodBottomNavigation(
        currentRoute = currentRoute,
        onNavigate = { routeStr ->
            val route = when (routeStr) {
                "home" -> Screen.Home
                "calendar" -> Screen.Calendar
                "settings" -> Screen.Settings
                else -> Screen.Home
            }
            onNavigateToTopLevel(route)
        },
        modifier = Modifier.shadow(8.dp)
    )
}
