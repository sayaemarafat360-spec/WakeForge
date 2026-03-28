package com.wakeforge.app.presentation.alarms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.R
import com.wakeforge.app.core.components.TimeDisplay
import com.wakeforge.app.core.components.TimeDisplayStyle
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFToggle
import com.wakeforge.app.core.extensions.pressEffect
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.PrimaryAccent
import com.wakeforge.app.core.theme.SecondaryAccent
import com.wakeforge.app.core.theme.Success
import com.wakeforge.app.core.theme.Warning
import com.wakeforge.app.core.theme.Error
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.models.DayOfWeek
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType

/**
 * Premium alarm card for displaying a single alarm in the alarm list.
 *
 * Features:
 * - Left accent stripe (2dp) colored by mission difficulty
 * - Large time display (titleLarge) in primary text color
 * - AM/PM badge if using 12-hour format
 * - Alarm label in secondary text
 * - Repeat days shown as a row of small circle dots (filled = active)
 * - Mission type chip
 * - Difficulty dots indicator
 * - WFToggle on the right side
 * - Subtle shadow/elevation
 * - Card entrance animation with slide + fade
 *
 * @param alarm The alarm data to display.
 * @param onToggle Callback invoked when the toggle state changes (alarmId, newIsActive).
 * @param onClick Callback invoked when the card is clicked (alarmId).
 */
@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (String, Boolean) -> Unit,
    onClick: (String) -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // Difficulty color for the left accent stripe
    val accentColor = when (alarm.difficulty) {
        MissionDifficulty.TRIVIAL -> Success
        MissionDifficulty.EASY -> Success
        MissionDifficulty.MEDIUM -> SecondaryAccent
        MissionDifficulty.HARD -> Warning
        MissionDifficulty.EXTREME -> Error
    }

    // Mission type display name
    val missionName = when (alarm.missionType) {
        MissionType.MATH -> stringResource(id = R.string.mission_math)
        MissionType.MEMORY -> stringResource(id = R.string.mission_memory)
        MissionType.TYPE_PHRASE -> stringResource(id = R.string.mission_type_phrase)
        MissionType.SHAKE -> stringResource(id = R.string.mission_shake)
        MissionType.STEP -> stringResource(id = R.string.mission_step)
    }

    // AM/PM text
    val amPmText = if (alarm.hour < 12) "AM" else "PM"

    WFCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressEffect()
            .clickable { onClick(alarm.id) },
        elevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Left accent stripe (2dp width)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(
                        androidx.compose.foundation.layout.IntrinsicSize.Min
                    )
                    .background(accentColor),
            )

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
            ) {
                // Row: Time + AM/PM badge + Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Time display
                    TimeDisplay(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        style = TimeDisplayStyle.Medium,
                        color = if (alarm.isActive) colors.primaryText else colors.secondaryText,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // AM/PM badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (alarm.isActive) PrimaryAccent.copy(alpha = 0.15f)
                                else colors.border.copy(alpha = 0.3f),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = amPmText,
                            style = typography.labelMedium.copy(fontSize = 9.sp),
                            color = if (alarm.isActive) PrimaryAccent else colors.secondaryText,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Alarm label
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = typography.bodyMedium,
                        color = if (alarm.isActive) colors.primaryText else colors.secondaryText.copy(alpha = 0.7f),
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: repeat days + mission chip + difficulty dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Repeat days as small circle dots
                    if (alarm.repeatDays.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val allDays = DayOfWeek.entries
                            allDays.forEach { day ->
                                val isActive = alarm.repeatDays.contains(day)
                                Canvas(
                                    modifier = Modifier.size(8.dp),
                                ) {
                                    if (isActive) {
                                        drawCircle(
                                            color = if (alarm.isActive) PrimaryAccent else colors.secondaryText.copy(alpha = 0.5f),
                                            radius = 3.dp.toPx(),
                                            center = Offset(size.width / 2f, size.height / 2f),
                                        )
                                    } else {
                                        drawCircle(
                                            color = colors.border,
                                            radius = 3.dp.toPx(),
                                            center = Offset(size.width / 2f, size.height / 2f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = 1.dp.toPx(),
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        // "Once" label
                        Text(
                            text = stringResource(id = R.string.alarm_repeat_once),
                            style = typography.labelMedium,
                            color = colors.secondaryText.copy(alpha = 0.6f),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Mission type chip
                    Box(
                        modifier = Modifier
                            .clip(
                                androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (alarm.isActive) PrimaryAccent.copy(alpha = 0.1f)
                                else colors.border.copy(alpha = 0.3f),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = missionName,
                            style = typography.labelMedium.copy(fontSize = 9.sp),
                            color = if (alarm.isActive) PrimaryAccent else colors.secondaryText.copy(alpha = 0.7f),
                        )
                    }

                    // Difficulty dots
                    DifficultyDots(
                        difficulty = alarm.difficulty,
                        isEnabled = alarm.isActive,
                    )
                }
            }

            // Toggle on the right side
            WFToggle(
                checked = alarm.isActive,
                onCheckedChange = { isActive -> onToggle(alarm.id, isActive) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 14.dp),
            )
        }
    }
}

/**
 * Difficulty dots indicator for alarm cards.
 */
@Composable
private fun DifficultyDots(
    difficulty: MissionDifficulty,
    isEnabled: Boolean,
) {
    val colors = LocalWakeForgeColors.current
    val totalDots = 5
    val filledDots = difficulty.tier

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..totalDots) {
            val isFilled = i <= filledDots
            val dotColor = when {
                !isEnabled -> colors.border.copy(alpha = 0.4f)
                isFilled && i <= 2 -> Success
                isFilled && i <= 3 -> SecondaryAccent
                isFilled && i <= 4 -> Warning
                isFilled -> Error
                else -> colors.border
            }

            Canvas(
                modifier = Modifier.size(6.dp),
            ) {
                drawCircle(
                    color = dotColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
        }
    }
}
