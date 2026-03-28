package com.wakeforge.app.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.R
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.EmptyStateIllustrations
import com.wakeforge.app.core.components.TimeDisplay
import com.wakeforge.app.core.components.TimeDisplayStyle
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.components.WFEmptyState
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
import java.util.Calendar

/**
 * Premium card showing the next upcoming alarm.
 *
 * Features:
 * - Large time display (Large if < 6 hours away, Medium otherwise)
 * - Alarm label text
 * - Time-until countdown text (e.g., "in 7h 23m")
 * - Repeat days shown as WFChips
 * - Mission type badge chip
 * - Difficulty indicator (colored dots)
 * - Subtle gradient background
 * - Press effect that navigates to the Alarms tab
 * - Empty state when no alarm exists
 *
 * @param alarm The next alarm to display, or null if no alarm is set.
 * @param timeUntil Formatted countdown string, or null.
 * @param is24Hour Whether to use 24-hour time format.
 * @param onNavigateToAlarms Callback when card is pressed.
 * @param onCreateAlarm Callback when the create alarm button is pressed.
 */
@Composable
fun NextAlarmCard(
    alarm: Alarm?,
    timeUntil: String?,
    is24Hour: Boolean = false,
    onNavigateToAlarms: () -> Unit = {},
    onCreateAlarm: () -> Unit = {},
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    if (alarm == null) {
        // Empty state when no alarm is set
        WFEmptyState(
            icon = {
                EmptyStateIllustrations.NoAlarms(
                    modifier = Modifier.size(100.dp),
                )
            },
            title = stringResource(id = R.string.no_next_alarm),
            subtitle = stringResource(id = R.string.empty_no_alarms_description),
            actionLabel = stringResource(id = R.string.create_alarm),
            onAction = onCreateAlarm,
        )
        return
    }

    // Determine display size based on time until alarm
    val hoursUntil = parseHoursFromCountdown(timeUntil)
    val timeStyle = if (hoursUntil != null && hoursUntil < 6) {
        TimeDisplayStyle.Large
    } else {
        TimeDisplayStyle.Medium
    }

    // Difficulty color mapping
    val difficultyColor = when (alarm.difficulty) {
        MissionDifficulty.TRIVIAL -> Success
        MissionDifficulty.EASY -> Success
        MissionDifficulty.MEDIUM -> SecondaryAccent
        MissionDifficulty.HARD -> Warning
        MissionDifficulty.EXTREME -> Error
    }

    // Mission type display name
    val missionDisplayName = when (alarm.missionType) {
        MissionType.MATH -> stringResource(id = R.string.mission_math)
        MissionType.MEMORY -> stringResource(id = R.string.mission_memory)
        MissionType.TYPE_PHRASE -> stringResource(id = R.string.mission_type_phrase)
        MissionType.SHAKE -> stringResource(id = R.string.mission_shake)
        MissionType.STEP -> stringResource(id = R.string.mission_step)
    }

    WFCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressEffect()
            .clickable { onNavigateToAlarms() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // Header: "Next Alarm" label
            Text(
                text = stringResource(id = R.string.home_next_alarm_label),
                style = typography.labelMedium,
                color = colors.secondaryText,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time display
            TimeDisplay(
                hour = alarm.hour,
                minute = alarm.minute,
                is24Hour = is24Hour,
                style = timeStyle,
                color = colors.primaryText,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Time until alarm + label row
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (timeUntil != null) {
                    Text(
                        text = timeUntil,
                        style = typography.bodyMedium,
                        color = colors.secondaryAccent,
                    )
                }

                if (alarm.label.isNotBlank()) {
                    if (timeUntil != null) {
                        Text(
                            text = " · ",
                            style = typography.bodyMedium,
                            color = colors.secondaryText,
                        )
                    }
                    Text(
                        text = alarm.label,
                        style = typography.bodyMedium,
                        color = colors.secondaryText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Repeat days as WFChips
            if (alarm.repeatDays.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val allDays = DayOfWeek.entries
                    allDays.forEach { day ->
                        val isActive = alarm.repeatDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) PrimaryAccent.copy(alpha = 0.15f)
                                    else Color.Transparent,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = day.abbreviation.first().toString(),
                                style = typography.labelMedium.copy(fontSize = 10.sp),
                                color = if (isActive) colors.primaryAccent
                                else colors.secondaryText.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: mission type chip + difficulty dots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Mission type badge (small chip-like)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.primaryAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = missionDisplayName,
                        style = typography.labelMedium,
                        color = colors.primaryAccent,
                    )
                }

                // Difficulty dots indicator
                DifficultyDots(difficulty = alarm.difficulty)
            }
        }
    }
}

/**
 * Difficulty indicator rendered as a row of colored dots.
 */
@Composable
private fun DifficultyDots(difficulty: MissionDifficulty) {
    val totalDots = 5
    val filledDots = difficulty.tier

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..totalDots) {
            val isFilled = i <= filledDots
            val dotColor = when {
                isFilled && i <= 1 -> Success
                isFilled && i <= 2 -> Success
                isFilled && i <= 3 -> SecondaryAccent
                isFilled && i <= 4 -> Warning
                isFilled -> Error
                else -> com.wakeforge.app.core.theme.LocalWakeForgeColors.current.border
            }

            Canvas(
                modifier = Modifier.size(8.dp),
            ) {
                drawCircle(
                    color = dotColor,
                    radius = 3.dp.toPx(),
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
        }
    }
}

/**
 * Parses hours from a countdown string like "in 7h 23m" or "in 45m".
 * Returns null if the countdown cannot be parsed.
 */
private fun parseHoursFromCountdown(timeUntil: String?): Int? {
    if (timeUntil == null) return null
    val match = Regex("""(\d+)h""").find(timeUntil)
    return match?.groupValues?.get(1)?.toIntOrNull()
}
