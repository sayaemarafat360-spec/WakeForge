package com.wakeforge.app.presentation.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeforge.app.core.components.AnimatedCounter
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFLoadingIndicator
import com.wakeforge.app.core.components.WFTopBar
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.presentation.stats.components.StatsCard
import com.wakeforge.app.presentation.stats.components.StatsIconMap
import com.wakeforge.app.presentation.stats.components.StreakLineChart
import com.wakeforge.app.presentation.stats.components.SuccessRateCircle
import com.wakeforge.app.presentation.stats.components.WeeklyBarChart

/**
 * Full analytics dashboard screen showing wake-up statistics.
 *
 * Sections:
 * 1. **Top Row**: Current Streak, Weekly Success Rate (circle), Total Wake-ups
 * 2. **Weekly Overview**: Stacked bar chart (Mon–Sun)
 * 3. **Streak Trend**: 30-day smooth line chart with gradient fill
 * 4. **Quick Stats Grid**: 2×2 grid of stat cards (snoozes, failures, avg snooze, monthly rate)
 * 5. **Insights**: Best day, most used mission, motivational message
 */
@Composable
fun StatsScreen(
    navController: NavController? = null,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WFLoadingIndicator(size = 48.dp)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WFTopBar(title = "Statistics")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Section 1: Top Row (3 metric cards) ──────────────────────
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Current Streak Card
                        WFCard(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Fire icon drawn on Canvas
                                Canvas(modifier = Modifier.size(28.dp)) {
                                    drawStreakFire(colors.warning)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                AnimatedCounter(
                                    targetValue = uiState.currentStreak,
                                    textStyle = typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.warning
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "day streak",
                                    style = typography.labelMedium,
                                    color = colors.secondaryText
                                )
                            }
                        }

                        // Weekly Success Rate Circle
                        WFCard(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SuccessRateCircle(
                                    rate = uiState.weeklySuccessRate,
                                    size = 100.dp
                                )
                            }
                        }

                        // Total Wake-ups Card
                        WFCard(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = colors.secondaryAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                AnimatedCounter(
                                    targetValue = uiState.totalWakeUps,
                                    textStyle = typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.secondaryAccent
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "wake-ups",
                                    style = typography.labelMedium,
                                    color = colors.secondaryText
                                )
                            }
                        }
                    }
                }
            }

            // ── Section 2: Weekly Overview Bar Chart ─────────────────────
            item {
                StaggeredCard(
                    delayMs = 100
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "This Week",
                                style = typography.titleLarge,
                                color = colors.primaryText
                            )
                            Text(
                                text = "${uiState.weeklyData.sumOf { it.successes }} success",
                                style = typography.labelMedium,
                                color = colors.success
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.weeklyData.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No data yet",
                                    style = typography.bodyMedium,
                                    color = colors.secondaryText
                                )
                            }
                        } else {
                            WeeklyBarChart(data = uiState.weeklyData)
                        }
                    }
                }
            }

            // ── Section 3: Streak Trend Line Chart ───────────────────────
            item {
                StaggeredCard(delayMs = 200) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Streak Trend (30 Days)",
                                style = typography.titleLarge,
                                color = colors.primaryText
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        StreakLineChart(streakData = uiState.streakHistory)
                    }
                }
            }

            // ── Section 4: Quick Stats Grid (2×2) ───────────────────────
            item {
                StaggeredCard(delayMs = 300) {
                    Text(
                        text = "Quick Stats",
                        style = typography.titleLarge,
                        color = colors.primaryText,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
            }

            item {
                StaggeredCard(delayMs = 350) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            StatsCard(
                                label = "Total Snoozes",
                                value = uiState.totalSnoozes,
                                color = colors.warning,
                                icon = Icons.Default.Snooze
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            StatsCard(
                                label = "Total Failures",
                                value = uiState.totalFailures,
                                color = colors.error,
                                icon = Icons.Default.Close
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            StatsCard(
                                label = "Avg Snooze/Alarm",
                                value = uiState.averageSnoozePerAlarm.toInt(),
                                color = colors.secondaryAccent,
                                icon = Icons.Default.Bedtime
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            StatsCard(
                                label = "Monthly Rate",
                                value = (uiState.monthlySuccessRate * 100).toInt(),
                                color = colors.success,
                                icon = Icons.Default.TrendingUp
                            )
                        }
                    }
                }
            }

            // ── Section 5: Insights ──────────────────────────────────────
            item {
                StaggeredCard(delayMs = 400) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Insights",
                            style = typography.titleLarge,
                            color = colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Best Day of Week
                        InsightRow(
                            icon = { 
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = colors.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = "Best Day of Week",
                            value = uiState.bestDayOfWeek ?: "N/A",
                            colors = colors,
                            typography = typography
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Most Used Mission
                        InsightRow(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = colors.secondaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = "Most Used Mission",
                            value = uiState.mostUsedMission ?: "N/A",
                            colors = colors,
                            typography = typography
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Motivational message
                        val motivationalMessage = getMotivationalMessage(
                            weeklySuccessRate = uiState.weeklySuccessRate,
                            currentStreak = uiState.currentStreak
                        )
                        WFCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.primaryAccent.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = motivationalMessage,
                                style = typography.bodyMedium,
                                color = colors.primaryAccent,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Longest streak
                        Spacer(modifier = Modifier.height(8.dp))
                        InsightRow(
                            icon = {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawStreakFire(colors.warning)
                                }
                            },
                            label = "Longest Streak",
                            value = "${uiState.longestStreak} days",
                            colors = colors,
                            typography = typography
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Wrapper that applies staggered entrance animation to a card section.
 */
@Composable
private fun StaggeredCard(
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY = remember { Animatable(30f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    WFCard(
        modifier = Modifier.graphicsLayer {
            translationY = offsetY.value
            this.alpha = alpha.value
        }
    ) {
        content()
    }
}

/**
 * Single row inside the Insights section showing an icon, label, and value.
 */
@Composable
private fun InsightRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    colors: com.wakeforge.app.core.theme.ThemePalette,
    typography: com.wakeforge.app.core.theme.TextStyles
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = typography.labelMedium,
                color = colors.secondaryText
            )
            Text(
                text = value,
                style = typography.bodyLarge,
                color = colors.primaryText,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Draws a simple stylised fire icon on Canvas.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStreakFire(color: Color) {
    val path = Path().apply {
        val cx = size.width / 2f
        val baseY = size.height * 0.85f
        val topY = size.height * 0.1f

        // Flame outer shape
        moveTo(cx, topY)
        cubicTo(
            cx + size.width * 0.45f, topY + size.height * 0.2f,
            cx + size.width * 0.35f, baseY - size.height * 0.2f,
            cx + size.width * 0.15f, baseY
        )
        lineTo(cx - size.width * 0.15f, baseY)
        cubicTo(
            cx - size.width * 0.35f, baseY - size.height * 0.2f,
            cx - size.width * 0.45f, topY + size.height * 0.2f,
            cx, topY
        )
        close()
    }
    drawPath(path, color)

    // Inner flame
    val innerPath = Path().apply {
        val cx = size.width / 2f
        val baseY = size.height * 0.85f
        val topY = size.height * 0.3f

        moveTo(cx, topY)
        cubicTo(
            cx + size.width * 0.2f, topY + size.height * 0.15f,
            cx + size.width * 0.15f, baseY - size.height * 0.15f,
            cx + size.width * 0.05f, baseY
        )
        lineTo(cx - size.width * 0.05f, baseY)
        cubicTo(
            cx - size.width * 0.15f, baseY - size.height * 0.15f,
            cx - size.width * 0.2f, topY + size.height * 0.15f,
            cx, topY
        )
        close()
    }
    drawPath(innerPath, color.copy(alpha = 0.5f))
}

/**
 * Generates a motivational message based on weekly performance.
 */
private fun getMotivationalMessage(weeklySuccessRate: Float, currentStreak: Int): String {
    return when {
        currentStreak >= 14 -> "Incredible! A ${currentStreak}-day streak! You're unstoppable."
        currentStreak >= 7 -> "Amazing work! ${currentStreak} days strong. Keep the momentum going!"
        weeklySuccessRate >= 0.8f -> "Outstanding week! ${(weeklySuccessRate * 100).toInt()}% success rate."
        weeklySuccessRate >= 0.6f -> "Great progress! Your discipline is paying off."
        weeklySuccessRate >= 0.4f -> "You're building a solid habit. Every morning counts!"
        currentStreak == 0 -> "Start your streak today! Small steps lead to big changes."
        else -> "Keep at it! Consistency is the key to building lasting habits."
    }
}


