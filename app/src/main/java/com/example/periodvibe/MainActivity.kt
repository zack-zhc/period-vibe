package com.example.periodvibe

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.periodvibe.utils.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // 无论用户是否同意，我们都继续安排通知（如果有权限的话）
        activityScope.launch {
            notificationScheduler.rescheduleAllNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 请求通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // 旧版本 Android 不需要请求通知权限，直接安排通知
            activityScope.launch {
                notificationScheduler.rescheduleAllNotifications()
            }
        }

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            var themeMode by remember { mutableStateOf(Settings.ThemeMode.SYSTEM) }

            LaunchedEffect(Unit) {
                mainViewModel.getSettings().collect { settings ->
                    settings?.let {
                        themeMode = it.themeMode
                        // 应用锁开启时 FLAG_SECURE 常驻：最近任务预览始终模糊/隐藏。
                        // 不做动态 addFlags/clearFlags 切换——同一会话内多次切换后
                        // 部分系统版本不再更新 recents 预览，模糊会失效。
                        if (it.appLockEnabled) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
                }
            }

            val darkTheme = when (themeMode) {
                Settings.ThemeMode.LIGHT -> false
                Settings.ThemeMode.DARK -> true
                Settings.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                Settings.ThemeMode.DYNAMIC -> isSystemInDarkTheme()
            }

            PeriodVibeTheme(
                darkTheme = darkTheme,
                dynamicColor = themeMode == Settings.ThemeMode.DYNAMIC
            ) {
                PeriodVibeNavHost(
                    mainViewModel = mainViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
