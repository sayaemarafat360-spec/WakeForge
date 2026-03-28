package com.wakeforge.app.presentation.stats.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors

/**
 * Canvas-rendered smooth line chart showing streak data over the last 30 days.
 *
 * Features:
 * - **Smooth curve**: Bezier (cubicTo) interpolation between data points.
 * - **Animated line drawing**: The line progressively reveals from left to right.
 * - **Gradient area fill**: Vertical gradient from primaryAccent (top) to transparent (bottom).
 * - **Dot markers**: Small circles at each data point.
 * - **Axis labels**: X-axis labels every 5th day, implicit Y-axis scale.
 */
@Composable
fun StreakLineChart(
    streakData: List<Int>,
    modifier: Modifier = Modifier
) {
    val colors = LocalWakeForgeColors.current

    val paddedData = if (streakData.size < 30) {
        List(30) { index -> streakData.getOrNull(index) ?: 0 }
    } else {
        streakData.take(30)
    }

    val maxValue = paddedData.maxOf { it }.coerceAtLeast(1)

    // Animated drawing progress (0.0 to 1.0)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(streakData) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val paddingStart = 24.dp.toPx()
        val paddingEnd = 16.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val plotWidth = chartWidth - paddingStart - paddingEnd
        val plotHeight = chartHeight - paddingTop - paddingBottom

        val stepX = plotWidth / (paddedData.size - 1).coerceAtLeast(1)

        // Convert data point to canvas coordinates
        fun pointAt(index: Int): Offset {
            val x = paddingStart + index * stepX
            val normalizedValue = paddedData[index].toFloat() / maxValue
            val y = paddingTop + plotHeight * (1f - normalizedValue)
            return Offset(x, y)
        }

        // Draw subtle horizontal grid lines
        for (i in 0..4) {
            val y = paddingTop + (plotHeight * i / 4f)
            drawLine(
                color = colors.border.copy(alpha = 0.3f),
                start = Offset(paddingStart, y),
                end = Offset(chartWidth - paddingEnd, y),
                strokeWidth = 0.5f
            )
        }

        // Y-axis labels
        val yLabels = listOf(maxValue.toString(), (maxValue * 3 / 4).toString(), (maxValue / 2).toString(), (maxValue / 4).toString(), "0")
        drawContext.canvas.nativeCanvas.apply {
            yLabels.forEachIndexed { index, label ->
                val y = paddingTop + (plotHeight * index / 4f)
                val paint = android.graphics.Paint().apply {
                    color = colors.secondaryText.hashCode()
                    textSize = 9f * density
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
                drawText(label, paddingStart - 4.dp.toPx(), y + 3.dp.toPx(), paint)
            }
        }

        // X-axis labels (every 5th day)
        val xLabelPositions = listOf(0, 4, 9, 14, 19, 24, 29)
        val xLabelValues = listOf("1", "5", "10", "15", "20", "25", "30")
        drawContext.canvas.nativeCanvas.apply {
            xLabelPositions.forEachIndexed { index, dataIdx ->
                val point = pointAt(dataIdx)
                val paint = android.graphics.Paint().apply {
                    color = colors.secondaryText.hashCode()
                    textSize = 9f * density
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(xLabelValues[index], point.x, chartHeight - 4.dp.toPx(), paint)
            }
        }

        // Build smooth bezier path through data points
        val smoothPath = Path().apply {
            val points = paddedData.indices.map { pointAt(it) }

            if (points.size >= 2) {
                moveTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]
                    val controlPointOffset = stepX * 0.3f

                    val cp1x = current.x + controlPointOffset
                    val cp1y = current.y
                    val cp2x = next.x - controlPointOffset
                    val cp2y = next.y

                    cubicTo(cp1x, cp1y, cp2x, cp2y, next.x, next.y)
                }
            }
        }

        // Build area fill path (line path + close to bottom)
        val areaPath = Path().apply {
            val points = paddedData.indices.map { pointAt(it) }

            if (points.size >= 2) {
                moveTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]
                    val controlPointOffset = stepX * 0.3f

                    cubicTo(
                        current.x + controlPointOffset, current.y,
                        next.x - controlPointOffset, next.y,
                        next.x, next.y
                    )
                }

                // Close to bottom-right, then bottom-left
                lineTo(points.last().x, paddingTop + plotHeight)
                lineTo(points.first().x, paddingTop + plotHeight)
                close()
            }
        }

        val currentProgress = progress.value

        // Clip drawing to current progress
        drawContext.canvas.save()
        drawContext.canvas.clipRect(
            left = 0f,
            top = 0f,
            right = paddingStart + plotWidth * currentProgress,
            bottom = chartHeight
        )

        // Draw gradient area fill
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.primaryAccent.copy(alpha = 0.25f),
                    colors.primaryAccent.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = paddingTop + plotHeight
            )
        )

        // Draw line
        drawPath(
            path = smoothPath,
            color = colors.primaryAccent,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw dot markers at data points
        paddedData.forEachIndexed { index, value ->
            if (value > 0) {
                val point = pointAt(index)
                drawCircle(
                    color = colors.secondaryAccent,
                    radius = 3.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = colors.cardSurface,
                    radius = 1.5.dp.toPx(),
                    center = point
                )
            }
        }

        drawContext.canvas.restore()
    }
}
