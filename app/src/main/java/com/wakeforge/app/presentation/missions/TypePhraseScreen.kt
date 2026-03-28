package com.wakeforge.app.presentation.missions

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Type the exact phrase challenge screen.
 *
 * Displays a target phrase the user must type. Real-time character comparison shows
 * correct characters in green and incorrect in red. An accuracy bar and character
 * counter provide progress feedback. After 60% of the timer elapses, a first-letter
 * hint appears.
 *
 * @param viewModel The [MissionViewModel] backing this screen.
 */
@Composable
fun TypePhraseScreen(viewModel: MissionViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val targetPhrase = state.targetPhrase
    val typedPhrase = state.typedPhrase

    // Accuracy percentage (how many characters match up to typed length)
    val correctChars = typedPhrase.zip(targetPhrase).count { (typed, target) -> typed == target }.toFloat()
    val accuracy = if (typedPhrase.isNotEmpty()) {
        (correctChars / targetPhrase.length.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Auto-focus
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100L)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Target phrase card ─────────────────────────────────────────

        com.wakeforge.app.core.components.WFCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            cornerRadius = 16.dp,
        ) {
            Text(
                text = "Type this phrase:",
                style = typography.labelMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = targetPhrase,
                style = typography.headlineMedium.copy(
                    color = colors.primaryText,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
        }

        // ── Hint ───────────────────────────────────────────────────────

        if (state.showHint && targetPhrase.isNotEmpty()) {
            Text(
                text = "Hint: starts with '${targetPhrase.first()}'",
                style = typography.labelMedium,
                color = colors.warning,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        // ── Character comparison display ───────────────────────────────

        CharacterComparisonRow(
            targetPhrase = targetPhrase,
            typedPhrase = typedPhrase,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
                .padding(12.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Progress bar ───────────────────────────────────────────────

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${typedPhrase.length}/${targetPhrase.length} characters",
                style = typography.labelMedium,
                color = colors.secondaryText,
            )
            Text(
                text = "${(accuracy * 100).toInt()}% accurate",
                style = typography.labelMedium,
                color = if (accuracy >= 0.95f) colors.success else colors.secondaryText,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { accuracy },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                accuracy >= 0.95f -> colors.success
                accuracy >= 0.7f -> colors.warning
                else -> colors.error
            },
            trackColor = colors.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Input field ────────────────────────────────────────────────

        OutlinedTextField(
            value = typedPhrase,
            onValueChange = { viewModel.updateTypedText(it) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            singleLine = false,
            maxLines = 3,
            placeholder = {
                Text(
                    text = "Start typing...",
                    style = typography.bodyLarge,
                    color = colors.secondaryText.copy(alpha = 0.5f),
                )
            },
            textStyle = typography.bodyLarge.copy(
                color = colors.primaryText,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryAccent,
                unfocusedBorderColor = colors.border,
                cursorColor = colors.primaryAccent,
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
            ),
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Submit button ──────────────────────────────────────────────

        WFButton(
            text = "Submit",
            onClick = { viewModel.updateTypedText(typedPhrase) },
            enabled = typedPhrase.length == targetPhrase.length && typedPhrase.isNotEmpty(),
            fullWidth = true,
        )
    }
}

/**
 * Displays the target phrase character-by-character with color-coded comparison.
 *
 * - Characters that match: green.
 * - Characters that differ: red.
 * - Untyped characters: dimmed secondary text.
 * - Current expected position: underline indicator.
 */
@Composable
private fun CharacterComparisonRow(
    targetPhrase: String,
    typedPhrase: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWakeForgeColors.current

    // We display the full target phrase, character by character
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
    ) {
        targetPhrase.forEachIndexed { index, char ->
            val isTyped = index < typedPhrase.length
            val isCorrect = isTyped && typedPhrase[index] == char
            val isCurrentPosition = index == typedPhrase.length
            val isNextExpected = index == typedPhrase.length && typedPhrase.length < targetPhrase.length

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(32.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        text = char.toString(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = when {
                                isCorrect -> colors.success
                                isTyped -> colors.error
                                else -> colors.secondaryText.copy(alpha = 0.3f)
                            },
                        ),
                    )

                    // Cursor underline at next expected position
                    if (isNextExpected) {
                        Spacer(
                            modifier = Modifier
                                .width(10.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(colors.primaryAccent),
                        )
                    }
                }
            }
        }
    }
}
