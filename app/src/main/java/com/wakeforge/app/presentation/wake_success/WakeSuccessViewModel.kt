package com.wakeforge.app.presentation.wake_success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.usecases.stats.GetAnalyticsUseCase
import com.wakeforge.app.domain.usecases.stats.GetStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the wake success screen.
 *
 * Loads streak and analytics data after a successful (or failed) wake-up attempt,
 * detects new streak records, and exposes all data needed by the success UI.
 *
 * @property getStreakUseCase    Retrieves the current wake-up streak.
 * @property getAnalyticsUseCase Retrieves comprehensive wake-up analytics.
 */
@HiltViewModel
class WakeSuccessViewModel @Inject constructor(
    private val getStreakUseCase: GetStreakUseCase,
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val KEY_ALARM_ID = "alarmId"
        private const val KEY_WAKE_RECORD_ID = "wakeRecordId"
    }

    // ── State ─────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(WakeSuccessUiState())
    val uiState: StateFlow<WakeSuccessUiState> = _uiState.asStateFlow()

    // ── Navigation args ──────────────────────────────────────────────

    private val alarmId: String = savedStateHandle[KEY_ALARM_ID] ?: ""
    private val wakeRecordId: String = savedStateHandle[KEY_WAKE_RECORD_ID] ?: ""

    // ── Init ─────────────────────────────────────────────────────────

    init {
        loadStreakAndAnalytics()
    }

    private fun loadStreakAndAnalytics() {
        viewModelScope.launch {
            try {
                // Load streak data
                val streak = getStreakUseCase().first()

                // Load analytics
                val analytics = getAnalyticsUseCase()

                val isNewRecord = streak.currentStreak > streak.longestStreak ||
                    (streak.currentStreak == streak.longestStreak && streak.currentStreak > 0)

                _uiState.value = WakeSuccessUiState(
                    isNewStreakRecord = isNewRecord && streak.currentStreak > 1,
                    currentStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak.coerceAtLeast(streak.currentStreak),
                    missionType = analytics.mostUsedMissionType ?: MissionType.MATH,
                    difficulty = analytics.mostUsedDifficulty ?: MissionDifficulty.MEDIUM,
                    snoozeCount = analytics.totalSnoozes,
                )

                Timber.d("WakeSuccess loaded: streak=${streak.currentStreak}, newRecord=${_uiState.value.isNewStreakRecord}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load streak/analytics for WakeSuccess")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

/**
 * UI state for the wake success screen.
 *
 * @property isNewStreakRecord Whether the current streak exceeds the previous longest.
 * @property currentStreak     Number of consecutive successful wake-ups.
 * @property longestStreak     All-time highest consecutive successes.
 * @property completionTime    Formatted completion time string (e.g. "2m 15s").
 * @property missionType       The mission type most recently completed.
 * @property difficulty        The difficulty tier of the completed mission.
 * @property snoozeCount       Number of snoozes before completing.
 * @property isLoading         Whether data is still loading.
 */
data class WakeSuccessUiState(
    val isNewStreakRecord: Boolean = false,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionTime: String = "",
    val missionType: MissionType = MissionType.MATH,
    val difficulty: MissionDifficulty = MissionDifficulty.MEDIUM,
    val snoozeCount: Int = 0,
    val isLoading: Boolean = true,
)
