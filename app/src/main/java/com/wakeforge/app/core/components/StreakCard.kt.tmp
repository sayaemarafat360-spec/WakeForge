package com.wakeforge.app.core.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import androidx.compose.material3.Text

/**
 * Card displaying the user's current and longest wake-up streaks.
 *
 * Features:
 * - Animated counter for the current streak (rolls from previous value).
 * - Canvas-drawn fire icon that scales based on streak magnitude.
 * - Longest streak subtitle.
 *
 * @param currentStreak Current consecutive wake-up count.
 * @param longestStreak All-time highest streak.
 * @param modifier      Outer modifier.
 */
@Composable
fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // Animate the streak number
    val animatedStreak by animateFloatAsState(
        targetValue = currentStreak.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "streakCounter",
    )

    // Fire glow intensity based on streak size
    val glowIntensity = (animatedStreak / 30f).coerceIn(0f, 1f)

    WFCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left side: Streak info
            Column {
                Text(
                    text = "Current Streak",
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = animatedStreak.toInt().toString(),
                        style = typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent,
                        ),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "days",
                        style = typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Best: $longestStreak days",
                    style = typography.labelMedium,
                    color = colors.secondaryText,
                )
            }

            // Right side: Fire icon
            Canvas(
                modifier = Modifier.size(48.dp),
            ) {
                drawFireIcon(
                    center = Offset(size.width / 2f, size.height / 2f),
                    iconSize = size.width * 0.8f,
                    glowIntensity = glowIntensity,
                    accentColor = colors.primaryAccent,
                    warningColor = colors.warning,
                )
            }
        }
    }
}

/**
 * Draws a stylized fire icon using Canvas paths.
 * The fire glows more intensely as [glowIntensity] increases (0..1).
 */
private fun DrawScope.drawFireIcon(
    center: Offset,
    iconSize: Float,
    glowIntensity: Float,
    accentColor: Color,
    warningColor: Color,
) {
    val halfSize = iconSize / 2f
    val topLeft = Offset(center.x - halfSize, center.y - halfSize)

    // Fire body path — a simple flame shape
    val firePath = Path().apply {
        moveTo(center.x, topLeft.y) // tip of flame
        // Right curve
        cubicTo(
            center.x + halfSize * 0.6f, topLeft.y + halfSize * 0.3f,
            center.x + halfSize * 0.8f, topLeft.y + halfSize * 1.2f,
            center.x + halfSize * 0.4f, topLeft.y + iconSize * 0.85f,
        )
        // Bottom right to bottom left
        lineTo(center.x - halfSize * 0.4f, topLeft.y + iconSize * 0.85f)
        // Left curve
        cubicTo(
            center.x - halfSize * 0.8f, topLeft.y + halfSize * 1.2f,
            center.x - halfSize * 0.6f, topLeft.y + halfSize * 0.3f,
            center.x, topLeft.y,
        )
        close()
    }

    // Inner flame (smaller, lighter)
    val innerPath = Path().apply {
        val innerScale = 0.5f
        val innerTop = center.y - halfSize * innerScale * 0.5f
        moveTo(center.x, innerTop)
        cubicTo(
            center.x + halfSize * 0.3f, innerTop + halfSize * 0.3f,
            center.x + halfSize * 0.4f, innerTop + halfSize * 1.0f,
            center.x + halfSize * 0.2f, innerTop + halfSize * 1.4f,
        )
        lineTo(center.x - halfSize * 0.2f, innerTop + halfSize * 1.4f)
        cubicTo(
            center.x - halfSize * 0.4f, innerTop + halfSize * 1.0f,
            center.x - halfSize * 0.3f, innerTop + halfSize * 0.3f,
            center.x, innerTop,
        )
        close()
    }

    // Draw outer fire with gradient
    val fireBrush = Brush.verticalGradient(
        colors = listOf(
            accentColor,
            if (glowIntensity > 0.5f) warningColor else accentColor.copy(alpha = 0.7f),
        ),
        startY = topLeft.y,
        endY = topLeft.y + iconSize,
    )
    drawPath(firePath, fireBrush)

    // Draw inner flame (lighter)
    drawPath(innerPath, Color.White.copy(alpha = 0.3f + glowIntensity * 0.2f))

    // Glow effect
    if (glowIntensity > 0.1f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = glowIntensity * 0.3f),
                    Color.Transparent,
                ),
                center = center,
                radius = halfSize * 1.2f,
            ),
            center = center,
            radius = halfSize * 1.2f,
        )
    }
}
