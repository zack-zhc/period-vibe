package com.example.periodvibe.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    strokeWidth: Dp = 8.dp,
    backgroundColor: Color,
    progressColor: Color,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            // Draw background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(width = strokeWidthPx)
            )

            // Draw progress arc
            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                topLeft = androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius
                )
            )
        }

        content()
    }
}
