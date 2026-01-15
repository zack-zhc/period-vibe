package com.example.periodvibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.periodvibe.ui.home.HomeScreen
import com.example.periodvibe.ui.home.PeriodBottomNavigation
import com.example.periodvibe.ui.history.HistoryScreen
import com.example.periodvibe.ui.onboarding.OnboardingScreen
import com.example.periodvibe.ui.setup.InitialSetupScreen
import com.example.periodvibe.ui.settings.SettingsScreen
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeriodVibeTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
                var showSetup by remember { mutableStateOf(false) }
                var currentRoute by remember { mutableStateOf("home") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        showOnboarding == null -> {
                            Text(
                                text = "加载中...",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        showOnboarding == true -> {
                            OnboardingScreen(
                                onGetStarted = {
                                    mainViewModel.markOnboardingCompleted()
                                    showSetup = true
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                        showSetup -> {
                            InitialSetupScreen(
                                onComplete = {
                                    showSetup = false
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                        else -> {
                            when (currentRoute) {
                                "home" -> {
                                    HomeScreen(
                                        onRecordClick = { },
                                        onCalendarClick = { currentRoute = "calendar" },
                                        onHistoryClick = { currentRoute = "history" },
                                        onSettingsClick = { currentRoute = "settings" },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "calendar" -> {
                                    Text(
                                        text = "日历",
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                "history" -> {
                                    HistoryScreen(
                                        onNavigateToHome = { currentRoute = "home" },
                                        onNavigateToCalendar = { currentRoute = "calendar" },
                                        onNavigateToSettings = { currentRoute = "settings" },
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
                                                    when (route) {
                                                        "home" -> currentRoute = "home"
                                                        "calendar" -> currentRoute = "calendar"
                                                        "history" -> currentRoute = "history"
                                                        "settings" -> {}
                                                    }
                                                }
                                            )
                                        }
                                    ) { paddingValues ->
                                        SettingsScreen(
                                            onNavigateToHome = { currentRoute = "home" },
                                            onNavigateToCalendar = { currentRoute = "calendar" },
                                            onNavigateToHistory = { currentRoute = "history" },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues)
                                        )
                                    }
                                }
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