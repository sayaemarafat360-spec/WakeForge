package com.wakeforge.app.presentation.missions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Math challenge screen: solve arithmetic problems to dismiss the alarm.
 *
 * Shows a large problem display, a numeric input field, and a progress bar
 * indicating how many problems remain. Correct answers trigger a green flash;
 * wrong answers trigger a red shake animation.
 *
 * @param viewModel The [MissionViewModel] backing this screen.
 */
@Composable
fun MathChallengeScreen(viewModel: MissionViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val problem = state.currentProblem
    val mission = state.mission as? com.wakeforge.app.domain.models.Mission.MathMission

    // ── Animations ─────────────────────────────────────────────────────

    // Shake animation on wrong answer
    val shakeOffsetX = remember { Animatable(0f) }

    // Green flash on correct answer
    var flashVisible by remember { mutableStateOf(false) }

    // Trigger shake on incorrect, flash on correct
    LaunchedEffect(state.answerFeedback) {
        when (state.answerFeedback) {
            AnswerFeedback.Incorrect -> {
                shakeOffsetX.animateTo(targetValue = -12f, animationSpec = tween(60))
                shakeOffsetX.animateTo(targetValue = 12f, animationSpec = tween(60))
                shakeOffsetX.animateTo(targetValue = -6f, animationSpec = tween(60))
                shakeOffsetX.animateTo(targetValue = 6f, animationSpec = tween(60))
                shakeOffsetX.animateTo(targetValue = 0f, animationSpec = tween(60))
            }
            AnswerFeedback.Correct -> {
                flashVisible = true
                kotlinx.coroutines.delay(300L)
                flashVisible = false
            }
            AnswerFeedback.Idle -> { /* no-op */ }
        }
    }

    // Auto-focus the input field
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(problem) {
        kotlinx.coroutines.delay(100L)
        focusRequester.requestFocus()
    }

    // ── Border color based on feedback ─────────────────────────────────

    val borderColor by animateColorAsState(
        targetValue = when (state.answerFeedback) {
            AnswerFeedback.Correct -> colors.success
            AnswerFeedback.Incorrect -> colors.error
            AnswerFeedback.Idle -> colors.border
        },
        animationSpec = tween(200),
        label = "answerBorderColor",
    )

    // ── Content ────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Progress indicator ─────────────────────────────────────

            if (mission != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Problem ${state.mathProblemIndex + 1} of ${mission.problems.size}",
                        style = typography.bodyMedium,
                        color = colors.secondaryText,
                    )
                    Text(
                        text = "Attempt ${state.attempts + 1}",
                        style = typography.labelMedium,
                        color = colors.secondaryText,
                    )
                }

                LinearProgressIndicator(
                    progress = {
                        if (mission.problems.isNotEmpty()) {
                            (state.mathProblemIndex.toFloat() + 1f) / mission.problems.size.toFloat()
                        } else 0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = colors.primaryAccent,
                    trackColor = colors.surfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Green flash overlay ────────────────────────────────────

            if (flashVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.success.copy(alpha = 0.15f)),
                )
            }

            // ── Problem display ────────────────────────────────────────

            if (problem != null) {
                Text(
                    text = problem.question,
                    style = typography.displayLarge.copy(
                        color = colors.primaryText,
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "= ?",
                    style = typography.displayLarge.copy(
                        color = colors.primaryAccent,
                        fontSize = 40.sp,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Answer input ───────────────────────────────────────────

            OutlinedTextField(
                value = state.userAnswer,
                onValueChange = { viewModel.updateUserAnswer(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .graphicsLayer { translationX = shakeOffsetX.value }
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Your answer",
                        style = typography.bodyLarge,
                        color = colors.secondaryText.copy(alpha = 0.5f),
                    )
                },
                textStyle = typography.headlineLarge.copy(
                    textAlign = TextAlign.Center,
                    color = colors.primaryText,
                    fontSize = 32.sp,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    cursorColor = colors.primaryAccent,
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedContainerColor = colors.surfaceVariant,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Submit button ──────────────────────────────────────────

            WFButton(
                text = "Submit",
                onClick = { viewModel.submitAnswer() },
                enabled = state.userAnswer.isNotBlank() && state.answerFeedback != AnswerFeedback.Correct,
                fullWidth = true,
            )

            // ── Feedback text ──────────────────────────────────────────

            when (state.answerFeedback) {
                AnswerFeedback.Correct -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Correct!",
                        style = typography.titleLarge,
                        color = colors.success,
                    )
                }
                AnswerFeedback.Incorrect -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Try again",
                        style = typography.titleLarge,
                        color = colors.error,
                    )
                }
                AnswerFeedback.Idle -> { /* no feedback text */ }
            }
        }
    }
}
