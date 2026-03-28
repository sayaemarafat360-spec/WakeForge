package com.wakeforge.app.presentation.wake_success

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wakeforge.app.core.components.AnimatedCounter
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.presentation.navigation.Route
import kotlinx.coroutines.delay

/**
 * Premium success screen shown after a wake-up mission is completed.
 *
 * Displays a celebration animation background, a large success icon with
 * animated checkmark, streak information (with new record callout), a stats
 * summary card, and a "Back to Home" button.
 *
 * @param navController Navigation controller for returning to Home.
 * @param viewModel     The [WakeSuccessViewModel] backing this screen.
 */
@Composable
fun WakeSuccessScreen(
    navController: NavController,
    viewModel: WakeSuccessViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // ── Animations ────────────────────────────────────────────────────

    // Content scale-in
    val contentScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    // Staggered child reveals
    val iconScale = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val streakAlpha = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    // Checkmark sweep
    val checkmarkSweep = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Overall entrance
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
        contentAlpha.animateTo(1f, animationSpec = tween(300))

        // Icon
        iconScale.animateTo(1f, animationSpec = tween(400))
        delay(200L)
        checkmarkSweep.animateTo(270f, animationSpec = tween(500))

        // Title
        titleAlpha.animateTo(1f, animationSpec = tween(300))
        delay(150L)

        // Streak banner (if applicable)
        if (state.isNewStreakRecord) {
            streakAlpha.animateTo(1f, animationSpec = tween(400))
        }
        delay(200L)

        // Stats card
        cardAlpha.animateTo(1f, animationSpec = tween(400))
        delay(200L)

        // Button
        buttonAlpha.animateTo(1f, animationSpec = tween(300))
    }

    // ── Background ────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0F14),
                        Color(0xFF121821),
                        Color(0xFF0B0F14),
                    ),
                ),
            ),
    ) {
        // Celebration animation background
        SuccessCelebration(isNewRecord = state.isNewStreakRecord)

        // ── Main content ──────────────────────────────────────────────

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Success icon with animated checkmark ──────────────────

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .alpha(iconScale.value),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                ) {
                    // Background circle
                    drawCircle(
                        color = colors.success.copy(alpha = 0.15f),
                        radius = size.minDimension / 2f,
                    )

                    // Animated ring
                    val strokeWidth = 4.dp.toPx()
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

                    // Checkmark path after sweep completes
                    if (checkmarkSweep.value >= 260f) {
                        val checkAlpha = ((checkmarkSweep.value - 260f) / 10f).coerceIn(0f, 1f)
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val tickSize = size.minDimension * 0.22f

                        val checkPath = Path().apply {
                            moveTo(cx - tickSize, cy + tickSize * 0.05f)
                            lineTo(cx - tickSize * 0.3f, cy + tickSize * 0.65f)
                            lineTo(cx + tickSize, cy - tickSize * 0.5f)
                        }
                        drawPath(
                            path = checkPath,
                            color = colors.success.copy(alpha = checkAlpha),
                            style = Stroke(
                                width = strokeWidth * 1.5f,
                                cap = StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── "Wake Up Successful!" text ────────────────────────────

            Text(
                text = "Wake Up Successful!",
                style = typography.headlineLarge.copy(
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(titleAlpha.value),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── New streak record banner ──────────────────────────────

            if (state.isNewStreakRecord) {
                Row(
                    modifier = Modifier
                        .alpha(streakAlpha.value),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Animated fire icon (Canvas)
                    Canvas(
                        modifier = Modifier.size(24.dp),
                    ) {
                        drawFlame(
                            center = Offset(size.width / 2f, size.height * 0.6f),
                            size = size,
                            color = colors.warning,
                        )
                    }

                    Text(
                        text = "New Streak Record!",
                        style = typography.titleLarge.copy(
                            color = colors.warning,
                            fontWeight = FontWeight.Bold,
                        ),
                    )

                    Canvas(
                        modifier = Modifier.size(24.dp),
                    ) {
                        drawFlame(
                            center = Offset(size.width / 2f, size.height * 0.6f),
                            size = size,
                            color = colors.error,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Streak count with animated counter
                AnimatedCounter(
                    targetValue = state.currentStreak,
                    textStyle = typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.warning,
                    ),
                )

                Text(
                    text = "days in a row",
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.alpha(streakAlpha.value),
                )
            } else {
                // Show current streak even if not a record
                Text(
                    text = "Current Streak: ${state.currentStreak} days",
                    style = typography.titleLarge,
                    color = colors.secondaryText,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Stats summary card ────────────────────────────────────

            WFCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha.value),
                cornerRadius = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {

                    // Current Streak
                    StatRow(
                        label = "Current Streak",
                        value = "${state.currentStreak} days",
                        valueColor = colors.primaryAccent,
                    )

                    // Longest Streak
                    StatRow(
                        label = "Longest Streak",
                        value = "${state.longestStreak} days",
                        valueColor = colors.secondaryAccent,
                    )

                    // Mission Type
                    StatRow(
                        label = "Mission",
                        value = formatMissionType(state.missionType),
                        valueColor = colors.primaryText,
                    )

                    // Difficulty
                    StatRow(
                        label = "Difficulty",
                        value = state.difficulty.displayName.replaceFirstChar { it.uppercase() },
                        valueColor = colors.primaryText,
                    )

                    // Snoozes
                    StatRow(
                        label = "Snoozes",
                        value = "${state.snoozeCount}",
                        valueColor = if (state.snoozeCount == 0) colors.success else colors.warning,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Back to Home button ───────────────────────────────────

            Box(modifier = Modifier.alpha(buttonAlpha.value)) {
                WFButton(
                    text = "Back to Home",
                    onClick = {
                        navController.navigate(Route.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    fullWidth = true,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * A single row in the stats summary card.
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
            color = colors.secondaryText,
        )
        Text(
            text = value,
            style = typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = valueColor,
        )
    }
}

/**
 * Formats a [MissionType] into a user-friendly display string.
 */
private fun formatMissionType(type: MissionType): String {
    return when (type) {
        MissionType.MATH -> "Math Challenge"
        MissionType.MEMORY -> "Memory Match"
        MissionType.TYPE_PHRASE -> "Type Phrase"
        MissionType.SHAKE -> "Shake It"
        MissionType.STEP -> "Step Out"
    }
}

/**
 * Draws a simple flame icon on the canvas.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlame(
    center: Offset,
    size: Size,
    color: Color,
) {
    val flameWidth = size.width * 0.4f
    val flameHeight = size.height * 0.7f

    val flamePath = Path().apply {
        // Outer flame
        moveTo(center.x, center.y - flameHeight) // Tip
        cubicTo(
            center.x + flameWidth * 0.6f, center.y - flameHeight * 0.5f,
            center.x + flameWidth * 0.5f, center.y - flameHeight * 0.1f,
            center.x + flameWidth * 0.3f, center.y + flameHeight * 0.15f,
        )
        cubicTo(
            center.x + flameWidth * 0.15f, center.y + flameHeight * 0.35f,
            center.x + flameWidth * 0.05f, center.y + flameHeight * 0.3f,
            center.x, center.y + flameHeight * 0.15f,
        )
        cubicTo(
            center.x - flameWidth * 0.05f, center.y + flameHeight * 0.3f,
            center.x - flameWidth * 0.15f, center.y + flameHeight * 0.35f,
            center.x - flameWidth * 0.3f, center.y + flameHeight * 0.15f,
        )
        cubicTo(
            center.x - flameWidth * 0.5f, center.y - flameHeight * 0.1f,
            center.x - flameWidth * 0.6f, center.y - flameHeight * 0.5f,
            center.x, center.y - flameHeight, // Back to tip
        )
        close()
    }

    drawPath(
        path = flamePath,
        color = color,
    )

    // Inner flame (brighter)
    val innerPath = Path().apply {
        val innerW = flameWidth * 0.4f
        val innerH = flameHeight * 0.55f
        moveTo(center.x, center.y - innerH)
        cubicTo(
            center.x + innerW * 0.5f, center.y - innerH * 0.4f,
            center.x + innerW * 0.35f, center.y,
            center.x + innerW * 0.15f, center.y + innerH * 0.2f,
        )
        cubicTo(
            center.x, center.y + innerH * 0.15f,
            center.x - innerW * 0.15f, center.y + innerH * 0.2f,
            center.x - innerW * 0.35f, center.y,
        )
        cubicTo(
            center.x - innerW * 0.5f, center.y - innerH * 0.4f,
            center.x, center.y - innerH,
        )
        close()
    }

    drawPath(
        path = innerPath,
        color = Color.White.copy(alpha = 0.4f),
    )
}
