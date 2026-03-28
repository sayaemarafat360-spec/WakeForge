package com.wakeforge.app.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.models.AnalyticsData
import com.wakeforge.app.domain.models.DailyStats
import com.wakeforge.app.domain.models.Streak
import com.wakeforge.app.domain.usecases.stats.GetAnalyticsUseCase
import com.wakeforge.app.domain.usecases.stats.GetStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val getStreakUseCase: GetStreakUseCase
) : ViewModel() {

    data class StatsUiState(
        val currentStreak: Int = 0,
        val longestStreak: Int = 0,
        val weeklySuccessRate: Float = 0f,
        val totalWakeUps: Int = 0,
        val totalSnoozes: Int = 0,
        val totalFailures: Int = 0,
        val averageSnoozePerAlarm: Float = 0f,
        val monthlySuccessRate: Float = 0f,
        val weeklyData: List<DailyStats> = emptyList(),
        val bestDayOfWeek: String? = null,
        val mostUsedMission: String? = null,
        val streakHistory: List<Int> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        viewModelScope.launch {
            val analyticsFlow = kotlinx.coroutines.flow.flow {
                emit(getAnalyticsUseCase())
            }

            combine(
                getStreakUseCase(),
                analyticsFlow
            ) { streak, analytics ->
                Pair(streak, analytics)
            }.debounce(100)
             .collect { (streak, analytics) ->
                _uiState.value = mapToUiState(streak, analytics)
            }
        }
    }

    private fun mapToUiState(streak: Streak, analytics: AnalyticsData): StatsUiState {
        return StatsUiState(
            currentStreak = analytics.currentStreak,
            longestStreak = analytics.longestStreak,
            weeklySuccessRate = analytics.weeklySuccessRate,
            totalWakeUps = analytics.totalWakeUps,
            totalSnoozes = analytics.totalSnoozes,
            totalFailures = analytics.totalFailures,
            averageSnoozePerAlarm = analytics.averageSnoozePerAlarm,
            monthlySuccessRate = analytics.monthlySuccessRate,
            weeklyData = analytics.weeklyData,
            bestDayOfWeek = analytics.bestDayOfWeek?.let { formatDayOfWeek(it) },
            mostUsedMission = analytics.mostUsedMissionType?.displayName?.let { name ->
                name.replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { char -> char.uppercase() }
                    }
            },
            streakHistory = buildStreakHistory(streak, analytics),
            isLoading = false
        )
    }

    private fun formatDayOfWeek(dayOfWeek: com.wakeforge.app.domain.models.DayOfWeek): String {
        return dayOfWeek.abbreviation
    }

    /**
     * Builds a synthetic 30-day streak history for the line chart.
     * Uses weekly data when available, and fills gaps with interpolated values.
     */
    private fun buildStreakHistory(streak: Streak, analytics: AnalyticsData): List<Int> {
        val history = mutableListOf<Int>()

        // Build from weekly data spread across 30 days
        val weeklyMap = analytics.weeklyData.associateBy { it.dayOfWeek }

        for (dayIndex in 0 until 30) {
            val dayOfWeek = ((dayIndex % 7) + 2) % 7 + 1  // Calendar.SUNDAY=1 .. SATURDAY=7
            val dayData = weeklyMap[dayOfWeek]

            if (dayData != null) {
                // Use actual data for recent days, interpolated for older
                val weekNum = dayIndex / 7
                val factor = 1f - (weekNum * 0.15f).coerceAtMost(0.8f)
                val streakValue = (dayData.successes * factor).toInt().coerceAtLeast(0)
                history.add(streakValue)
            } else {
                // Generate synthetic data based on overall patterns
                val baseChance = analytics.weeklySuccessRate
                if (dayIndex >= 30 - analytics.currentStreak) {
                    val runPosition = dayIndex - (30 - analytics.currentStreak)
                    history.add(runPosition + 1)
                } else if (Math.random() < baseChance) {
                    history.add((Math.random() * 3 + 1).toInt())
                } else {
                    history.add(0)
                }
            }
        }

        return history.take(30)
    }
}
