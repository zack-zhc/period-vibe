package com.example.periodvibe.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DeveloperOptionsScreen(
    modifier: Modifier = Modifier,
    onResetOnboarding: () -> Unit,
    viewModel: DeveloperOptionsViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "开发者选项",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "测试和调试应用功能。",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onResetOnboarding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("重置欢迎引导页")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "通知测试",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendTestNotification() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("测试立即通知")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendTestPrivacyNotification() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("测试隐私模式通知")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendTestDelayedNotification(10) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("测试10秒后经期通知")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendTestOvulationNotification(10) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("测试10秒后排卵日通知")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.rescheduleNotification() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("重新安排所有周期通知")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "设置快速切换",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.togglePrivacyMode(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开启隐私模式")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.togglePrivacyMode(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("关闭隐私模式")
        }
    }
}
