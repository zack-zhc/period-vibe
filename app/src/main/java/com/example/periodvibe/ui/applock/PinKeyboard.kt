package com.example.periodvibe.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp

const val PIN_LENGTH = 4

@Composable
fun PinKeyboard(
    onNumberClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PinKey(number = 1, onClick = { onNumberClick(1) })
            PinKey(number = 2, onClick = { onNumberClick(2) })
            PinKey(number = 3, onClick = { onNumberClick(3) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PinKey(number = 4, onClick = { onNumberClick(4) })
            PinKey(number = 5, onClick = { onNumberClick(5) })
            PinKey(number = 6, onClick = { onNumberClick(6) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PinKey(number = 7, onClick = { onNumberClick(7) })
            PinKey(number = 8, onClick = { onNumberClick(8) })
            PinKey(number = 9, onClick = { onNumberClick(9) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 空白占位，让 0 居中于 8 下方
            Spacer(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
                    .aspectRatio(1f)
            )
            PinKey(number = 0, onClick = { onNumberClick(0) })
            FilledTonalButton(
                onClick = onBackspaceClick,
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
                    .aspectRatio(1f),
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

@Composable
private fun RowScope.PinKey(number: Int, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier
            .padding(4.dp)
            .weight(1f)
            .aspectRatio(1f),
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
