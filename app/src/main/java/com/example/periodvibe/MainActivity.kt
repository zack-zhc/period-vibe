package com.example.periodvibe

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.periodvibe.domain.model.Settings
import com.example.periodvibe.navigation.PeriodVibeNavHost
import com.example.periodvibe.ui.theme.PeriodVibeTheme
import com.example.periodvibe.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            var themeMode by remember { mutableStateOf(Settings.ThemeMode.SYSTEM) }

            LaunchedEffect(Unit) {
                mainViewModel.getSettings().collect { settings ->
                    settings?.let {
                        themeMode = it.themeMode
                    }
                }
            }

            val darkTheme = when (themeMode) {
                Settings.ThemeMode.LIGHT -> false
                Settings.ThemeMode.DARK -> true
                Settings.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            PeriodVibeTheme(darkTheme = darkTheme) {
                PeriodVibeNavHost(
                    mainViewModel = mainViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
