package com.automate.app.ui

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    val angle: Float, val speed: Float, val size: Float,
    val color: Color, val spin: Float, val delay: Float
)

private val palette = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
    Color(0xFF4D96FF), Color(0xFFB36BFF), Color(0xFFFF8FD0)
)

/** Bumps of [trigger] fire a short confetti burst. 0 means "never fired". */
@Composable
fun ConfettiOverlay(trigger: Int) {
    if (trigger == 0) return
    val progress = remember(trigger) { Animatable(0f) }
    val particles = remember(trigger) {
        val rnd = Random(trigger)
        List(80) {
            Particle(
                angle = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 400f + rnd.nextFloat() * 1100f,
                size = 9f + rnd.nextFloat() * 11f,
                color = palette[rnd.nextInt(palette.size)],
                spin = rnd.nextFloat() * 720f - 360f,
                delay = rnd.nextFloat() * 0.15f
            )
        }
    }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(1900, easing = LinearEasing))
    }
    if (progress.value >= 1f) return
    Canvas(Modifier.fillMaxSize()) {
        val t = progress.value
        val ox = size.width / 2f
        val oy = size.height * 0.35f
        particles.forEach { p ->
            val tt = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (tt > 0f) {
                val x = ox + cos(p.angle) * p.speed * tt * 0.55f
                val y = oy + sin(p.angle) * p.speed * tt * 0.55f + 1100f * tt * tt
                rotate(p.spin * tt, pivot = Offset(x, y)) {
                    drawRect(
                        p.color.copy(alpha = (1f - tt).coerceIn(0f, 1f)),
                        topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
