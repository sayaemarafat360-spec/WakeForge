package com.wakeforge.app.presentation.create_alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.domain.models.DayOfWeek
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import androidx.compose.material3.Text

/**
 * Section for selecting which days of the week the alarm repeats on.
 *
 * Displays a row of 7 day-of-week chips (Mon-Sun) and quick-action buttons
 * for "Every Day", "Weekdays", "Weekends", and "Clear".
 *
 * @param selectedDays    Currently selected repeat days.
 * @param onDayToggle     Callback when a day chip is tapped.
 * @param onSetAll        Callback to select all days.
 * @param onClearAll      Callback to clear all selections.
 * @param onSetWeekdays   Callback to select Mon-Fri.
 * @param onSetWeekends   Callback to select Sat-Sun.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepeatDaysSection(
    selectedDays: List<DayOfWeek>,
    onDayToggle: (DayOfWeek) -> Unit,
    onSetAll: () -> Unit,
    onClearAll: () -> Unit,
    onSetWeekdays: () -> Unit,
    onSetWeekends: () -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    WFCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Section label
            Text(
                text = "Repeat",
                style = typography.labelMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Day-of-week chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DayOfWeek.entries.forEach { day ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        WFChip(
                            label = day.abbreviation,
                            selected = selectedDays.contains(day),
                            onClick = { onDayToggle(day) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick action buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                WFChip(
                    label = "Every Day",
                    selected = selectedDays.size == 7,
                    onClick = onSetAll,
                )
                WFChip(
                    label = "Weekdays",
                    selected = selectedDays.containsAll(
                        listOf(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI)
                    ) && selectedDays.size == 5,
                    onClick = onSetWeekdays,
                )
                WFChip(
                    label = "Weekends",
                    selected = selectedDays.containsAll(
                        listOf(DayOfWeek.SAT, DayOfWeek.SUN)
                    ) && selectedDays.size == 2,
                    onClick = onSetWeekends,
                )
                WFChip(
                    label = "Clear",
                    selected = false,
                    onClick = onClearAll,
                )
            }
        }
    }
}
