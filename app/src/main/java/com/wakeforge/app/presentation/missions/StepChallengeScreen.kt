package com.wakeforge.app.presentation.missions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Step challenge (walking) screen.
 *
 * Displays animated alternating footprints, a circular progress ring showing
 * steps vs target, and a real-time step counter. Falls back to a sensor-unavailable
 * message if the device lacks a step counter.
 *
 * @param viewModel The [MissionViewModel] backing this screen.
 */
@Composable
fun StepChallengeScreen(viewModel: MissionViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val progress = if (state.stepTarget > 0) {
        state.stepProgress.toFloat() / state.stepTarget.toFloat()
    } else 0f

    // ── Alternating footstep animation ────────────────────────────────

    // Track which foot is "active" (alternates with each step)
    val leftFootY = remember { Animatable(0f) }
    val rightFootY = remember { Animatable(0f) }

    var lastStepCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.stepProgress) {
        if (state.stepProgress > lastStepCount) {
            val isLeftStep = state.stepProgress % 2 == 1

            if (isLeftStep) {
                // Left foot steps forward (up)
                leftFootY.animateTo(-12f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f))
                leftFootY.animateTo(0f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f))
            } else {
                // Right foot steps forward
                rightFootY.animateTo(-12f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f))
                rightFootY.animateTo(0f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f))
            }
            lastStepCount = state.stepProgress
        }
    }

    // Idle bounce animation for footprints
    val infiniteTransition = rememberInfiniteTransition(label = "stepIdle")
    val idleBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stepIdleBounce",
    )

    if (!state.stepSensorAvailable) {
        // ── Fallback: sensor not available ────────────────────────────

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Step sensor not available",
                style = typography.headlineMedium,
                color = colors.warning,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your device doesn't have a step counter.\nTry a shake challenge instead!",
                style = typography.bodyLarge,
                color = colors.secondaryText,
            )
        }
        return
    }

    // ── Main content ──────────────────────────────────────────────────

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Footprint icons ───────────────────────────────────────────

        Row(
            modifier = Modifier.height(80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Left footprint
            Canvas(
                modifier = Modifier
                    .size(48.dp)
                    .offset { IntOffset(0, leftFootY.value.toInt()) },
            ) {
                val idleOffset = (idleBounce - 0.5f) * 2f
                drawFootprint(
                    center = Offset(center.x, center.y + idleOffset * density),
                    size = size * 0.8f,
                    color = if (state.stepProgress % 2 == 1) colors.primaryAccent else colors.secondaryText.copy(alpha = 0.4f),
                )
            }

            // Right footprint
            Canvas(
                modifier = Modifier
                    .size(48.dp)
                    .offset { IntOffset(0, rightFootY.value.toInt()) },
            ) {
                val idleOffset = (idleBounce - 0.5f) * -2f
                drawFootprint(
                    center = Offset(center.x, center.y + idleOffset * density),
                    size = size * 0.8f,
                    color = if (state.stepProgress % 2 == 0 && state.stepProgress > 0) colors.primaryAccent else colors.secondaryText.copy(alpha = 0.4f),
                    isRight = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Progress ring ─────────────────────────────────────────────

        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier.size(180.dp),
            ) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f

                // Track
                drawCircle(
                    color = colors.surfaceVariant,
                    radius = radius,
                    style = Stroke(width = strokeWidth),
                )

                // Progress arc
                if (progress > 0f) {
                    drawArc(
                        color = colors.secondaryAccent,
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

            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${state.stepProgress}",
                    style = typography.displayLarge.copy(
                        color = colors.primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                    ),
                )
                Text(
                    text = "of ${state.stepTarget} steps",
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Percentage ────────────────────────────────────────────────

        Text(
            text = "${(progress * 100).toInt()}%",
            style = typography.titleLarge,
            color = colors.secondaryAccent,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ── Instructions ──────────────────────────────────────────────

        Text(
            text = "Walk to complete the challenge",
            style = typography.bodyLarge,
            color = colors.secondaryText,
        )
    }
}

/**
 * Draws a simplified footprint icon on the canvas.
 *
 * @param center The center of the footprint.
 * @param size   The bounding size.
 * @param color  The fill color.
 * @param isRight If true, the footprint is mirrored horizontally.
 */
private fun DrawScope.drawFootprint(
    center: Offset,
    size: androidx.compose.ui.geometry.Size,
    color: Color,
    isRight: Boolean = false,
) {
    val footWidth = size.width * 0.5f
    val footHeight = size.height * 0.65f
    val toeRadius = size.width * 0.09f
    val strokeWidth = 1.5.dp.toPx()

    val footLeft = if (isRight) center.x + size.width * 0.05f else center.x - footWidth / 2f
    val footTop = center.y - footHeight * 0.2f

    // Main sole (oval)
    drawOval(
        color = color,
        topLeft = Offset(footLeft, footTop + footHeight * 0.3f),
        size = androidx.compose.ui.geometry.Size(footWidth, footHeight * 0.7f),
        style = Stroke(width = strokeWidth),
    )

    // Toes (5 small circles at top)
    val toeCount = 5
    val toeSpacing = footWidth / (toeCount + 1)
    for (i in 0 until toeCount) {
        val toeX = footLeft + toeSpacing * (i + 1)
        val toeY = footTop + footHeight * 0.25f
        // Slightly vary toe size (big toe is larger)
        val isBigToe = (isRight && i == toeCount - 1) || (!isRight && i == 0)
        val radius = if (isBigToe) toeRadius * 1.3f else toeRadius
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(toeX, toeY),
            style = Stroke(width = strokeWidth),
        )
    }
}
