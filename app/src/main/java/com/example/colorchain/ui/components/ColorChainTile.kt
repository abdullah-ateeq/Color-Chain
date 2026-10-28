package com.example.colorchain.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorchain.data.model.ColorTile
import kotlin.math.roundToInt

@Composable
fun ColorChainTile(
    tile: ColorTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWrongTapped: Boolean = false,
    isCorrectTapped: Boolean = false,
    showSymbol: Boolean = false,
    enabled: Boolean = true
) {
    val shakeOffset = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isWrongTapped) {
        if (isWrongTapped) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -16f at 50
                    16f at 100
                    -12f at 150
                    12f at 200
                    -8f at 250
                    8f at 300
                    0f at 350
                }
            )
        }
    }

    LaunchedEffect(isCorrectTapped) {
        if (isCorrectTapped) {
            scaleAnim.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            )
        }
    }

    val cornerRadius = 14.dp

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .scale(scaleAnim.value)
            .padding(3.dp)
            .shadow(
                elevation = if (isCorrectTapped) 8.dp else 3.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = tile.color.color.copy(alpha = 0.4f),
                spotColor = tile.color.color
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(tile.color.color)
            .border(
                width = if (isWrongTapped) 3.dp else if (isCorrectTapped) 2.5.dp else 1.5.dp,
                color = when {
                    isWrongTapped -> Color.White
                    isCorrectTapped -> Color.White.copy(alpha = 0.9f)
                    else -> Color.White.copy(alpha = 0.22f)
                },
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White)
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (showSymbol) {
            Text(
                text = tile.color.symbolChar,
                color = tile.color.onColor.copy(alpha = 0.75f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
