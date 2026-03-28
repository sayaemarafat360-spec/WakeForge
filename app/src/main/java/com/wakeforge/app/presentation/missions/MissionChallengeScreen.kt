package com.wakeforge.app.presentation.missions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeforge.app.core.components.WFDialog
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.presentation.navigation.Route

/**
 * Router screen that delegates to the appropriate mission challenge composable
 * based on the current [Mission] type.
 *
 * Displays a top bar with a timer countdown ring and step indicator, then renders
 * the specific mission challenge content below. Handles navigation to [WakeSuccess]
 * on completion or shows a retry dialog on failure.
 *
 * @param navController Navigation controller for screen transitions.
 * @param viewModel     The [MissionViewModel] backing this screen.
 */
@Composable
fun MissionChallengeScreen(
    navController: NavController,
    viewModel: MissionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // ── Completion overlay visibility ─────────────────────────────────

    var showCompletionOverlay by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    // ── Event handling ────────────────────────────────────────────────

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MissionEvent.MissionCompleted -> {
                    showCompletionOverlay = true
                }
                is MissionEvent.MissionFailed -> {
                    viewModel.recordWakeFailure()
                    showFailureDialog = true
                }
                is MissionEvent.StepCompleted -> {
                    viewModel.loadNextStepMission(event.nextMission)
                }
            }
        }
    }

    // ── Cleanup on dispose ───────────────────────────────────────────

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }

    // ── Navigate to WakeSuccess on completion overlay dismiss ─────────

    if (showCompletionOverlay) {
        MissionCompletionOverlay(
            onContinue = {
                showCompletionOverlay = false
                val alarmId = viewModel.getAlarmId()
                val wakeRecordId = java.util.UUID.randomUUID().toString()
                navController.navigate(
                    Route.WakeSuccess.createRoute(alarmId, wakeRecordId)
                ) {
                    popUpTo("alarm_ringing/{alarmId}") { inclusive = true }
                }
            },
        )
        return
    }

    // ── Failure dialog ────────────────────────────────────────────────

    if (showFailureDialog) {
        WFDialog(
            onDismissRequest = {
                showFailureDialog = false
                val alarmId = viewModel.getAlarmId()
                val wakeRecordId = java.util.UUID.randomUUID().toString()
                navController.navigate(
                    Route.WakeSuccess.createRoute(alarmId, wakeRecordId)
                ) {
                    popUpTo("alarm_ringing/{alarmId}") { inclusive = true }
                }
            },
            title = "Time's Up!",
            message = "The mission timer expired. Your wake attempt has been recorded as a failure.",
            confirmText = "Continue",
            onConfirm = {
                showFailureDialog = false
                val alarmId = viewModel.getAlarmId()
                val wakeRecordId = java.util.UUID.randomUUID().toString()
                navController.navigate(
                    Route.WakeSuccess.createRoute(alarmId, wakeRecordId)
                ) {
                    popUpTo("alarm_ringing/{alarmId}") { inclusive = true }
                }
            },
        )
        return
    }

    // ── Loading state ─────────────────────────────────────────────────

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center,
        ) {
            com.wakeforge.app.core.components.WFLoadingIndicator()
        }
        return
    }

    // ── Main layout ───────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {

        // ── Top bar: Timer + Step indicator ───────────────────────────

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // Step indicator
            Text(
                text = "Step ${state.currentStepIndex + 1}/${state.totalSteps}",
                style = typography.labelLarge,
                color = colors.secondaryText,
            )

            // Timer ring
            if (state.mission?.isTimed == true && state.totalTimeMs > 0) {
                TimerCountdownRing(
                    timeRemainingMs = state.timeRemainingMs,
                    totalTimeMs = state.totalTimeMs,
                )
            }
        }

        // ── Mission content ───────────────────────────────────────────

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (val mission = state.mission) {
                is Mission.MathMission -> {
                    MathChallengeScreen(viewModel = viewModel)
                }
                is Mission.MemoryMission -> {
                    MemoryPatternScreen(viewModel = viewModel)
                }
                is Mission.TypePhraseMission -> {
                    TypePhraseScreen(viewModel = viewModel)
                }
                is Mission.ShakeMission -> {
                    ShakeChallengeScreen(viewModel = viewModel)
                }
                is Mission.StepMission -> {
                    StepChallengeScreen(viewModel = viewModel)
                }
                null -> {
                    // Mission not yet loaded
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Preparing challenge...",
                            style = typography.bodyLarge,
                            color = colors.secondaryText,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular timer countdown ring that transitions from green to yellow to red
 * as time runs out.
 */
@Composable
private fun TimerCountdownRing(
    timeRemainingMs: Long,
    totalTimeMs: Long,
) {
    val colors = LocalWakeForgeColors.current

    val fraction = if (totalTimeMs > 0) {
        (timeRemainingMs.toFloat() / totalTimeMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Color: green → yellow → red
    val timerColor by animateColorAsState(
        targetValue = when {
            fraction > 0.5f -> colors.success
            fraction > 0.2f -> colors.warning
            else -> colors.error
        },
        animationSpec = tween(300),
        label = "timerColor",
    )

    // Format time remaining
    val totalSeconds = (timeRemainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ring
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(40.dp),
        ) {
            val strokeWidth = 3.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f

            // Track
            drawCircle(
                color = colors.surfaceVariant,
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            )

            // Progress arc
            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = fraction * 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
        }

        // Time text
        Text(
            text = timeText,
            style = LocalWakeForgeTypography.current.labelLarge.copy(
                color = timerColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
