package com.wakeforge.app.presentation.create_alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.models.DayOfWeek
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.usecases.alarm.CreateAlarmUseCase
import com.wakeforge.app.domain.usecases.settings.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Create Alarm screen.
 *
 * Manages the full alarm configuration form state and delegates persistence
 * to [CreateAlarmUseCase]. Default values are loaded from user settings.
 */
@HiltViewModel
class CreateAlarmViewModel @Inject constructor(
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    // ── UI State ───────────────────────────────────────────────────────────

    data class CreateAlarmUiState(
        val hour: Int = 7,
        val minute: Int = 0,
        val label: String = "",
        val repeatDays: List<DayOfWeek> = emptyList(),
        val soundUri: String = "builtin_dawn",
        val vibrationEnabled: Boolean = true,
        val gradualVolumeEnabled: Boolean = false,
        val gradualVolumeDuration: Int = 60,
        val missionType: MissionType = MissionType.MATH,
        val difficulty: MissionDifficulty = MissionDifficulty.MEDIUM,
        val snoozeInterval: Int = 5,
        val maxSnoozeCount: Int = 3,
        val smartEscalationEnabled: Boolean = true,
        val strictModeEnabled: Boolean = false,
        val multiStepEnabled: Boolean = false,
        val multiStepCount: Int = 2,
        val timedModeEnabled: Boolean = true,
        val isSaving: Boolean = false,
        val is24HourFormat: Boolean = false
    )

    private val _uiState = MutableStateFlow(CreateAlarmUiState())
    val uiState: StateFlow<CreateAlarmUiState> = _uiState.asStateFlow()

    // ── Events ─────────────────────────────────────────────────────────────

    sealed interface CreateAlarmEvent {
        data object SaveSuccess : CreateAlarmEvent
        data class SaveError(val message: String) : CreateAlarmEvent
    }

    private val _events = MutableSharedFlow<CreateAlarmEvent>()
    val events: SharedFlow<CreateAlarmEvent> = _events.asSharedFlow()

    // ── Init ───────────────────────────────────────────────────────────────

    init {
        loadDefaultsFromSettings()
    }

    private fun loadDefaultsFromSettings() {
        viewModelScope.launch {
            try {
                val settings = getSettingsUseCase().first()
                _uiState.value = _uiState.value.copy(
                    snoozeInterval = settings.defaultSnoozeInterval,
                    maxSnoozeCount = settings.defaultMaxSnoozeCount,
                    missionType = settings.defaultMissionType,
                    difficulty = settings.defaultDifficulty,
                    strictModeEnabled = settings.strictModeDefault,
                    is24HourFormat = settings.is24HourFormat
                )
            } catch (_: Exception) {
                // Keep hardcoded defaults if settings fail to load
            }
        }
    }

    // ── Time ───────────────────────────────────────────────────────────────

    fun updateHour(hour: Int) {
        _uiState.value = _uiState.value.copy(hour = hour.coerceIn(0, 23))
    }

    fun updateMinute(minute: Int) {
        _uiState.value = _uiState.value.copy(minute = minute.coerceIn(0, 59))
    }

    // ── Label ──────────────────────────────────────────────────────────────

    fun updateLabel(label: String) {
        _uiState.value = _uiState.value.copy(label = label.take(50))
    }

    // ── Repeat Days ────────────────────────────────────────────────────────

    fun toggleRepeatDay(day: DayOfWeek) {
        val currentDays = _uiState.value.repeatDays.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(repeatDays = currentDays)
    }

    fun setAllDays() {
        _uiState.value = _uiState.value.copy(
            repeatDays = DayOfWeek.entries
        )
    }

    fun clearAllDays() {
        _uiState.value = _uiState.value.copy(repeatDays = emptyList())
    }

    fun setWeekdays() {
        _uiState.value = _uiState.value.copy(
            repeatDays = listOf(
                DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED,
                DayOfWeek.THU, DayOfWeek.FRI
            )
        )
    }

    fun setWeekends() {
        _uiState.value = _uiState.value.copy(
            repeatDays = listOf(DayOfWeek.SAT, DayOfWeek.SUN)
        )
    }

    // ── Sound & Vibration ──────────────────────────────────────────────────

    fun updateSound(uri: String) {
        _uiState.value = _uiState.value.copy(soundUri = uri)
    }

    fun toggleVibration() {
        _uiState.value = _uiState.value.copy(
            vibrationEnabled = !_uiState.value.vibrationEnabled
        )
    }

    // ── Gradual Volume ─────────────────────────────────────────────────────

    fun toggleGradualVolume() {
        _uiState.value = _uiState.value.copy(
            gradualVolumeEnabled = !_uiState.value.gradualVolumeEnabled
        )
    }

    fun updateGradualVolumeDuration(duration: Int) {
        _uiState.value = _uiState.value.copy(
            gradualVolumeDuration = duration.coerceIn(30, 300)
        )
    }

    // ── Mission & Difficulty ───────────────────────────────────────────────

    fun setMissionType(type: MissionType) {
        _uiState.value = _uiState.value.copy(missionType = type)
    }

    fun setDifficulty(difficulty: MissionDifficulty) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    // ── Snooze ─────────────────────────────────────────────────────────────

    fun updateSnoozeInterval(interval: Int) {
        _uiState.value = _uiState.value.copy(
            snoozeInterval = interval.coerceIn(1, 30)
        )
    }

    fun updateMaxSnoozeCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            maxSnoozeCount = count.coerceIn(0, 10)
        )
    }

    // ── Toggles ────────────────────────────────────────────────────────────

    fun toggleSmartEscalation() {
        _uiState.value = _uiState.value.copy(
            smartEscalationEnabled = !_uiState.value.smartEscalationEnabled
        )
    }

    fun toggleStrictMode() {
        _uiState.value = _uiState.value.copy(
            strictModeEnabled = !_uiState.value.strictModeEnabled
        )
    }

    fun toggleMultiStep() {
        val newState = !_uiState.value.multiStepEnabled
        _uiState.value = _uiState.value.copy(
            multiStepEnabled = newState,
            multiStepCount = if (newState) 2 else _uiState.value.multiStepCount
        )
    }

    fun toggleTimedMode() {
        _uiState.value = _uiState.value.copy(
            timedModeEnabled = !_uiState.value.timedModeEnabled
        )
    }

    fun updateMultiStepCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            multiStepCount = count.coerceIn(2, 5)
        )
    }

    // ── Save ───────────────────────────────────────────────────────────────

    fun saveAlarm() {
        val state = _uiState.value
        if (state.isSaving) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val alarm = Alarm(
                    hour = state.hour,
                    minute = state.minute,
                    label = state.label,
                    repeatDays = state.repeatDays,
                    soundUri = state.soundUri,
                    vibrationEnabled = state.vibrationEnabled,
                    gradualVolumeEnabled = state.gradualVolumeEnabled,
                    gradualVolumeDurationSeconds = state.gradualVolumeDuration,
                    missionType = state.missionType,
                    difficulty = state.difficulty,
                    snoozeIntervalMinutes = state.snoozeInterval,
                    maxSnoozeCount = state.maxSnoozeCount,
                    smartEscalationEnabled = state.smartEscalationEnabled,
                    strictModeEnabled = state.strictModeEnabled,
                    multiStepEnabled = state.multiStepEnabled,
                    multiStepCount = state.multiStepCount,
                    timedModeEnabled = state.timedModeEnabled,
                    isActive = true
                )
                createAlarmUseCase(alarm)
                _events.emit(CreateAlarmEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(CreateAlarmEvent.SaveError(e.message ?: "Failed to save alarm"))
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
}
