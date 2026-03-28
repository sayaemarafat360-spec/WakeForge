package com.wakeforge.app.presentation.alarm_ringing

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFLoadingIndicator
import com.wakeforge.app.core.extensions.observeEventsWithLifecycle
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import java.time.format.DateTimeFormatter

/**
 * Full-screen immersive alarm ringing experience.
 *
 * This screen is designed to be impossible to ignore:
 * - Pulsing radial gradient background using [Canvas]
 * - Large pulsing time display
 * - Circular progress ring around the time
 * - Subtle shake animation for urgency
 * - Prominent "Complete Mission" and optional "Snooze" buttons
 *
 * The screen enters immersive mode (no system bars) for maximum impact.
 *
 * @param navController Navigation controller for navigating to the mission challenge.
 * @param viewModel     ViewModel injected by Hilt.
 */
@Composable
fun AlarmRingingScreen(
    navController: NavController,
    viewModel: AlarmRingingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val haptic = LocalHapticFeedback.current

    // Infinite pulse transitions
    val infiniteTransition = rememberInfiniteTransition(label = "alarmPulse")

    // Background pulse — radial gradient alpha oscillates
    val backgroundPulse by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bgPulse",
    )

    // Time scale pulse — slight breathing effect
    val timeScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "timePulse",
    )

    // Ring progress — sweeps around the circular progress indicator
    val ringProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ringProgress",
    )

    // Shake animation for the whole screen
    var shakeOffset by remember { mutableFloatStateOf(0f) }
    val shakePulse by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shake",
    )

    // Trigger haptic feedback periodically
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000L)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Handle navigation events
    viewModel.events.observeEventsWithLifecycle { event ->
        when (event) {
            is AlarmRingingViewModel.AlarmRingingEvent.StartMission -> {
                navController.navigate(
                    "mission_challenge?alarmId=${event.alarmId}&missionType=${event.missionType}&difficulty=${event.escalatedDifficulty}&snoozeCount=${event.snoozeCount}"
                )
            }
            is AlarmRingingViewModel.AlarmRingingEvent.Snooze -> {
                navController.popBackStack()
            }
        }
    }

    // Background color
    val bgColor = Color(0xFF0B0F14)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clipToBounds()
            .graphicsLayer {
                translationX = shakePulse
            },
        contentAlignment = Alignment.Center,
    ) {
        // ── Pulsing radial gradient background ─────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width.coerceAtLeast(size.height) * 0.6f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primaryAccent.copy(alpha = backgroundPulse),
                        colors.primaryAccent.copy(alpha = backgroundPulse * 0.5f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = maxRadius,
                ),
                center = center,
                radius = maxRadius,
            )
        }

        // ── Error state ───────────────────────────────────────────────────
        if (state.errorMessage != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = state.errorMessage ?: "Error",
                    style = typography.bodyLarge,
                    color = colors.error,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                WFButton(
                    text = "Go Back",
                    onClick = { navController.popBackStack() },
                    type = ButtonType.Secondary,
                )
            }
            return@Box
        }

        // ── Loading state ─────────────────────────────────────────────────
        if (state.isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                WFLoadingIndicator(size = 64.dp, color = colors.primaryAccent)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Waking you up...",
                    style = typography.bodyLarge,
                    color = colors.secondaryText,
                )
            }
            return@Box
        }

        // ── Main content ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // ── Circular progress ring + time display ─────────────────────
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Progress ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 3.dp.toPx()
                    val arcSize = size.minDimension - strokeWidth
                    val topLeft = Offset(
                        x = (size.width - arcSize) / 2f,
                        y = (size.height - arcSize) / 2f,
                    )

                    // Track
                    drawArc(
                        color = colors.primaryAccent.copy(alpha = 0.15f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )

                    // Animated sweep
                    drawArc(
                        color = colors.primaryAccent.copy(alpha = 0.6f),
                        startAngle = ringProgress - 90f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                }

                // Time display with pulse
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val timeString = state.currentTime.format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                    )
                    Text(
                        text = timeString,
                        style = typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.graphicsLayer {
                            scaleX = timeScale
                            scaleY = timeScale
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Alarm label ────────────────────────────────────────────────
            val alarmLabel = state.alarm?.label
            if (!alarmLabel.isNullOrBlank()) {
                Text(
                    text = alarmLabel,
                    style = typography.headlineMedium,
                    color = colors.secondaryAccent,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Snooze remaining indicator ────────────────────────────────
            if (state.canSnooze) {
                val remaining = state.maxSnoozeCount - state.snoozeCount
                Text(
                    text = if (remaining > 0) "Snooze: $remaining remaining" else "Last snooze",
                    style = typography.labelMedium,
                    color = colors.warning,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = "No more snoozes — time to wake up!",
                    style = typography.labelMedium,
                    color = colors.error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Action buttons ─────────────────────────────────────────────
            // Complete Mission button — primary CTA
            WFButton(
                text = "Complete Mission",
                onClick = { viewModel.startMission() },
                type = ButtonType.Primary,
                fullWidth = true,
                modifier = Modifier.height(56.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Snooze button — secondary, only when allowed
            if (state.canSnooze) {
                WFButton(
                    text = "Snooze",
                    onClick = { viewModel.snooze() },
                    type = ButtonType.Secondary,
                    fullWidth = true,
                    modifier = Modifier.height(48.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
