package com.example.colorchain.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorchain.data.model.GameColor

@Composable
fun TargetColorSwatch(
    targetColor: GameColor,
    modifier: Modifier = Modifier,
    swatchSize: Dp = 80.dp,
    showLabel: Boolean = true,
    showSymbol: Boolean = false,
    isUrgent: Boolean = false
) {
    val animatedBgColor by animateColorAsState(
        targetValue = targetColor.color,
        animationSpec = tween(durationMillis = 150),
        label = "targetColorAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUrgent) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isUrgent) 300 else 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swatchPulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TARGET COLOR",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(swatchSize)
                .shadow(elevation = 10.dp, shape = CircleShape, spotColor = animatedBgColor)
                .clip(CircleShape)
                .background(animatedBgColor)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showSymbol) {
                Text(
                    text = targetColor.symbolChar,
                    color = targetColor.onColor.copy(alpha = 0.7f),
                    fontSize = (swatchSize.value * 0.45f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = targetColor.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.2.sp
            )
        }
    }
}
