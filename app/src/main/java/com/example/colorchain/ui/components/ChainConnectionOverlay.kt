package com.example.colorchain.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.example.colorchain.ui.viewmodel.ConnectionAnimationData

@Composable
fun ChainConnectionOverlay(
    animationData: ConnectionAnimationData?,
    tileCenter: Offset?,
    targetCenter: Offset?,
    modifier: Modifier = Modifier
) {
    if (animationData == null || tileCenter == null || targetCenter == null) return

    val progress = remember(animationData.timestamp) { Animatable(0f) }

    LaunchedEffect(animationData.timestamp) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 160, easing = LinearEasing)
        )
    }

    if (progress.value in 0.01f..0.99f) {
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)
        val lineColor = animationData.color.color

        Canvas(modifier = modifier.fillMaxSize()) {
            val currentBeamEnd = Offset(
                x = tileCenter.x + (targetCenter.x - tileCenter.x) * p,
                y = tileCenter.y + (targetCenter.y - tileCenter.y) * p
            )

            // Outer glow line
            drawLine(
                color = lineColor.copy(alpha = alpha * 0.4f),
                start = tileCenter,
                end = currentBeamEnd,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
            // Core beam
            drawLine(
                color = Color.White.copy(alpha = alpha * 0.9f),
                start = tileCenter,
                end = currentBeamEnd,
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
            // Head particle
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 7f,
                center = currentBeamEnd
            )
        }
    }
}
