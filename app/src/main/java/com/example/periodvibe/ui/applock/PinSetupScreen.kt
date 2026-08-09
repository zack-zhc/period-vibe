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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.periodvibe.R

/** PIN 设置模式：SETUP=首次设置，CHANGE=修改（先验证当前 PIN） */
enum class PinSetupMode { SETUP, CHANGE }

private enum class PinStage { VERIFY, NEW, CONFIRM }

@Composable
fun PinSetupScreen(
    onPinSet: () -> Unit,
    viewModel: PinSetupViewModel,
    mode: PinSetupMode = PinSetupMode.SETUP
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val wrongPinText = stringResource(R.string.applock_wrong_pin)
    var stage by remember { mutableStateOf(if (mode == PinSetupMode.CHANGE) PinStage.VERIFY else PinStage.NEW) }
    var verifyError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is PinSetupUiState.PinSet) {
            onPinSet()
        } else if (uiState is PinSetupUiState.Error) {
            snackbarHostState.showSnackbar(
                context.getString((uiState as PinSetupUiState.Error).messageRes)
            )
            // 两次输入不一致：清空并从首次输入重新开始
            viewModel.resetPin()
            stage = PinStage.NEW
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
                text = when (stage) {
                    PinStage.VERIFY -> stringResource(R.string.pin_verify_title)
                    PinStage.NEW -> if (mode == PinSetupMode.CHANGE) {
                        stringResource(R.string.pin_new_title)
                    } else {
                        stringResource(R.string.pin_set_title)
                    }
                    PinStage.CONFIRM -> stringResource(R.string.pin_confirm_title)
                },
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            PinDotRow(
                entered = when (stage) {
                    PinStage.VERIFY -> viewModel.verifyPin.value.length
                    PinStage.NEW -> viewModel.pin.value.length
                    PinStage.CONFIRM -> viewModel.confirmPin.value.length
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(24.dp)) {
                verifyError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            PinKeyboard(
                onNumberClick = { digit ->
                    when (stage) {
                        PinStage.VERIFY -> {
                            if (viewModel.verifyPin.value.length < PIN_LENGTH) {
                                viewModel.onVerifyPinChange(viewModel.verifyPin.value + digit)
                                if (viewModel.verifyPin.value.length == PIN_LENGTH) {
                                    if (viewModel.verifyCurrentPin()) {
                                        verifyError = null
                                        viewModel.resetVerifyPin()
                                        stage = PinStage.NEW
                                    } else {
                                        verifyError = wrongPinText
                                        viewModel.resetVerifyPin()
                                    }
                                }
                            }
                        }
                        PinStage.NEW -> {
                            if (viewModel.pin.value.length < PIN_LENGTH) {
                                viewModel.onPinChange(viewModel.pin.value + digit)
                                // 输满 4 位自动进入确认阶段
                                if (viewModel.pin.value.length == PIN_LENGTH) {
                                    stage = PinStage.CONFIRM
                                }
                            }
                        }
                        PinStage.CONFIRM -> {
                            if (viewModel.confirmPin.value.length < PIN_LENGTH) {
                                viewModel.onConfirmPinChange(viewModel.confirmPin.value + digit)
                                // 输满 4 位自动提交校验
                                if (viewModel.confirmPin.value.length == PIN_LENGTH) {
                                    viewModel.onSetPin()
                                }
                            }
                        }
                    }
                },
                onBackspaceClick = {
                    when (stage) {
                        PinStage.VERIFY -> viewModel.onVerifyPinChange(viewModel.verifyPin.value.dropLast(1))
                        PinStage.NEW -> viewModel.onPinChange(viewModel.pin.value.dropLast(1))
                        PinStage.CONFIRM -> viewModel.onConfirmPinChange(viewModel.confirmPin.value.dropLast(1))
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
