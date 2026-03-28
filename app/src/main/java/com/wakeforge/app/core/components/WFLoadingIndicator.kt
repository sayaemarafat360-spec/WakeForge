package com.wakeforge.app.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors

/**
 * Custom loading indicator using an infinitely rotating arc drawn on Canvas.
 * Uses the WakeForge primary accent color for brand consistency.
 *
 * @param modifier Outer modifier.
 * @param size     Diameter of the indicator.
 * @param color    Override arc color; defaults to palette's primaryAccent.
 */
@Composable
fun WFLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color? = null,
) {
    val colors = LocalWakeForgeColors.current
    val resolvedColor = color ?: colors.primaryAccent

    val infiniteTransition = rememberInfiniteTransition(label = "loadingRotation")

    // Rotation angle — full 360° loop
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loadingRotationAngle",
    )

    // Arc sweep — sweeps from 0° to 270° then resets (creates the spinning effect)
    val sweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loadingSweep",
    )

    val trackColor = resolvedColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val arcSize = size.minDimension
            val strokeWidth = arcSize * 0.1f
            val topLeft = Offset(
                x = (size.width - arcSize) / 2f,
                y = (size.height - arcSize) / 2f,
            )

            // Track (background circle)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                ),
            )

            // Spinning arc
            drawArc(
                color = resolvedColor,
                startAngle = rotation,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                ),
            )
        }
    }
}
