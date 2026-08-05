package com.example.periodvibe.ui.applock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp

@Composable
fun PinSetupScreen(
    onPinSet: () -> Unit,
    viewModel: PinSetupViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // true = 确认阶段，false = 首次输入阶段
    var confirming by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is PinSetupUiState.PinSet) {
            onPinSet()
        } else if (uiState is PinSetupUiState.Error) {
            snackbarHostState.showSnackbar((uiState as PinSetupUiState.Error).message)
            // 两次输入不一致：清空并从首次输入重新开始
            viewModel.resetPin()
            confirming = false
            viewModel.resetErrorState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (confirming) "确认 PIN 码" else "设置 PIN 码",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            PinDotRow(
                entered = if (confirming) {
                    viewModel.confirmPin.value.length
                } else {
                    viewModel.pin.value.length
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            PinKeyboard(
                onNumberClick = { digit ->
                    if (confirming) {
                        if (viewModel.confirmPin.value.length < PIN_LENGTH) {
                            viewModel.onConfirmPinChange(viewModel.confirmPin.value + digit)
                            // 输满 4 位自动提交校验
                            if (viewModel.confirmPin.value.length == PIN_LENGTH) {
                                viewModel.onSetPin()
                            }
                        }
                    } else {
                        if (viewModel.pin.value.length < PIN_LENGTH) {
                            viewModel.onPinChange(viewModel.pin.value + digit)
                            // 输满 4 位自动进入确认阶段
                            if (viewModel.pin.value.length == PIN_LENGTH) {
                                confirming = true
                            }
                        }
                    }
                },
                onBackspaceClick = {
                    if (confirming) {
                        viewModel.onConfirmPinChange(viewModel.confirmPin.value.dropLast(1))
                    } else {
                        viewModel.onPinChange(viewModel.pin.value.dropLast(1))
                    }
                }
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
