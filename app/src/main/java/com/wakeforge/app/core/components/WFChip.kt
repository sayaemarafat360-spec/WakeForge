package com.wakeforge.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.WakeForgeShapes

/**
 * Selectable chip for WakeForge with animated selection state.
 * Used for day-of-week selection, difficulty picker, and filter tags.
 *
 * @param label        Display text.
 * @param selected     Whether the chip is currently selected.
 * @param onClick      Callback invoked on tap.
 * @param modifier     Outer modifier.
 * @param leadingIcon  Optional icon displayed before the label.
 */
@Composable
fun WFChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // Animated colors
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primaryAccent else Color.Transparent,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "chipBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primaryAccent else colors.border,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "chipBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else colors.secondaryText,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "chipContent",
    )

    val shape = WakeForgeShapes.extraSmall

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = modifier
                .clip(shape)
                .background(backgroundColor, shape)
                .border(1.dp, borderColor, shape)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = typography.labelMedium,
                color = contentColor,
            )
        }
    }
}
