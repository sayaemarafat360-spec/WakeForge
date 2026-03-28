package com.wakeforge.app.presentation.wake_success

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Canvas-based success celebration animation.
 *
 * Features:
 * - Expanding circle from center (primary accent, fading alpha).
 * - 50 particles that explode outward with random angles, distances, and sizes.
 * - Colors cycle through success, primary, secondary, and warning.
 * - Firework effect: 5 bursts at staggered delays (0, 200, 400, 600, 800ms).
 * - Optional streak fire animation for new records (flickering flame).
 *
 * @param isNewRecord If true, adds a fire/flame flicker animation overlay.
 */
@Composable
fun SuccessCelebration(isNewRecord: Boolean = false) {
    val colors = LocalWakeForgeColors.current

    val particleCount = 50
    val particles = remember {
        List(particleCount) {
            CelebrationParticle(
                angle = Random.nextFloat() * 360f,
                distance = Random.nextFloat() * 200f + 100f,
                size = Random.nextFloat() * 6f + 2f,
                color = listOf(
                    colors.success,
                    colors.primaryAccent,
                    colors.secondaryAccent,
                    colors.warning,
                ).random(),
                delayMs = (it / 10) * 200L, // Stagger particles in groups
            )
        }
    }

    // ── Animation state ───────────────────────────────────────────────

    val circleExpansion = remember { Animatable(0f) }
    val circleAlpha = remember { Animatable(0.3f) }
    val particleProgress = remember { Animatable(0f) }
    val particleAlpha = remember { Animatable(1f) }

    // Firework burst state: 5 bursts
    val burstCount = 5
    val burstProgresses = remember { List(burstCount) { Animatable(0f) } }
    val burstAlphas = remember { List(burstCount) { Animatable(0f) } }

    // Fire flicker for streak records
    val fireFlicker = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Expanding circle
        kotlinx.coroutines.launch {
            circleExpansion.animateTo(1f, animationSpec = tween(1000))
        }
        kotlinx.coroutines.launch {
            circleAlpha.animateTo(0f, animationSpec = tween(1200))
        }

        // 2. Particle explosion
        kotlinx.coroutines.delay(200L)
        particleProgress.animateTo(1f, animationSpec = tween(1000))
        particleAlpha.animateTo(0f, animationSpec = tween(800, delayMillis = 600))

        // 3. Firework bursts at staggered delays
        for (i in 0 until burstCount) {
            kotlinx.coroutines.launch {
                kotlinx.coroutines.delay(i * 200L)
                burstProgresses[i].animateTo(1f, animationSpec = tween(600))
                burstAlphas[i].animateTo(0f, animationSpec = tween(400, delayMillis = 200))
            }
        }

        // 4. Fire flicker for new records (continuous)
        if (isNewRecord) {
            kotlinx.coroutines.launch {
                fireFlicker.animateTo(1f, animationSpec = tween(300))
                infiniteRepeatable(
                    animation = tween(150),
                    repeatMode = RepeatMode.Reverse,
                )
                while (true) {
                    fireFlicker.animateTo(
                        targetValue = if (fireFlicker.value < 0.5f) 1f else 0.3f,
                        animationSpec = tween(100 + Random.nextLong(100)),
                    )
                }
            }
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // ── Expanding circle ──────────────────────────────────────────

        val maxCircleRadius = size.minDimension * 0.8f
        val currentRadius = maxCircleRadius * circleExpansion.value
        if (circleAlpha.value > 0.01f) {
            drawCircle(
                color = colors.primaryAccent.copy(alpha = circleAlpha.value),
                radius = currentRadius,
                center = Offset(cx, cy),
            )
        }

        // ── Particles ─────────────────────────────────────────────────

        if (particleProgress.value > 0f) {
            particles.forEach { particle ->
                if (particle.delayMs > 0) return@forEach // Skip delayed particles (handled below)
                val angleRad = (particle.angle / 180f) * kotlin.math.PI.toFloat()
                val dist = particle.distance * particleProgress.value
                val px = cx + cos(angleRad) * dist
                val py = cy + sin(angleRad) * dist
                drawCircle(
                    color = particle.color.copy(alpha = particleAlpha.value * 0.8f),
                    radius = particle.size.toPx(),
                    center = Offset(px, py),
                )
            }
        }

        // ── Firework bursts ───────────────────────────────────────────

        val burstColors = listOf(colors.success, colors.primaryAccent, colors.secondaryAccent, colors.warning)
        for (i in 0 until burstCount) {
            if (burstProgresses[i].value > 0f && burstAlphas[i].value > 0.01f) {
                val burstCx = cx + (Random.nextFloat() - 0.5f) * size.width * 0.6f
                val burstCy = cy + (Random.nextFloat() - 0.5f) * size.height * 0.4f
                val burstColor = burstColors[i % burstColors.size]

                // Draw burst rays
                val rayCount = 8
                for (j in 0 until rayCount) {
                    val rayAngle = (j.toFloat() / rayCount) * 360f
                    val rayRad = (rayAngle / 180f) * kotlin.math.PI.toFloat()
                    val rayLen = 40f * burstProgresses[i].value
                    val rx = burstCx + cos(rayRad) * rayLen
                    val ry = burstCy + sin(rayRad) * rayLen
                    drawCircle(
                        color = burstColor.copy(alpha = burstAlphas[i].value * 0.6f),
                        radius = 3.dp.toPx(),
                        center = Offset(rx, ry),
                    )
                }

                // Burst center glow
                drawCircle(
                    color = burstColor.copy(alpha = burstAlphas[i].value * 0.3f),
                    radius = 20.dp.toPx() * burstProgresses[i].value,
                    center = Offset(burstCx, burstCy),
                )
            }
        }

        // ── Streak fire overlay ───────────────────────────────────────

        if (isNewRecord && fireFlicker.value > 0.01f) {
            // Flickering orange/red glow at bottom
            val flameHeight = size.height * 0.3f * fireFlicker.value
            val gradient = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    colors.warning.copy(alpha = fireFlicker.value * 0.15f),
                    colors.error.copy(alpha = fireFlicker.value * 0.08f),
                ),
                startY = size.height - flameHeight,
                endY = size.height,
            )
            drawRect(
                brush = gradient,
                size = size,
            )
        }
    }
}

/**
 * Data class representing a single celebration particle.
 */
private data class CelebrationParticle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val color: Color,
    val delayMs: Long = 0L,
)
