package com.wakeforge.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.core.utils.TimeUtils
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.models.DailyStats
import com.wakeforge.app.domain.models.Streak
import com.wakeforge.app.domain.usecases.alarm.GetNextAlarmUseCase
import com.wakeforge.app.domain.usecases.stats.GetStreakUseCase
import com.wakeforge.app.domain.usecases.stats.GetWeeklyStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen dashboard.
 *
 * Aggregates data from multiple sources (next alarm, streak, weekly stats)
 * and presents them as a unified UI state. Automatically updates the
 * countdown timer every minute.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNextAlarmUseCase: GetNextAlarmUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val getWeeklyStatsUseCase: GetWeeklyStatsUseCase,
) : ViewModel() {

    data class HomeUiState(
        val nextAlarm: Alarm? = null,
        val currentStreak: Int = 0,
        val longestStreak: Int = 0,
        val weeklySuccessRate: Float = 0f,
        val totalWakeUps: Int = 0,
        val averageSnooze: Float = 0f,
        val timeUntilNextAlarm: String? = null,
        val isLoading: Boolean = true,
        val weeklyData: List<DailyStats> = emptyList(),
    )

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadInitialData()
        startCountdownUpdates()
    }

    /**
     * Loads initial data from all use cases.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            // Collect streak reactively
            getStreakUseCase().distinctUntilChanged().collect { streak ->
                _state.value = _state.value.copy(
                    currentStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak,
                    totalWakeUps = streak.totalSuccesses + streak.totalFailures,
                    averageSnooze = if (streak.totalSuccesses + streak.totalFailures > 0) {
                        streak.totalSnoozes.toFloat() / (streak.totalSuccesses + streak.totalFailures)
                    } else {
                        0f
                    },
                    isLoading = false,
                )
            }
        }

        viewModelScope.launch {
            // Collect next alarm reactively
            getNextAlarmUseCase().distinctUntilChanged().collect { alarm ->
                _state.value = _state.value.copy(
                    nextAlarm = alarm,
                )
                updateTimeUntilAlarm(alarm)
            }
        }

        viewModelScope.launch {
            // Load weekly stats once (suspend function)
            try {
                val weeklyStats = getWeeklyStatsUseCase()
                val totalSuccesses = weeklyStats.sumOf { it.successes }
                val totalFailures = weeklyStats.sumOf { it.failures }
                val totalEvents = totalSuccesses + totalFailures
                val successRate = if (totalEvents > 0) {
                    (totalSuccesses.toFloat() / totalEvents) * 100f
                } else {
                    0f
                }

                _state.value = _state.value.copy(
                    weeklySuccessRate = successRate,
                    weeklyData = weeklyStats,
                )
            } catch (_: Exception) {
                // Silently handle errors; use default 0f
            }
        }
    }

    /**
     * Calculates and updates the countdown string for the next alarm.
     */
    private fun updateTimeUntilAlarm(alarm: Alarm?) {
        if (alarm == null) {
            _state.value = _state.value.copy(timeUntilNextAlarm = null)
            return
        }

        val nextFireTime = alarm.nextFireTime()
        val timeUntil = TimeUtils.getTimeUntilAlarm(nextFireTime)
        val formatted = TimeUtils.formatCountdown(timeUntil)

        _state.value = _state.value.copy(
            timeUntilNextAlarm = if (timeUntil > 0) "in $formatted" else null,
        )
    }

    /**
     * Starts a repeating coroutine that updates the countdown timer every 30 seconds.
     */
    private fun startCountdownUpdates() {
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(30_000L) // Update every 30 seconds
                val alarm = _state.value.nextAlarm
                updateTimeUntilAlarm(alarm)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
