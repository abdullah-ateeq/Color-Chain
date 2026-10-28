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
import com.example.colorchain.data.model.GameColor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val startX: Float,
    val startY: Float,
    val angle: Double,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun ParticleBurstCanvas(
    modifier: Modifier = Modifier,
    triggerKey: Any? = null,
    maxParticles: Int = 28,
    colors: List<Color> = listOf(
        Color(0xFFE53935),
        Color(0xFFFB8C00),
        Color(0xFFFDD835),
        Color(0xFF43A047),
        Color(0xFF1E88E5),
        Color(0xFF8E24AA)
    )
) {
    val progress = remember { Animatable(0f) }

    val particles = remember(triggerKey) {
        val rand = Random(System.currentTimeMillis())
        List(maxParticles) {
            val angle = rand.nextDouble(0.0, 2 * Math.PI)
            val speed = rand.nextFloat() * 220f + 80f
            val radius = rand.nextFloat() * 4f + 3f
            val color = colors[rand.nextInt(colors.size)]
            Particle(0f, 0f, angle, speed, radius, color)
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey != null) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = LinearEasing)
            )
        }
    }

    if (progress.value in 0.001f..0.999f) {
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)

        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            for (particle in particles) {
                val distance = particle.speed * p
                val gravity = 90f * p * p
                val x = centerX + (cos(particle.angle).toFloat() * distance)
                val y = centerY + (sin(particle.angle).toFloat() * distance) + gravity

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.radius * (1f - p * 0.5f),
                    center = Offset(x, y)
                )
            }
        }
    }
}
