package com.wakeforge.app.presentation.missions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Full-screen celebration overlay displayed when a mission step is completed.
 *
 * Features:
 * - Semi-transparent dark scrim.
 * - Canvas-drawn checkmark that progressively draws itself.
 * - Particle burst animation after the checkmark completes.
 * - "Mission Complete!" text with staggered fade-in.
 * - "Continue" button that appears after a 1-second delay.
 * - Auto-dismisses after 5 seconds.
 *
 * @param onContinue Callback invoked when the user taps Continue or auto-dismiss fires.
 */
@Composable
fun MissionCompletionOverlay(onContinue: () -> Unit) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // ── Animation state ────────────────────────────────────────────────

    // Overall scale entrance
    val scaleAnimatable = remember { Animatable(0f) }

    // Checkmark sweep angle: 0f → 270f degrees
    val checkmarkSweep = remember { Animatable(0f) }

    // Text alpha
    val textAlpha = remember { Animatable(0f) }

    // Button alpha
    val buttonAlpha = remember { Animatable(0f) }

    // Particle system
    val particleCount = 30
    val particles = remember {
        List(particleCount) {
            Particle(
                angle = Random.nextFloat() * 360f,
                distance = Random.nextFloat() * 200f + 100f,
                size = Random.nextFloat() * 6f + 2f,
                color = listOf(
                    colors.success,
                    colors.primaryAccent,
                    colors.secondaryAccent,
                    Color(0xFFFFB84D),
                ).random(),
            )
        }
    }
    val particleProgress = remember { Animatable(0f) }
    val particleAlpha = remember { Animatable(1f) }

    // Auto-dismiss timer
    LaunchedEffect(Unit) {
        // 1. Scale-in entrance
        scaleAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )

        // 2. Draw checkmark (600ms)
        checkmarkSweep.animateTo(
            targetValue = 270f,
            animationSpec = tween(durationMillis = 600),
        )

        // 3. Particle burst
        particleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800),
        )

        // 4. Fade out particles
        particleAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600),
        )

        // 5. Show text
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400),
        )

        // 6. Show button (after 1s from start)
        buttonAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300),
        )

        // 7. Auto-dismiss after 5s total
        kotlinx.coroutines.delay(2000L)
        onContinue()
    }

    // ── Layout ─────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.80f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(scaleAnimatable.value.coerceIn(0f, 1f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Checkmark Canvas ───────────────────────────────────────

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
            ) {
                val strokeWidth = 6.dp.toPx()

                // Background circle
                drawCircle(
                    color = colors.success.copy(alpha = 0.15f),
                    radius = (size.minDimension - strokeWidth) / 2f,
                    style = Stroke(width = strokeWidth),
                )

                // Animated checkmark arc
                val sweepRad = (checkmarkSweep.value / 360f) * 2f * kotlin.math.PI.toFloat()
                if (checkmarkSweep.value > 0f) {
                    drawArc(
                        color = colors.success,
                        startAngle = -90f,
                        sweepAngle = checkmarkSweep.value,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                }

                // Draw checkmark tick once sweep reaches ~270°
                if (checkmarkSweep.value >= 260f) {
                    val checkAlpha = ((checkmarkSweep.value - 260f) / 10f).coerceIn(0f, 1f)
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val tickSize = size.minDimension * 0.2f

                    val checkPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - tickSize, cy)
                        lineTo(cx - tickSize * 0.3f, cy + tickSize * 0.6f)
                        lineTo(cx + tickSize, cy - tickSize * 0.5f)
                    }
                    drawPath(
                        path = checkPath,
                        color = colors.success.copy(alpha = checkAlpha),
                        style = Stroke(
                            width = strokeWidth * 1.2f,
                            cap = StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        ),
                    )
                }

                // Particles
                if (particleProgress.value > 0f) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    particles.forEach { particle ->
                        val angleRad = (particle.angle / 180f) * kotlin.math.PI.toFloat()
                        val currentDist = particle.distance * particleProgress.value
                        val px = center.x + cos(angleRad) * currentDist
                        val py = center.y + sin(angleRad) * currentDist
                        drawCircle(
                            color = particle.color.copy(alpha = particleAlpha.value),
                            radius = particle.size.toPx(),
                            center = Offset(px, py),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── "Mission Complete!" text ────────────────────────────────

            Text(
                text = "Mission Complete!",
                style = typography.headlineLarge.copy(
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                modifier = Modifier.alpha(textAlpha.value),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Continue button ─────────────────────────────────────────

            androidx.compose.foundation.layout.Box(
                modifier = Modifier.alpha(buttonAlpha.value),
            ) {
                WFButton(
                    text = "Continue",
                    onClick = onContinue,
                )
            }
        }
    }
}

/**
 * Data class representing a single particle in the burst animation.
 */
private data class Particle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val color: Color,
)
