package com.wakeforge.app.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.StrokeCap
import androidx.compose.ui.unit.dp
import com.wakeforge.app.R
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.PrimaryAccent
import com.wakeforge.app.core.theme.SecondaryAccent
import com.wakeforge.app.core.theme.Success
import com.wakeforge.app.core.theme.Warning

/**
 * Data class representing a single onboarding page.
 *
 * @property title The page title string resource ID.
 * @property description The page description string resource ID.
 * @property illustration Composable function that renders the animated illustration.
 */
data class OnboardingPage(
    val titleRes: Int,
    val descriptionRes: Int,
    val illustration: @Composable () -> Unit,
)

/**
 * Returns the list of all onboarding pages with their animated Canvas illustrations.
 */
fun getOnboardingPages(): List<OnboardingPage> = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_title_1,
        descriptionRes = R.string.onboarding_description_1,
        illustration = { SunriseIllustration() },
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_2,
        descriptionRes = R.string.onboarding_description_2,
        illustration = { MissionIconsIllustration() },
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_3,
        descriptionRes = R.string.onboarding_description_3,
        illustration = { FireIllustration() },
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_4,
        descriptionRes = R.string.onboarding_description_4,
        illustration = { ShieldIllustration() },
    ),
)

// ──────────────────────────────────────────────────────────────────────────────
// Illustration Composables
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Animated sunrise illustration — an arc that sweeps upward with a gradient fill.
 */
@Composable
private fun SunriseIllustration() {
    val colors = LocalWakeForgeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "sunrise")

    // Sweep angle animates from 0 to 180 degrees
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sunriseSweep",
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .size(200.dp),
    ) {
        val cx = size.width / 2f
        val cy = size.height * 0.55f
        val arcRadius = size.width * 0.35f

        // Sun circle
        drawCircle(
            color = colors.primaryAccent.copy(alpha = 0.15f),
            radius = arcRadius * 0.25f,
            center = Offset(cx, cy - arcRadius * 0.3f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PrimaryAccent.copy(alpha = 0.6f),
                    Warning.copy(alpha = 0.3f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy - arcRadius * 0.3f),
                radius = arcRadius * 0.35f,
            ),
            radius = arcRadius * 0.35f,
            center = Offset(cx, cy - arcRadius * 0.3f),
        )

        // Sunrise gradient arc
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(
                    PrimaryAccent.copy(alpha = 0.6f),
                    Warning.copy(alpha = 0.4f),
                    SecondaryAccent.copy(alpha = 0.2f),
                ),
                start = Offset(cx - arcRadius, cy),
                end = Offset(cx + arcRadius, cy - arcRadius),
            ),
            startAngle = 180f,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(cx - arcRadius, cy - arcRadius),
            size = Size(arcRadius * 2f, arcRadius * 2f),
        )

        // Horizon line
        drawLine(
            color = colors.border,
            start = Offset(cx - arcRadius * 1.2f, cy),
            end = Offset(cx + arcRadius * 1.2f, cy),
            strokeWidth = 2.dp.toPx(),
        )
    }
}

/**
 * Animated mission icons illustration — three circles that bounce with staggered delays.
 */
@Composable
private fun MissionIconsIllustration() {
    val colors = LocalWakeForgeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "missionIcons")

    // Three bouncing circles with staggered animation delays
    val bounce1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                delayMillis = 0,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounce1",
    )
    val bounce2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                delayMillis = 150,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounce2",
    )
    val bounce3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                delayMillis = 300,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounce3",
    )

    val offsets = listOf(bounce1, bounce2, bounce3)
    val circleColors = listOf(colors.primaryAccent, colors.secondaryAccent, colors.success)
    val icons = listOf("⊕", "△", "○") // Represented as shapes drawn on Canvas

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .size(200.dp),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val spacing = size.width * 0.22f
        val circleRadius = size.width * 0.12f

        offsets.forEachIndexed { index, yOffset ->
            val xOffset = cx + (index - 1) * spacing
            val yFinal = cy + yOffset

            // Glow behind circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        circleColors[index].copy(alpha = 0.2f),
                        Color.Transparent,
                    ),
                    center = Offset(xOffset, yFinal),
                    radius = circleRadius * 1.5f,
                ),
                center = Offset(xOffset, yFinal),
                radius = circleRadius * 1.5f,
            )

            // Circle
            drawCircle(
                color = circleColors[index].copy(alpha = 0.8f),
                radius = circleRadius,
                center = Offset(xOffset, yFinal),
            )

            // Icon symbol inside circle
            when (index) {
                0 -> {
                    // Math symbol — plus
                    drawLine(
                        color = Color.White,
                        start = Offset(xOffset - circleRadius * 0.4f, yFinal),
                        end = Offset(xOffset + circleRadius * 0.4f, yFinal),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(xOffset, yFinal - circleRadius * 0.4f),
                        end = Offset(xOffset, yFinal + circleRadius * 0.4f),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                1 -> {
                    // Memory symbol — grid dots
                    val dotR = circleRadius * 0.12f
                    val gridSpacing = circleRadius * 0.35f
                    for (r in -1..1) {
                        for (c in -1..1) {
                            if ((r + c) % 2 == 0) {
                                drawCircle(
                                    color = Color.White,
                                    radius = dotR,
                                    center = Offset(
                                        xOffset + c * gridSpacing,
                                        yFinal + r * gridSpacing,
                                    ),
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Step symbol — footsteps
                    drawCircle(
                        color = Color.White,
                        radius = circleRadius * 0.2f,
                        center = Offset(xOffset - circleRadius * 0.2f, yFinal - circleRadius * 0.1f),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = circleRadius * 0.2f,
                        center = Offset(xOffset + circleRadius * 0.2f, yFinal + circleRadius * 0.15f),
                    )
                }
            }
        }
    }
}

/**
 * Animated fire illustration — a flame drawn with a Path that has flickering alpha.
 */
@Composable
private fun FireIllustration() {
    val colors = LocalWakeForgeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "fire")

    // Flickering alpha effect
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fireFlicker",
    )

    // Secondary flicker at a different rate
    val innerFlicker by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 450,
                delayMillis = 100,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fireInnerFlicker",
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .size(200.dp),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val flameHeight = size.height * 0.45f
        val flameWidth = size.width * 0.3f

        // Outer flame path
        val outerFlame = Path().apply {
            moveTo(cx, cy - flameHeight)
            cubicTo(
                cx + flameWidth * 0.4f, cy - flameHeight * 0.6f,
                cx + flameWidth * 0.55f, cy - flameHeight * 0.1f,
                cx + flameWidth * 0.4f, cy + flameHeight * 0.3f,
            )
            cubicTo(
                cx + flameWidth * 0.3f, cy + flameHeight * 0.5f,
                cx + flameWidth * 0.1f, cy + flameHeight * 0.55f,
                cx, cy + flameHeight * 0.4f,
            )
            cubicTo(
                cx - flameWidth * 0.1f, cy + flameHeight * 0.55f,
                cx - flameWidth * 0.3f, cy + flameHeight * 0.5f,
                cx - flameWidth * 0.4f, cy + flameHeight * 0.3f,
            )
            cubicTo(
                cx - flameWidth * 0.55f, cy - flameHeight * 0.1f,
                cx - flameWidth * 0.4f, cy - flameHeight * 0.6f,
                cx, cy - flameHeight,
            )
            close()
        }

        // Inner flame path (smaller, lighter)
        val innerFlame = Path().apply {
            val scale = 0.45f
            moveTo(cx, cy - flameHeight * scale * 0.5f)
            cubicTo(
                cx + flameWidth * scale * 0.5f, cy - flameHeight * scale * 0.2f,
                cx + flameWidth * scale * 0.6f, cy + flameHeight * scale * 0.3f,
                cx + flameWidth * scale * 0.25f, cy + flameHeight * scale * 0.7f,
            )
            lineTo(cx - flameWidth * scale * 0.25f, cy + flameHeight * scale * 0.7f)
            cubicTo(
                cx - flameWidth * scale * 0.6f, cy + flameHeight * scale * 0.3f,
                cx - flameWidth * scale * 0.5f, cy - flameHeight * scale * 0.2f,
                cx, cy - flameHeight * scale * 0.5f,
            )
            close()
        }

        // Glow behind flame
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.primaryAccent.copy(alpha = 0.2f * flickerAlpha),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = flameWidth * 1.8f,
            ),
            center = Offset(cx, cy),
            radius = flameWidth * 1.8f,
        )

        // Outer flame with gradient
        drawPath(
            path = outerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.primaryAccent.copy(alpha = flickerAlpha),
                    Warning.copy(alpha = flickerAlpha * 0.8f),
                    Warning.copy(alpha = flickerAlpha * 0.5f),
                ),
                startY = cy - flameHeight,
                endY = cy + flameHeight * 0.5f,
            ),
        )

        // Inner flame (lighter)
        drawPath(
            path = innerFlame,
            color = Color.White.copy(alpha = innerFlicker),
        )
    }
}

/**
 * Animated shield with checkmark illustration — a shield path with a scale animation.
 */
@Composable
private fun ShieldIllustration() {
    val colors = LocalWakeForgeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "shield")

    // Shield pulses with scale-like alpha animation
    val shieldAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shieldAlpha",
    )

    // Checkmark drawing progress
    val checkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = 400,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "checkProgress",
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .size(200.dp),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val shieldWidth = size.width * 0.32f
        val shieldHeight = size.height * 0.38f

        // Shield path
        val shieldPath = Path().apply {
            moveTo(cx, cy - shieldHeight)
            lineTo(cx + shieldWidth, cy - shieldHeight * 0.65f)
            lineTo(cx + shieldWidth, cy + shieldHeight * 0.1f)
            cubicTo(
                cx + shieldWidth, cy + shieldHeight * 0.5f,
                cx + shieldWidth * 0.3f, cy + shieldHeight,
                cx, cy + shieldHeight * 1.1f,
            )
            cubicTo(
                cx - shieldWidth * 0.3f, cy + shieldHeight,
                cx - shieldWidth, cy + shieldHeight * 0.5f,
                cx - shieldWidth, cy + shieldHeight * 0.1f,
            )
            lineTo(cx - shieldWidth, cy - shieldHeight * 0.65f)
            close()
        }

        // Glow behind shield
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.success.copy(alpha = 0.15f * shieldAlpha),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = shieldWidth * 1.8f,
            ),
            center = Offset(cx, cy),
            radius = shieldWidth * 1.8f,
        )

        // Shield body
        drawPath(
            path = shieldPath,
            color = colors.success.copy(alpha = 0.3f * shieldAlpha),
        )
        drawPath(
            path = shieldPath,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
            color = colors.success.copy(alpha = 0.8f * shieldAlpha),
        )

        // Checkmark inside shield (animated draw progress)
        if (checkProgress > 0f) {
            val checkPath = Path().apply {
                val checkScale = shieldWidth * 0.5f
                moveTo(cx - checkScale * 0.45f, cy)
                lineTo(cx - checkScale * 0.1f, cy + checkScale * 0.35f)
                lineTo(cx + checkScale * 0.5f, cy - checkScale * 0.3f)
            }

            // We approximate progress by controlling the stroke alpha
            drawPath(
                path = checkPath,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round,
                ),
                color = Color.White.copy(alpha = checkProgress * shieldAlpha),
            )
        }
    }
}
