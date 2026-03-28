package com.wakeforge.app.presentation.create_alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.components.WFToggle
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Snooze configuration section with interval presets, max count presets,
 * and a smart escalation toggle.
 *
 * @param snoozeInterval    Current snooze interval in minutes.
 * @param maxSnoozeCount    Current max snooze count.
 * @param smartEscalation   Whether smart escalation is enabled.
 * @param onIntervalChange  Callback when snooze interval changes.
 * @param onMaxCountChange  Callback when max snooze count changes.
 * @param onEscalationToggle Callback when smart escalation is toggled.
 */
@Composable
fun SnoozeSettingsSection(
    snoozeInterval: Int,
    maxSnoozeCount: Int,
    smartEscalation: Boolean,
    onIntervalChange: (Int) -> Unit,
    onMaxCountChange: (Int) -> Unit,
    onEscalationToggle: (Boolean) -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    WFCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Section header
            Text(
                text = "Snooze Settings",
                style = typography.labelMedium,
                color = colors.secondaryText,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Snooze interval
            Text(
                text = "Snooze Interval",
                style = typography.bodyLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1, 5, 10, 15, 30).forEach { interval ->
                    WFChip(
                        label = "${interval}m",
                        selected = snoozeInterval == interval,
                        onClick = { onIntervalChange(interval) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Max snooze count
            Text(
                text = "Max Snooze Count",
                style = typography.bodyLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1, 2, 3, 5, 7, 10).forEach { count ->
                    WFChip(
                        label = count.toString(),
                        selected = maxSnoozeCount == count,
                        onClick = { onMaxCountChange(count) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Escalation toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Escalation",
                        style = typography.bodyLarge,
                        color = colors.primaryText,
                    )
                    Text(
                        text = "Mission difficulty increases with each snooze",
                        style = typography.bodyMedium,
                        color = colors.secondaryText,
                    )
                }
                WFToggle(
                    checked = smartEscalation,
                    onCheckedChange = onEscalationToggle,
                )
            }
        }
    }
}
