package com.wakeforge.app.presentation.stats.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.AnimatedCounter
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Small metric card with a left color accent stripe, icon, animated counter, and label.
 *
 * @param label  Descriptive label (e.g., "Total Snoozes").
 * @param value  Numeric value to display.
 * @param color  Accent color for the left stripe and icon tint.
 * @param icon   Material icon to display.
 * @param modifier Outer modifier.
 */
@Composable
fun StatsCard(
    label: String,
    value: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // Entrance animation
    val offsetY = remember { Animatable(20f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ).let { spec ->
            offsetY.animateTo(0f, spec)
            alpha.animateTo(1f, spec)
        }
    }

    WFCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                AnimatedCounter(
                    targetValue = value,
                    textStyle = typography.headlineMedium.copy(color = colors.primaryText),
                    color = color
                )
                Text(
                    text = label,
                    style = typography.labelMedium,
                    color = colors.secondaryText
                )
            }
        }
    }
}

/**
 * Convenience map of icon names to Material [ImageVector] icons.
 * Used by [StatsCard] when the icon is specified as a string.
 */
object StatsIconMap {
    fun getIcon(name: String): ImageVector = when (name.lowercase()) {
        "fire", "streak", "local_fire_department" -> Icons.Default.LocalFireDepartment
        "alarm", "clock" -> Icons.Default.Alarm
        "snooze", "bedtime" -> Icons.Default.Snooze
        "close", "failure", "cancel" -> Icons.Default.Close
        "star", "best" -> Icons.Default.Star
        "trending", "analytics", "trending_up" -> Icons.Default.TrendingUp
        "walk", "steps", "directions_walk" -> Icons.Default.DirectionsWalk
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Star
    }
}
