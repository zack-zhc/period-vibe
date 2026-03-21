package com.example.periodvibe.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // Navigation 3: 使用 rememberNavBackStack 直接管理返回栈
    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(Screen.Loading) as NavBackStack<Screen>

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
            if (backStack.lastOrNull() == Screen.Loading) {
                backStack.clear()
                backStack.add(destination)
            }
        }
    }

    // 导航到子页面（历史、开发者选项）- 添加到栈顶
    val navigateToDetail: (Screen) -> Unit = { screen ->
        backStack.add(screen)
    }

    // 底部导航栏切换 - 替换当前栈
    val navigateToBottomBarScreen: (Screen) -> Unit = { screen ->
        val newKeys = if (screen == Screen.Home) {
            listOf(Screen.Home)
        } else {
            listOf(Screen.Home, screen)
        }
        backStack.clear()
        backStack.addAll(newKeys)
    }

    // 返回上一页
    val goBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    // Navigation 3: 定义目的地提供者
    val myEntryProvider: (Screen) -> NavEntry<Screen> = { key ->
        when (key) {
            is Screen.Loading -> NavEntry(key) { }
            is Screen.AppLock -> NavEntry(key) {
                AppLockScreen(
                    onUnlock = {
                        val destination = if (showOnboarding == true) {
                            Screen.Onboarding
                        } else {
                            Screen.Home
                        }
                        backStack.clear()
                        backStack.add(destination)
                    }
                )
            }
            is Screen.Onboarding -> NavEntry(key) {
                OnboardingScreen(
                    onGetStarted = {
                        mainViewModel.markOnboardingCompleted()
                    },
                    onComplete = {
                        backStack.clear()
                        backStack.add(Screen.InitialSetup)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is Screen.InitialSetup -> NavEntry(key) {
                InitialSetupScreen(
                    onComplete = {
                        backStack.clear()
                        backStack.add(Screen.Home)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is Screen.Home -> NavEntry(key) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        PeriodBottomNavigation(
                            currentRoute = "home",
                            onNavigate = { routeStr ->
                                val route = when (routeStr) {
                                    "home" -> Screen.Home
                                    "calendar" -> Screen.Calendar
                                    "settings" -> Screen.Settings
                                    else -> Screen.Home
                                }
                                navigateToBottomBarScreen(route)
                            }
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(
                        onRecordClick = { },
                        onCalendarClick = { navigateToBottomBarScreen(Screen.Calendar) },
                        onHistoryClick = { navigateToDetail(Screen.History) },
                        onSettingsClick = { navigateToBottomBarScreen(Screen.Settings) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
            is Screen.Calendar -> NavEntry(key) {
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
                        PeriodBottomNavigation(
                            currentRoute = "calendar",
                            onNavigate = { routeStr ->
                                val route = when (routeStr) {
                                    "home" -> Screen.Home
                                    "calendar" -> Screen.Calendar
                                    "settings" -> Screen.Settings
                                    else -> Screen.Home
                                }
                                navigateToBottomBarScreen(route)
                            }
                        )
                    }
                ) { paddingValues ->
                    CalendarScreen(
                        onNavigateToHome = { navigateToBottomBarScreen(Screen.Home) },
                        onNavigateToHistory = { navigateToDetail(Screen.History) },
                        onNavigateToSettings = { navigateToBottomBarScreen(Screen.Settings) },
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
            is Screen.History -> NavEntry(key) {
                val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                    androidx.compose.material3.rememberTopAppBarState()
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        androidx.compose.material3.MediumTopAppBar(
                            title = { Text("历史记录") },
                            navigationIcon = {
                                IconButton(onClick = { goBack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                        PeriodBottomNavigation(
                            currentRoute = "calendar",
                            onNavigate = { routeStr ->
                                val route = when (routeStr) {
                                    "home" -> Screen.Home
                                    "calendar" -> Screen.Calendar
                                    "settings" -> Screen.Settings
                                    else -> Screen.Home
                                }
                                navigateToBottomBarScreen(route)
                            }
                        )
                    }
                ) { paddingValues ->
                    HistoryScreen(
                        onNavigateToHome = { navigateToBottomBarScreen(Screen.Home) },
                        onNavigateToCalendar = { goBack() },
                        onNavigateToSettings = { navigateToBottomBarScreen(Screen.Settings) },
                        scrollBehavior = scrollBehavior,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
            is Screen.Settings -> NavEntry(key) {
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
                        PeriodBottomNavigation(
                            currentRoute = "settings",
                            onNavigate = { routeStr ->
                                val route = when (routeStr) {
                                    "home" -> Screen.Home
                                    "calendar" -> Screen.Calendar
                                    "settings" -> Screen.Settings
                                    else -> Screen.Home
                                }
                                navigateToBottomBarScreen(route)
                            }
                        )
                    }
                ) { paddingValues ->
                    SettingsScreen(
                        onNavigateToHome = { navigateToBottomBarScreen(Screen.Home) },
                        onNavigateToCalendar = { navigateToBottomBarScreen(Screen.Calendar) },
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
            is Screen.DeveloperOptions -> NavEntry(key) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        androidx.compose.material3.CenterAlignedTopAppBar(
                            title = { Text("开发者选项") },
                            navigationIcon = {
                                IconButton(
                                    onClick = { goBack() }
                                ) {
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
                                backStack.clear()
                                backStack.add(Screen.Onboarding)
                            }
                        }
                    )
                }
            }
        }
    }

    // Navigation 3: 使用 NavDisplay
    NavDisplay<Screen>(
        backStack = backStack,
        entryProvider = myEntryProvider,
        onBack = { goBack() },
        transitionSpec = {
            // 获取初始和目标状态的 key
            val initialKey = initialState.entries.lastOrNull()?.contentKey as? Screen
            val targetKey = targetState.entries.lastOrNull()?.contentKey as? Screen

            // 判断是否是 Tab 切换
            val isTabChange = (initialKey is Screen.Home || initialKey is Screen.Calendar || initialKey is Screen.Settings) &&
                (targetKey is Screen.Home || targetKey is Screen.Calendar || targetKey is Screen.Settings)

            when {
                isTabChange -> {
                    // 底部 Tab 切换 - 无动画
                    ContentTransform(EnterTransition.None, ExitTransition.None)
                }
                else -> {
                    // 子页面滑动动画
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut()
                }
            }
        },
        modifier = modifier
    )
}
