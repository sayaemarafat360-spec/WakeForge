package com.wakeforge.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wakeforge.app.R
import com.wakeforge.app.core.components.AnimatedCounter
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.EmptyStateIllustrations
import com.wakeforge.app.core.components.StreakCard
import com.wakeforge.app.core.components.TimeDisplay
import com.wakeforge.app.core.components.TimeDisplayStyle
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.components.WFEmptyState
import com.wakeforge.app.core.components.WFLoadingIndicator
import com.wakeforge.app.core.theme.BackgroundDark
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.Success
import com.wakeforge.app.core.theme.Warning
import com.wakeforge.app.core.theme.Error
import com.wakeforge.app.core.utils.TimeUtils
import com.wakeforge.app.domain.models.DailyStats
import com.wakeforge.app.domain.models.DayOfWeek
import com.wakeforge.app.presentation.home.components.NextAlarmCard
import com.wakeforge.app.presentation.home.components.QuickStatsRow
import java.util.Calendar

/**
 * Beautiful home dashboard screen for WakeForge.
 *
 * Displays:
 * - Greeting section based on time of day
 * - Next alarm card (or empty state)
 * - Streak card
 * - Quick stats row (weekly success, total wake-ups, avg snooze)
 * - Quick create CTA button
 * - Recent wake history section
 *
 * All cards use staggered entrance animations.
 *
 * @param navController Controller for navigation.
 * @param viewModel The [HomeViewModel] managing dashboard state.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            WFLoadingIndicator()
        }
        return
    }

    // Determine greeting based on time of day
    val greeting = getGreeting()
    val currentDate = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault())
        .format(java.util.Date())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 24.dp,
            bottom = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Greeting Section ──────────────────────────────────────────────
        item(key = "greeting") {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                ),
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = typography.headlineLarge,
                        color = colors.primaryText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDate,
                        style = typography.bodyMedium,
                        color = colors.secondaryText,
                    )
                }
            }
        }

        // ── Next Alarm Card ──────────────────────────────────────────────
        item(key = "nextAlarm") {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 80,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 300,
                        delayMillis = 80,
                    ),
                ),
            ) {
                NextAlarmCard(
                    alarm = uiState.nextAlarm,
                    timeUntil = uiState.timeUntilNextAlarm,
                    is24Hour = TimeUtils.is24HourFormat(context),
                    onNavigateToAlarms = {
                        navController.navigate("alarms")
                    },
                    onCreateAlarm = {
                        navController.navigate("create_alarm")
                    },
                )
            }
        }

        // ── Streak Card ──────────────────────────────────────────────────
        item(key = "streak") {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 160,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 300,
                        delayMillis = 160,
                    ),
                ),
            ) {
                StreakCard(
                    currentStreak = uiState.currentStreak,
                    longestStreak = uiState.longestStreak,
                )
            }
        }

        // ── Quick Stats Row ──────────────────────────────────────────────
        item(key = "quickStats") {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 240,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 300,
                        delayMillis = 240,
                    ),
                ),
            ) {
                QuickStatsRow(
                    weeklySuccessRate = uiState.weeklySuccessRate,
                    totalWakeUps = uiState.totalWakeUps,
                    averageSnooze = uiState.averageSnooze,
                )
            }
        }

        // ── Quick Create CTA ─────────────────────────────────────────────
        item(key = "createCta") {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 320,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 300,
                        delayMillis = 320,
                    ),
                ),
            ) {
                WFButton(
                    text = stringResource(id = R.string.create_alarm),
                    onClick = { navController.navigate("create_alarm") },
                    type = ButtonType.Primary,
                    fullWidth = true,
                )
            }
        }

        // ── Recent Wake History Section ──────────────────────────────────
        item(key = "historyHeader") {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 400,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 300,
                        delayMillis = 400,
                    ),
                ),
            ) {
                Text(
                    text = "Recent Wake History",
                    style = typography.headlineMedium,
                    color = colors.primaryText,
                )
            }
        }

        // Recent wake history — show daily summaries or empty state
        val recentDays = uiState.weeklyData
            .filter { it.successes > 0 || it.failures > 0 || it.snoozes > 0 }
            .reversed()
            .take(5)
        val hasHistory = recentDays.isNotEmpty()
        if (hasHistory) {
            items(
                count = recentDays.size,
                key = { index -> "history_${recentDays[index].date}" },
            ) { index ->
                val day = recentDays[index]
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 400,
                            delayMillis = 480 + index * 80,
                            easing = FastOutSlowInEasing,
                        ),
                    ) + fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 300,
                            delayMillis = 480 + index * 80,
                        ),
                    ),
                ) {
                    RecentWakeItem(
                        dayStats = day,
                    )
                }
            }
        } else {
            item {
                WFEmptyState(
                    icon = {
                        Canvas(modifier = Modifier.size(64.dp)) {
                            val iconColor = colors.secondaryText
                            drawCircle(
                                color = iconColor.copy(alpha = 0.15f),
                                radius = 30.dp.toPx(),
                            )
                            drawCircle(
                                color = iconColor.copy(alpha = 0.4f),
                                radius = 16.dp.toPx(),
                            )
                            drawCircle(
                                color = colors.success.copy(alpha = 0.6f),
                                radius = 4.dp.toPx(),
                            )
                        }
                    },
                    title = "No Wake History Yet",
                    subtitle = "Complete your first alarm to see your wake history here",
                )
            }
        }

        // ── Bottom spacing for navigation bar ────────────────────────────
        item {
            Spacer(
                modifier = Modifier
                    .height(80.dp)
                    .navigationBarsPadding(),
            )
        }
    }
}

/**
 * Returns a greeting based on the current hour of the day.
 */
@Composable
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

/**
 * A single wake history item card summarizing one day's wake-up activity.
 */
@Composable
private fun RecentWakeItem(
    dayStats: DailyStats,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val totalEvents = dayStats.successes + dayStats.failures
    val successRate = if (totalEvents > 0) dayStats.successes.toFloat() / totalEvents else 0f

    // Border and label color based on success rate
    val (borderColor, outcomeLabel) = when {
        successRate >= 0.8f -> Success to "Great Day"
        successRate >= 0.5f -> Warning to "Mixed"
        else -> Error to "Tough Day"
    }

    // Format the date from the timestamp
    val dateText = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        .format(java.util.Date(dayStats.date))

    WFCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor,
        borderWidth = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: outcome badge
            Box(
                modifier = Modifier
                    .padding(end = 12.dp),
            ) {
                Text(
                    text = outcomeLabel,
                    style = typography.labelLarge,
                    color = borderColor,
                )
            }

            // Center: stats summary
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = dateText,
                    style = typography.titleLarge,
                    color = colors.primaryText,
                )
                Text(
                    text = "${dayStats.successes} succeeded · ${dayStats.failures} failed · ${dayStats.snoozes} snoozed",
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                )
            }

            // Right: success rate
            Text(
                text = "${(successRate * 100).toInt()}%",
                style = typography.labelMedium,
                color = borderColor,
            )
        }
    }
}
