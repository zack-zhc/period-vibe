package com.example.periodvibe.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val PIN_LENGTH = 4

// 折叠屏/平板等宽屏下限制键盘尺寸，避免按键随行宽无限放大导致布局溢出
private val MaxKeypadWidth = 400.dp
// 按键最大尺寸，宽屏下封顶
private val MaxKeySize = 112.dp
// 行间/键间间隙
private val KeySpacing = 8.dp

@Composable
fun PinKeyboard(
    onNumberClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = MaxKeypadWidth)
            .fillMaxWidth()
    ) {
        // 按键尺寸 = (可用宽度 - 两个键间间隙) / 3，宽屏封顶。
        // 显式固定尺寸，不依赖 weight/aspectRatio，保证正方形与间隙在所有设备上一致
        val keySize = ((maxWidth - KeySpacing * 2) / 3f).coerceAtMost(MaxKeySize)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KeySpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PinKey(size = keySize, number = 1, onClick = { onNumberClick(1) })
                PinKey(size = keySize, number = 2, onClick = { onNumberClick(2) })
                PinKey(size = keySize, number = 3, onClick = { onNumberClick(3) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PinKey(size = keySize, number = 4, onClick = { onNumberClick(4) })
                PinKey(size = keySize, number = 5, onClick = { onNumberClick(5) })
                PinKey(size = keySize, number = 6, onClick = { onNumberClick(6) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PinKey(size = keySize, number = 7, onClick = { onNumberClick(7) })
                PinKey(size = keySize, number = 8, onClick = { onNumberClick(8) })
                PinKey(size = keySize, number = 9, onClick = { onNumberClick(9) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 空白占位，让 0 居中于 8 下方
                Spacer(modifier = Modifier.size(keySize))
                PinKey(size = keySize, number = 0, onClick = { onNumberClick(0) })
                FilledTonalButton(
                    onClick = onBackspaceClick,
                    modifier = Modifier.size(keySize),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace"
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKey(size: Dp, number: Int, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier.size(size),
        colors = ButtonDefaults.filledTonalButtonColors()
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun PinDotRow(
    entered: Int,
    total: Int = PIN_LENGTH,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val filled = index < entered
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
        }
    }
}
