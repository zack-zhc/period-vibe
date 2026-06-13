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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 256.dp,
    strokeWidth: Dp = 12.dp,
    backgroundColor: Color,
    progressColor: Color,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "progressAnimation"
    )

    val gradientColors = remember(progressColor) {
        listOf(
            progressColor.copy(alpha = 0.6f),
            progressColor,
            progressColor.copy(alpha = 0.8f)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background track with subtle gradient
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            // Subtle background ring with gradient
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.3f),
                        backgroundColor.copy(alpha = 0.5f),
                        backgroundColor.copy(alpha = 0.3f)
                    )
                ),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(
                    width = strokeWidthPx * 0.6f,
                    cap = StrokeCap.Round
                )
            )
        }

        // Progress arc with gradient
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            // Draw progress arc with gradient
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = gradientColors,
                        center = Offset(centerX, centerY)
                    ),
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
            }
        }

        // Subtle glow effect on the progress tip
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            if (animatedProgress > 0f) {
                // Calculate the tip position
                val tipAngle = Math.toRadians((-90f + animatedProgress * 360f).toDouble())
                val tipX = centerX + radius * kotlin.math.cos(tipAngle).toFloat()
                val tipY = centerY + radius * kotlin.math.sin(tipAngle).toFloat()

                // Draw a subtle glow at the tip
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
