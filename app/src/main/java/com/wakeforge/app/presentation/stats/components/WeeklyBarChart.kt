package com.wakeforge.app.presentation.stats.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.domain.models.DailyStats

/**
 * Canvas-rendered stacked bar chart showing weekly wake-up data (Mon–Sun).
 *
 * Each bar is divided into three stacked segments:
 * - **Green** (bottom): successful wake-ups
 * - **Yellow** (middle): snooze events
 * - **Red** (top): failed wake-ups
 *
 * Bars animate to their target height using spring physics.
 * Subtle horizontal grid lines and day labels are drawn below.
 */
@Composable
fun WeeklyBarChart(
    data: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val paddedData = if (data.size < 7) {
        List(7) { index -> data.getOrNull(index) ?: DailyStats(0, index + 2, 0, 0, 0) }
    } else {
        data.take(7)
    }

    val maxTotal = paddedData.maxOf { it.successes + it.failures + it.snoozes }.coerceAtLeast(1)

    // Animate each bar's total height
    val animatedHeights = paddedData.map { dayData ->
        val total = dayData.successes + dayData.failures + dayData.snoozes
        val animated by animateFloatAsState(
            targetValue = if (total > 0) total.toFloat() / maxTotal else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "barHeight_${dayData.date}"
        )
        animated
    }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val chartHeight = 160.dp
    val barWidth = 24.dp
    val barSpacing = 8.dp
    val cornerRadius = 4f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Chart area
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val chartWidth = size.width
            val totalBarsWidth = 7 * barWidth.toPx() + 6 * barSpacing.toPx()
            val startX = (chartWidth - totalBarsWidth) / 2f
            val chartTop = 0f
            val chartBottom = size.height - 24.dp.toPx() // Leave room for labels
            val chartHeightPx = chartBottom - chartTop

            // Draw subtle horizontal grid lines
            for (i in 1..4) {
                val y = chartTop + (chartHeightPx * i / 4f)
                drawLine(
                    color = colors.border.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 0.5f
                )
            }

            // Draw bars
            paddedData.forEachIndexed { index, dayData ->
                val barX = startX + index * (barWidth.toPx() + barSpacing.toPx())
                val animHeight = animatedHeights[index]
                val totalBarHeight = animHeight * chartHeightPx

                if (totalBarHeight > 0f) {
                    val total = (dayData.successes + dayData.failures + dayData.snoozes).toFloat()

                    // Stacked segments (bottom to top: success, snooze, failure)
                    val successHeight = if (total > 0) (dayData.successes / total) * totalBarHeight else 0f
                    val snoozeHeight = if (total > 0) (dayData.snoozes / total) * totalBarHeight else 0f
                    val failureHeight = if (total > 0) (dayData.failures / total) * totalBarHeight else 0f

                    val barBottom = chartBottom

                    // Draw success segment (green, bottom)
                    if (successHeight > 1f) {
                        drawRoundedBar(
                            x = barX,
                            y = barBottom - successHeight,
                            width = barWidth.toPx(),
                            height = successHeight,
                            color = colors.success,
                            cornerRadius = if (snoozeHeight + failureHeight < 1f) cornerRadius else 0f,
                            isTop = snoozeHeight + failureHeight < 1f,
                            isBottom = true
                        )
                    }

                    // Draw snooze segment (yellow, middle)
                    if (snoozeHeight > 1f) {
                        drawRoundedBar(
                            x = barX,
                            y = barBottom - successHeight - snoozeHeight,
                            width = barWidth.toPx(),
                            height = snoozeHeight,
                            color = colors.warning,
                            cornerRadius = 0f,
                            isTop = failureHeight < 1f,
                            isBottom = successHeight < 1f
                        )
                    }

                    // Draw failure segment (red, top)
                    if (failureHeight > 1f) {
                        drawRoundedBar(
                            x = barX,
                            y = barBottom - successHeight - snoozeHeight - failureHeight,
                            width = barWidth.toPx(),
                            height = failureHeight,
                            color = colors.error,
                            cornerRadius = cornerRadius,
                            isTop = true,
                            isBottom = successHeight + snoozeHeight < 1f
                        )
                    }
                } else {
                    // Draw empty bar placeholder
                    drawRoundedBar(
                        x = barX,
                        y = chartBottom - 2.dp.toPx(),
                        width = barWidth.toPx(),
                        height = 2.dp.toPx(),
                        color = colors.border.copy(alpha = 0.4f),
                        cornerRadius = cornerRadius,
                        isTop = true,
                        isBottom = true
                    )
                }

                // Draw day label below bar
                drawContext.canvas.nativeCanvas.apply {
                    val label = dayLabels.getOrNull(index) ?: ""
                    val textX = barX + barWidth.toPx() / 2f
                    val textY = chartBottom + 16.dp.toPx()

                    val paint = android.graphics.Paint().apply {
                        color = colors.secondaryText.hashCode()
                        textSize = 11f * density
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(label, textX, textY, paint)
                }
            }
        }
    }
}

/**
 * Draws a rounded rectangle bar with optional top/bottom corner rounding.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundedBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: androidx.compose.ui.graphics.Color,
    cornerRadius: Float,
    isTop: Boolean,
    isBottom: Boolean
) {
    val path = Path().apply {
        if (isTop && isBottom) {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius),
                    bottomLeftCornerRadius = CornerRadius(cornerRadius),
                    bottomRightCornerRadius = CornerRadius(cornerRadius)
                )
            )
        } else if (isTop) {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius),
                    bottomLeftCornerRadius = CornerRadius(0f),
                    bottomRightCornerRadius = CornerRadius(0f)
                )
            )
        } else if (isBottom) {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(0f),
                    topRightCornerRadius = CornerRadius(0f),
                    bottomLeftCornerRadius = CornerRadius(cornerRadius),
                    bottomRightCornerRadius = CornerRadius(cornerRadius)
                )
            )
        } else {
            addRect(
                left = x,
                top = y,
                right = x + width,
                bottom = y + height
            )
        }
    }
    drawPath(path, color)
}
