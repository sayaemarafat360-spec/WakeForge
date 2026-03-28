package com.wakeforge.app.presentation.home.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.R
import com.wakeforge.app.core.components.AnimatedCounter
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.PrimaryAccent
import com.wakeforge.app.core.theme.SecondaryAccent
import com.wakeforge.app.core.theme.Warning

/**
 * Horizontal row of 3 compact stat cards for the home dashboard.
 *
 * Cards display:
 * 1. **Weekly Success** — circular Canvas progress ring with percentage text
 * 2. **Total Wake-ups** — large animated counter number
 * 3. **Avg Snooze** — animated counter number
 *
 * Each card is a compact [WFCard] with an icon, value, and label.
 *
 * @param weeklySuccessRate Success percentage (0–100).
 * @param totalWakeUps Total number of wake-up events.
 * @param averageSnooze Average snoozes per alarm event.
 */
@Composable
fun QuickStatsRow(
    weeklySuccessRate: Float,
    totalWakeUps: Int,
    averageSnooze: Float,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Weekly Success Card ───────────────────────────────────────
        WFCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Circular progress ring
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(56.dp)) {
                        val strokeWidth = 4.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Track (background ring)
                        drawArc(
                            color = colors.border,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        // Progress arc
                        val sweepAngle = (weeklySuccessRate / 100f) * 360f
                        if (sweepAngle > 0f) {
                            drawArc(
                                color = PrimaryAccent,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            )
                        }
                    }

                    // Percentage text in the center
                    Text(
                        text = "${weeklySuccessRate.toInt()}%",
                        style = typography.labelLarge.copy(
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        ),
                        color = colors.primaryText,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.stats_weekly_success),
                    style = typography.labelMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Total Wake-ups Card ───────────────────────────────────────
        WFCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = SecondaryAccent,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedCounter(
                    targetValue = totalWakeUps,
                    textStyle = typography.displayLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                    color = colors.primaryText,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.stats_total_wakeups),
                    style = typography.labelMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Avg Snooze Card ───────────────────────────────────────────
        WFCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Snooze,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedCounter(
                    targetValue = averageSnooze.toInt(),
                    textStyle = typography.displayLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                    color = colors.primaryText,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.stats_snooze_usage),
                    style = typography.labelMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
