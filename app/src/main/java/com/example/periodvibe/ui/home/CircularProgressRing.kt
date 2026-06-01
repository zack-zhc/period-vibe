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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
        // 内发光背景层 - 使用模糊效果
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    renderEffect = BlurEffect(
                        radiusX = 8.dp.toPx(),
                        radiusY = 8.dp.toPx(),
                        edgeTreatment = TileMode.Decal
                    )
                    alpha = 0.6f
                }
        ) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            // 发光的背景圆环
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidthPx * 1.5f)
            )

            // 发光的进度弧
            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx * 1.5f, cap = StrokeCap.Round),
                size = Size(diameter, diameter),
                topLeft = Offset(centerX - radius, centerY - radius)
            )
        }

        // 主圆环层
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.toPx() - strokeWidthPx
            val radius = diameter / 2f
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f

            // 内层柔和光晕
            drawCircle(
                color = progressColor.copy(alpha = 0.08f),
                radius = radius + strokeWidthPx * 0.3f,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidthPx * 0.5f)
            )

            // Draw background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidthPx)
            )

            // 进度条内侧微妙高光
            drawCircle(
                color = progressColor.copy(alpha = 0.15f),
                radius = radius - strokeWidthPx * 0.15f,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidthPx * 0.3f)
            )

            // Draw progress arc
            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = Size(diameter, diameter),
                topLeft = Offset(centerX - radius, centerY - radius)
            )

            // 进度弧内侧高光
            drawArc(
                color = progressColor.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx * 0.4f, cap = StrokeCap.Round),
                size = Size(diameter - strokeWidthPx * 0.6f, diameter - strokeWidthPx * 0.6f),
                topLeft = Offset(centerX - radius + strokeWidthPx * 0.3f, centerY - radius + strokeWidthPx * 0.3f)
            )
        }

        content()
    }
}
