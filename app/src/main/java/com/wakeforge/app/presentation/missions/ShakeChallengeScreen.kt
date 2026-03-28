package com.wakeforge.app.presentation.missions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import kotlin.random.Random

/**
 * Shake device challenge screen.
 *
 * Displays a large phone icon that visually shakes when the real device shakes,
 * a circular progress ring showing shake count vs target, and a real-time counter.
 * Ripple effects burst outward with each detected shake.
 *
 * @param viewModel The [MissionViewModel] backing this screen.
 */
@Composable
fun ShakeChallengeScreen(viewModel: MissionViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val progress = if (state.shakeTarget > 0) {
        state.shakeProgress.toFloat() / state.shakeTarget.toFloat()
    } else 0f

    // ── Phone vibration animation ─────────────────────────────────────

    val phoneShakeX = remember { Animatable(0f) }
    val phoneShakeY = remember { Animatable(0f) }

    // Trigger shake animation on each progress change
    LaunchedEffect(state.shakeProgress) {
        if (state.shakeProgress > 0) {
            val intensity = 8f
            phoneShakeX.animateTo(
                targetValue = (Random.nextFloat() * 2 - 1) * intensity,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
            )
            phoneShakeY.animateTo(
                targetValue = (Random.nextFloat() * 2 - 1) * intensity,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
            )
            phoneShakeX.animateTo(
                0f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
            )
            phoneShakeY.animateTo(
                0f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
            )
        }
    }

    // ── Ripple animations ─────────────────────────────────────────────

    val rippleCount = 4
    val rippleAlphas = remember { List(rippleCount) { Animatable(0f) } }
    val rippleScales = remember { List(rippleCount) { Animatable(0f) } }
    var rippleIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.shakeProgress) {
        if (state.shakeProgress > 0) {
            val idx = rippleIndex % rippleCount
            rippleAlphas[idx].snapTo(0.5f)
            rippleScales[idx].snapTo(0.2f)
            rippleAlphas[idx].animateTo(0f, animationSpec = tween(800))
            rippleScales[idx].animateTo(1f, animationSpec = tween(800))
            rippleIndex++
        }
    }

    // Idle micro-vibration animation
    val infiniteTransition = rememberInfiniteTransition(label = "idleShake")
    val idleVibration by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "idleVibration",
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Central ring area ─────────────────────────────────────────

        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Ripple circles
            repeat(rippleCount) { index ->
                Canvas(
                    modifier = Modifier.size(220.dp),
                ) {
                    val scale = rippleScales[index].value
                    val maxRadius = size.minDimension / 2f
                    val radius = maxRadius * scale
                    drawCircle(
                        color = colors.primaryAccent.copy(alpha = rippleAlphas[index].value),
                        radius = radius,
                        center = center,
                    )
                }
            }

            // ── Phone icon with shake offset ──────────────────────────

            Canvas(
                modifier = Modifier.size(100.dp),
            ) {
                val idleOffset = (idleVibration - 0.5f) * 3f
                val cx = size.width / 2f + phoneShakeX.value * density + idleOffset * density
                val cy = size.height / 2f + phoneShakeY.value * density + idleOffset * density

                drawPhoneIcon(
                    centerX = cx,
                    centerY = cy,
                    size = size * 0.6f,
                    color = colors.primaryAccent,
                )
            }

            // ── Progress ring ─────────────────────────────────────────

            Canvas(
                modifier = Modifier.size(220.dp),
            ) {
                val strokeWidth = 8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f

                // Background track
                drawCircle(
                    color = colors.surfaceVariant,
                    radius = radius,
                    style = Stroke(width = strokeWidth),
                )

                // Progress arc
                if (progress > 0f) {
                    drawArc(
                        color = colors.primaryAccent,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Progress text ─────────────────────────────────────────────

        Text(
            text = "${state.shakeProgress} / ${state.shakeTarget} shakes",
            style = typography.headlineLarge.copy(
                color = colors.primaryText,
                fontWeight = FontWeight.Bold,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = typography.titleLarge,
            color = colors.primaryAccent,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ── Instructions ──────────────────────────────────────────────

        Text(
            text = "Shake your device vigorously!",
            style = typography.bodyLarge,
            color = colors.secondaryText,
        )
    }
}

/**
 * Draws a simplified phone icon on the canvas.
 */
private fun DrawScope.drawPhoneIcon(
    centerX: Float,
    centerY: Float,
    size: Size,
    color: androidx.compose.ui.graphics.Color,
) {
    val phoneWidth = size.width * 0.55f
    val phoneHeight = size.height * 0.85f
    val cornerRadius = size.width * 0.08f
    val strokeWidth = 2.dp.toPx()

    val left = centerX - phoneWidth / 2f
    val top = centerY - phoneHeight / 2f

    // Phone body outline
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(phoneWidth, phoneHeight),
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = strokeWidth),
    )

    // Screen area fill
    val screenPadding = strokeWidth * 2
    drawRoundRect(
        color = color.copy(alpha = 0.15f),
        topLeft = Offset(left + screenPadding, top + screenPadding * 2),
        size = Size(
            width = phoneWidth - screenPadding * 2,
            height = phoneHeight * 0.7f,
        ),
        cornerRadius = CornerRadius(cornerRadius * 0.5f),
    )

    // Home button
    drawCircle(
        color = color.copy(alpha = 0.4f),
        radius = size.width * 0.04f,
        center = Offset(centerX, top + phoneHeight - screenPadding * 2.5f),
    )

    // Speaker slit
    drawRoundRect(
        color = color.copy(alpha = 0.4f),
        topLeft = Offset(centerX - size.width * 0.08f, top + screenPadding * 0.8f),
        size = Size(size.width * 0.16f, strokeWidth * 0.8f),
        cornerRadius = CornerRadius(strokeWidth * 0.4f),
    )
}
