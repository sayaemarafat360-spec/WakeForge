package com.wakeforge.app.core.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Animated number counter that smoothly rolls from a previous value to a new
 * target value using spring physics.
 *
 * When [targetValue] changes, the displayed number animates from the old value
 * to the new one, giving a satisfying "counting up" effect.
 *
 * @param targetValue  The number to display after animation completes.
 * @param modifier     Outer modifier.
 * @param durationMs   Not used directly (spring physics is time-independent),
 *                     but kept for API compatibility with tween-based designs.
 * @param textStyle    Override text style; defaults to [LocalWakeForgeTypography]'s displayLarge.
 * @param color        Override text color; defaults to palette's primaryAccent.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    durationMs: Int = 800,
    textStyle: TextStyle? = null,
    color: Color? = null,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val resolvedStyle = textStyle ?: typography.displayLarge.copy(
        fontWeight = FontWeight.Bold,
    )
    val resolvedColor = color ?: colors.primaryAccent

    // Animatable holds the current display value
    val animatable = remember { Animatable(targetValue.toFloat()) }

    // Whenever targetValue changes, animate to the new value
    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = animatable.value.toInt().toString(),
            style = resolvedStyle.copy(color = resolvedColor),
        )
    }
}
