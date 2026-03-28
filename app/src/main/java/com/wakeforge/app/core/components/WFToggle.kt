package com.wakeforge.app.core.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors

/**
 * Premium toggle switch for WakeForge with smooth thumb slide animation.
 *
 * @param checked         Current state.
 * @param onCheckedChange Callback invoked when the user toggles.
 * @param modifier        Outer modifier.
 * @param enabled         Whether the toggle is interactive.
 */
@Composable
fun WFToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalWakeForgeColors.current

    // Resolve colors
    val trackColor = if (checked) colors.toggleActive else colors.toggleInactive
    val thumbColor = if (enabled) Color.White else Color.White.copy(alpha = 0.6f)
    val trackBorderColor = if (!enabled) colors.border.copy(alpha = 0.3f) else Color.Transparent

    // Dimensions
    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val thumbPadding = (trackHeight - thumbSize) / 2

    // Animate thumb offset
    val targetOffsetX = if (checked) (trackWidth - thumbSize - thumbPadding) else thumbPadding
    val animatedOffset by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 400f,
        ),
        label = "toggleThumb",
    )

    // Animate track color
    val animatedTrackColor by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 400f,
        ),
        label = "toggleTrack",
    )

    val resolvedTrackColor = if (animatedTrackColor > 0.5f) colors.toggleActive else colors.toggleInactive

    val trackShape = RoundedCornerShape(trackHeight / 2)

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(trackShape)
            .background(resolvedTrackColor)
            .border(1.dp, trackBorderColor, trackShape)
            .then(
                if (enabled) {
                    Modifier.clickable { onCheckedChange(!checked) }
                } else {
                    Modifier
                },
            )
            .padding(thumbPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .offset(x = animatedOffset - thumbPadding)
                .clip(CircleShape)
                .background(thumbColor),
        )
    }
}
