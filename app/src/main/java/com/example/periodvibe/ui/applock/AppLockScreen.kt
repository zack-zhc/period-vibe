package com.example.periodvibe.ui.applock

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    onUnlock: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (uiState) {
        is AppLockUiState.Unlocked -> {
            LaunchedEffect(Unit) {
                onUnlock()
            }
        }
        else -> {
            // Show biometric prompt if available, otherwise show PIN screen
            LaunchedEffect(Unit) {
                if (viewModel.hasPin()) {
                    viewModel.showBiometricPrompt(context as FragmentActivity)
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
                    text = "输入 PIN 码",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                PinDotRow(entered = viewModel.pin.value.length)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.height(24.dp)) {
                    if (uiState is AppLockUiState.Error) {
                        Text(
                            text = (uiState as AppLockUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                PinKeyboard(
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
