package com.example.periodvibe.ui.applock

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.periodvibe.R
import kotlinx.coroutines.delay

/**
 * 沿 ContextWrapper 链向上查找 FragmentActivity。
 * 全屏 Modal（ModalBottomSheet）会包裹 ContextThemeWrapper，
 * LocalContext.current 不再是 Activity，直接强转会 ClassCastException。
 */
private fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun AppLockScreen(
    onUnlock: () -> Unit,
    autoPromptBiometric: Boolean = true,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lockoutRemainingSeconds by viewModel.lockoutRemainingSeconds.collectAsState()
    val context = LocalContext.current

    // 进入锁屏页时若残留 Unlocked（AppLock 的 ViewModel 可能被复用，携带上次解锁状态），
    // 重置为 Idle 显示 PIN 页，避免"自动锁定后被秒解"
    var staleUnlockedOnEntry by remember { mutableStateOf(uiState is AppLockUiState.Unlocked) }
    if (staleUnlockedOnEntry) {
        staleUnlockedOnEntry = false
        LaunchedEffect(Unit) {
            viewModel.resetToIdle()
        }
    }

    // 解锁跳转：仅当状态由"非解锁"变为"解锁"（本次会话内用户验证成功）才触发，
    // 排除进入时即残留 Unlocked 的情况
    var previousWasUnlocked by remember { mutableStateOf(uiState is AppLockUiState.Unlocked) }
    LaunchedEffect(uiState) {
        val unlockedNow = uiState is AppLockUiState.Unlocked
        if (unlockedNow && !previousWasUnlocked) {
            onUnlock()
        }
        previousWasUnlocked = unlockedNow
    }

    when (uiState) {
        is AppLockUiState.Unlocked -> {
            // 不做任何事：Unlocked 只用于触发 onUnlock（见上方 LaunchedEffect），
            // 残留状态已在进入时重置
        }
        else -> {
            // 自动锁定（后台恢复）场景不自动弹生物识别：
            // 设备刚解锁时 DEVICE_CREDENTIAL 会立即通过，锁屏一闪而过，直接显示 PIN 页
            LaunchedEffect(autoPromptBiometric) {
                if (autoPromptBiometric && viewModel.hasPin()) {
                    context.findFragmentActivity()?.let { activity ->
                        viewModel.showBiometricPrompt(activity)
                    }
                }
            }

            // PIN 错误提示后自动清空，方便重新输入
            LaunchedEffect(uiState) {
                if (uiState is AppLockUiState.Error) {
                    delay(1200)
                    viewModel.clearError()
                    viewModel.onPinChange("")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.applock_enter_pin_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                PinDotRow(entered = viewModel.pin.value.length)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.height(24.dp)) {
                    if (lockoutRemainingSeconds > 0L) {
                        Text(
                            text = stringResource(R.string.applock_lockout_message, lockoutRemainingSeconds),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (uiState is AppLockUiState.Error) {
                        val error = uiState as AppLockUiState.Error
                        Text(
                            text = error.messageRes?.let { stringResource(it) } ?: error.message ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                PinKeyboard(
                    enabled = lockoutRemainingSeconds <= 0L,
                    onNumberClick = { digit ->
                        if (viewModel.pin.value.length < PIN_LENGTH) {
                            viewModel.onPinChange(viewModel.pin.value + digit)
                            // 输满 4 位自动校验
                            if (viewModel.pin.value.length == PIN_LENGTH) {
                                viewModel.onPinEntered()
                            }
                        }
                    },
                    onBackspaceClick = {
                        viewModel.onPinChange(viewModel.pin.value.dropLast(1))
                    }
                )
            }
        }
    }
}
