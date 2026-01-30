package com.example.periodvibe

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.ui.applock.AppLockScreen
import com.example.periodvibe.ui.applock.PinSetupScreen
import com.example.periodvibe.ui.applock.PinSetupViewModel
import com.example.periodvibe.ui.calendar.CalendarScreen
import com.example.periodvibe.ui.home.HomeScreen
import com.example.periodvibe.ui.home.PeriodBottomNavigation
import com.example.periodvibe.ui.history.HistoryScreen
import com.example.periodvibe.ui.onboarding.OnboardingScreen
import com.example.periodvibe.ui.setup.InitialSetupScreen
import com.example.periodvibe.ui.settings.DeveloperOptionsScreen
import com.example.periodvibe.ui.settings.SettingsScreen
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Loading : AppScreen()
    object AppLock : AppScreen()
    object Onboarding : AppScreen()
    object InitialSetup : AppScreen()
    data class Main(val route: String) : AppScreen()
    object DeveloperOptions : AppScreen()
}

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val pinSetupViewModel: PinSetupViewModel = hiltViewModel()
            val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Loading) }
            var themeMode by remember { mutableStateOf(Settings.ThemeMode.SYSTEM) }
            var appLockEnabled by remember { mutableStateOf(false) }

            var showPinSetupSheet by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                mainViewModel.getSettings().collect { settings ->
                    settings?.let {
                        themeMode = it.themeMode
                        appLockEnabled = it.appLockEnabled
                    }
                }
            }

            LaunchedEffect(showOnboarding, appLockEnabled) {
                if (showOnboarding != null && currentScreen is AppScreen.Loading) {
                    currentScreen = when {
                        appLockEnabled -> AppScreen.AppLock
                        showOnboarding == true -> AppScreen.Onboarding
                        else -> AppScreen.Main("home")
                    }
                }
            }

            val darkTheme = when (themeMode) {
                Settings.ThemeMode.LIGHT -> false
                Settings.ThemeMode.DARK -> true
                Settings.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            PeriodVibeTheme(darkTheme = darkTheme) {
                if (showPinSetupSheet) {
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

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val screen = currentScreen) {
                        is AppScreen.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is AppScreen.AppLock -> {
                            AppLockScreen(onUnlock = {
                                currentScreen = if (showOnboarding == true) {
                                    AppScreen.Onboarding
                                } else {
                                    AppScreen.Main("home")
                                }
                            })
                        }

                        is AppScreen.Onboarding -> {
                            OnboardingScreen(
                                onGetStarted = {
                                    mainViewModel.markOnboardingCompleted()
                                },
                                onComplete = {
                                    currentScreen = AppScreen.InitialSetup
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        is AppScreen.InitialSetup -> {
                            InitialSetupScreen(
                                onComplete = {
                                    currentScreen = AppScreen.Main("home")
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        is AppScreen.Main -> {
                            when (screen.route) {
                                "home" -> {
                                    HomeScreen(
                                        onRecordClick = { },
                                        onCalendarClick = { currentScreen = AppScreen.Main("calendar") },
                                        onHistoryClick = { currentScreen = AppScreen.Main("history") },
                                        onSettingsClick = { currentScreen = AppScreen.Main("settings") },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                "calendar" -> {
                                    CalendarScreen(
                                        onNavigateToHome = { currentScreen = AppScreen.Main("home") },
                                        onNavigateToHistory = { currentScreen = AppScreen.Main("history") },
                                        onNavigateToSettings = { currentScreen = AppScreen.Main("settings") },
                                        onDateClick = {},
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                "history" -> {
                                    HistoryScreen(
                                        onNavigateToHome = { currentScreen = AppScreen.Main("home") },
                                        onNavigateToCalendar = { currentScreen = AppScreen.Main("calendar") },
                                        onNavigateToSettings = { currentScreen = AppScreen.Main("settings") },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                "settings" -> {
                                    androidx.compose.material3.Scaffold(
                                        modifier = Modifier.fillMaxSize(),
                                        bottomBar = {
                                            PeriodBottomNavigation(
                                                currentRoute = "settings",
                                                onNavigate = { route ->
                                                    currentScreen = AppScreen.Main(route)
                                                }
                                            )
                                        }
                                    ) { paddingValues ->
                                        SettingsScreen(
                                            onNavigateToHome = { currentScreen = AppScreen.Main("home") },
                                            onNavigateToCalendar = { currentScreen = AppScreen.Main("calendar") },
                                            onNavigateToHistory = { currentScreen = AppScreen.Main("history") },
                                            onNavigateToDeveloperOptions = { currentScreen = AppScreen.DeveloperOptions },
                                            onNavigateToPinSetup = { showPinSetupSheet = true },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues)
                                        )
                                    }
                                }
                            }
                        }

                        is AppScreen.DeveloperOptions -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                topBar = {
                                    CenterAlignedTopAppBar(
                                        title = { Text("开发者选项") },
                                        navigationIcon = {
                                            IconButton(
                                                onClick = { currentScreen = AppScreen.Main("settings") }
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
                                            currentScreen = AppScreen.Onboarding
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PeriodVibeTheme {
        Greeting("Android")
    }
}
