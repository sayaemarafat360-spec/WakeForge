package com.wakeforge.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Empty state placeholder with an illustration area, title, subtitle,
 * and optional action button.
 *
 * Used when a list has no items (no alarms, no history, no missions completed).
 *
 * @param icon         Composable icon or illustration displayed above the text.
 * @param title        Primary title text.
 * @param subtitle     Secondary description text.
 * @param actionLabel  Label for the optional action button; `null` hides it.
 * @param onAction     Callback for the action button; `null` hides the button.
 * @param modifier     Outer modifier.
 */
@Composable
fun WFEmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Illustration / Icon area
        icon()

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = title,
            style = typography.headlineMedium,
            color = colors.primaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Optional action button
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            WFButton(
                text = actionLabel,
                onClick = onAction,
                type = ButtonType.Primary,
            )
        }
    }
}

/**
 * Pre-built empty state illustration composables for common scenarios.
 */
object EmptyStateIllustrations {

    /**
     * A moon and stars illustration for "no alarms" state.
     */
    @Composable
    fun NoAlarms(modifier: Modifier = Modifier) {
        val colors = LocalWakeForgeColors.current
        Canvas(
            modifier = modifier.size(120.dp),
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Moon (circle with a bite taken out)
            val moonRadius = size.width * 0.18f
            drawCircle(
                color = colors.secondaryText.copy(alpha = 0.3f),
                radius = moonRadius,
                center = Offset(cx, cy - moonRadius * 0.5f),
            )
            // "Bite" — circle offset to create crescent
            drawCircle(
                color = colors.background,
                radius = moonRadius * 0.85f,
                center = Offset(cx + moonRadius * 0.5f, cy - moonRadius * 0.8f),
            )

            // Stars
            val starColor = colors.primaryAccent.copy(alpha = 0.4f)
            drawStar(cx - size.width * 0.3f, cy - size.height * 0.2f, size.width * 0.04f, starColor)
            drawStar(cx + size.width * 0.3f, cy - size.height * 0.3f, size.width * 0.03f, starColor)
            drawStar(cx - size.width * 0.15f, cy + size.height * 0.1f, size.width * 0.025f, starColor)
            drawStar(cx + size.width * 0.2f, cy + size.height * 0.05f, size.width * 0.035f, starColor)
        }
    }

    /**
     * A checkmark in a circle for "no history" state.
     */
    @Composable
    fun NoHistory(modifier: Modifier = Modifier) {
        val colors = LocalWakeForgeColors.current
        Canvas(
            modifier = modifier.size(120.dp),
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.width * 0.25f

            // Circle outline
            drawCircle(
                color = colors.secondaryText.copy(alpha = 0.2f),
                radius = radius,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = size.width * 0.02f,
                ),
            )

            // Checkmark
            val checkColor = colors.primaryAccent.copy(alpha = 0.4f)
            val checkPath = Path().apply {
                moveTo(cx - radius * 0.4f, cy)
                lineTo(cx - radius * 0.1f, cy + radius * 0.3f)
                lineTo(cx + radius * 0.45f, cy - radius * 0.3f)
            }
            drawPath(
                path = checkPath,
                color = checkColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = size.width * 0.035f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round,
                ),
            )
        }
    }

    /**
     * A trophy illustration for "no achievements" state.
     */
    @Composable
    fun NoAchievements(modifier: Modifier = Modifier) {
        val colors = LocalWakeForgeColors.current
        Canvas(
            modifier = modifier.size(120.dp),
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val cupColor = colors.warning.copy(alpha = 0.3f)
            val cupWidth = size.width * 0.35f
            val cupHeight = size.width * 0.3f
            val cupTop = cy - cupHeight * 0.3f

            // Cup body (trapezoid approximated with a path)
            val cupPath = Path().apply {
                moveTo(cx - cupWidth * 0.5f, cupTop)
                lineTo(cx - cupWidth * 0.35f, cupTop + cupHeight)
                lineTo(cx + cupWidth * 0.35f, cupTop + cupHeight)
                lineTo(cx + cupWidth * 0.5f, cupTop)
                close()
            }
            drawPath(cupPath, cupColor)

            // Handles
            val handleStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = size.width * 0.025f,
            )
            drawArc(
                color = cupColor,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - cupWidth * 0.5f - size.width * 0.1f, cupTop + cupHeight * 0.15f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.12f, cupHeight * 0.6f),
                style = handleStroke,
            )
            drawArc(
                color = cupColor,
                startAngle = 270f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx + cupWidth * 0.5f - size.width * 0.02f, cupTop + cupHeight * 0.15f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.12f, cupHeight * 0.6f),
                style = handleStroke,
            )

            // Base
            drawRect(
                color = cupColor,
                topLeft = Offset(cx - cupWidth * 0.25f, cupTop + cupHeight),
                size = androidx.compose.ui.geometry.Size(cupWidth * 0.5f, size.width * 0.05f),
            )
            drawRect(
                color = cupColor,
                topLeft = Offset(cx - cupWidth * 0.35f, cupTop + cupHeight + size.width * 0.05f),
                size = androidx.compose.ui.geometry.Size(cupWidth * 0.7f, size.width * 0.04f),
            )

            // Star on cup
            drawStar(cx, cupTop + cupHeight * 0.45f, size.width * 0.06f, colors.background.copy(alpha = 0.5f))
        }
    }
}

/**
 * Draws a simple 4-pointed star shape.
 */
private fun DrawScope.drawStar(
    centerX: Float,
    centerY: Float,
    radius: Float,
    color: Color,
) {
    val starPath = Path().apply {
        // Top point
        moveTo(centerX, centerY - radius)
        // Right point
        lineTo(centerX + radius * 0.3f, centerY - radius * 0.3f)
        lineTo(centerX + radius, centerY)
        // Bottom point
        lineTo(centerX + radius * 0.3f, centerY + radius * 0.3f)
        lineTo(centerX, centerY + radius)
        // Left point
        lineTo(centerX - radius * 0.3f, centerY + radius * 0.3f)
        lineTo(centerX - radius, centerY)
        lineTo(centerX - radius * 0.3f, centerY - radius * 0.3f)
        close()
    }
    drawPath(starPath, color)
}
