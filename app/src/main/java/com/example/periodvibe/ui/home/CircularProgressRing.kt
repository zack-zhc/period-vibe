package com.example.periodvibe.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 14.dp,
    backgroundColor: Color,
    progressColor: Color,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "progressAnimation"
    )

    val progressBrush = remember(progressColor) {
        Brush.sweepGradient(
            colors = listOf(
                progressColor.copy(alpha = 0.6f),
                progressColor,
                progressColor.copy(alpha = 0.8f)
            )
        )
    }
    val backgroundBrush = remember(backgroundColor) {
        Brush.sweepGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.3f),
                backgroundColor.copy(alpha = 0.5f),
                backgroundColor.copy(alpha = 0.3f)
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            // 背景环
            drawCircle(
                brush = backgroundBrush,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(
                    width = strokeWidthPx * 0.6f,
                    cap = StrokeCap.Round
                )
            )

            // 进度弧
            if (animatedProgress > 0f) {
                drawArc(
                    brush = progressBrush,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    ),
                    size = Size(diameter, diameter),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                // 进度弧末端的柔和光晕
                val tipAngle = Math.toRadians((-90f + animatedProgress * 360f).toDouble())
                val tipX = centerX + radius * kotlin.math.cos(tipAngle).toFloat()
                val tipY = centerY + radius * kotlin.math.sin(tipAngle).toFloat()
                drawCircle(
                    color = progressColor.copy(alpha = 0.3f),
                    radius = strokeWidthPx * 1.5f,
                    center = Offset(tipX, tipY)
                )
                drawCircle(
                    color = progressColor.copy(alpha = 0.15f),
                    radius = strokeWidthPx * 2.5f,
                    center = Offset(tipX, tipY)
                )
            }
        }

        content()
    }
}
