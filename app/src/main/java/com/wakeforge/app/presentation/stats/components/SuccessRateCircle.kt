package com.wakeforge.app.presentation.stats.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Canvas-rendered circular progress indicator showing a success rate percentage.
 *
 * Features:
 * - **Background track**: Semi-transparent circle in surfaceVariant color.
 * - **Foreground arc**: Animated sweep in success green, starting from the top (−90°).
 * - **Animated sweep**: Uses spring physics for a natural reveal.
 * - **Center text**: Large percentage number with a "success" label beneath.
 *
 * @param rate     Success rate from 0.0 to 1.0.
 * @param size     Diameter of the circle.
 * @param modifier Outer modifier.
 */
@Composable
fun SuccessRateCircle(
    rate: Float,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val clampedRate = rate.coerceIn(0f, 1f)
    val targetSweep = clampedRate * 360f

    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "successRateSweep"
    )

    val strokeWidth = 12.dp

    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val arcSize = size.toPx() - strokeWidth.toPx()
                val topLeft = Offset(
                    x = strokeWidth.toPx() / 2f,
                    y = strokeWidth.toPx() / 2f
                )
                val arcDimension = Size(arcSize, arcSize)

                // Background track circle
                drawArc(
                    color = colors.surfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcDimension,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Foreground progress arc
                if (animatedSweep > 0.5f) {
                    drawArc(
                        color = colors.success,
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcDimension,
                        style = Stroke(
                            width = strokeWidth.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(clampedRate * 100).toInt()}%",
                style = typography.headlineMedium,
                color = colors.primaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "success",
                style = typography.labelMedium,
                color = colors.secondaryText
            )
        }
    }
}
