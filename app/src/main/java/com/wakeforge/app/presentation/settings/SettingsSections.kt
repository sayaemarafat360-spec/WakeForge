package com.wakeforge.app.presentation.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.WFToggle
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Section header for grouping related settings.
 *
 * @param title Section title text.
 */
@Composable
fun SettingsSectionHeader(title: String) {
    val typography = LocalWakeForgeTypography.current
    val colors = LocalWakeForgeColors.current

    Text(
        text = title.uppercase(),
        style = typography.labelMedium,
        color = colors.secondaryText,
        modifier = Modifier.padding(
            start = 4.dp,
            top = 8.dp,
            bottom = 4.dp
        )
    )
}

/**
 * A single settings row with label on the left, optional value in the middle,
 * and an optional trailing composable (e.g., toggle, chevron) on the right.
 *
 * @param label     Setting label text displayed on the left.
 * @param value     Optional value text shown to the right of the label.
 * @param onClick   Optional callback when the row is tapped. Shows a chevron when non-null.
 * @param trailing  Optional composable placed at the trailing end (replaces value + chevron).
 * @param modifier  Outer modifier.
 */
@Composable
fun SettingsRow(
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(clickableModifier)
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label (left side, takes remaining space)
        Text(
            text = label,
            style = typography.bodyLarge,
            color = colors.primaryText,
            modifier = Modifier.weight(1f)
        )

        // Trailing content
        if (trailing != null) {
            trailing()
        } else if (value != null) {
            Text(
                text = value,
                style = typography.bodyMedium,
                color = colors.secondaryText
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.secondaryText.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.secondaryText.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Settings row variant with a [WFToggle] on the trailing side.
 *
 * @param label           Setting label text.
 * @param subtitle        Optional secondary description below the label.
 * @param checked         Current toggle state.
 * @param onCheckedChange Callback when toggle state changes.
 */
@Composable
fun SettingsToggleRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label + optional subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.primaryText
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = typography.labelMedium,
                    color = colors.secondaryText
                )
            }
        }

        // Toggle
        WFToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Settings row variant with a [Slider] control below the label.
 *
 * @param label       Setting label text.
 * @param value       Current slider value.
 * @param onValueChange Callback when slider value changes.
 * @param range       Range of valid values.
 * @param steps       Number of discrete steps (0 = continuous).
 * @param modifier    Outer modifier.
 */
@Composable
fun SettingsSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    modifier: Modifier = Modifier
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.primaryText
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = typography.labelMedium,
                color = colors.secondaryText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = colors.primaryAccent,
                activeTrackColor = colors.primaryAccent,
                inactiveTrackColor = colors.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
