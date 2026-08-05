package com.example.periodvibe.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * 全站卡片样式 token：
 * Hero 卡片（主状态卡/空态卡）与次要卡片（信息卡/预告卡）统一圆角与边框，
 * 避免各页面自行定义导致视觉分叉。
 */
val HomeCardShape: Shape = RoundedCornerShape(20.dp)

@Composable
fun cardBorderStroke(alpha: Float = 0.3f): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha))
