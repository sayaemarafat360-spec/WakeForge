package com.wakeforge.app.presentation.edit_alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.models.DayOfWeek
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.repositories.AlarmRepository
import com.wakeforge.app.domain.usecases.alarm.DeleteAlarmUseCase
import com.wakeforge.app.domain.usecases.alarm.UpdateAlarmUseCase
import com.wakeforge.app.domain.usecases.settings.GetSettingsUseCase
import com.wakeforge.app.presentation.create_alarm.CreateAlarmViewModel
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
 * ViewModel for the Edit Alarm screen.
 *
 * Identical configuration surface to [CreateAlarmViewModel] but pre-populates
 * state from an existing alarm loaded via [SavedStateHandle] and delegates
 * persistence to [UpdateAlarmUseCase] / [DeleteAlarmUseCase].
 */
@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    companion object {
        private const val KEY_ALARM_ID = "alarmId"
    }

    // ── UI State (reuses CreateAlarm's shape) ─────────────────────────────

    data class EditAlarmUiState(
        val alarmId: String = "",
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
        val isLoading: Boolean = true,
        val is24HourFormat: Boolean = false
    )

    private val _uiState = MutableStateFlow(EditAlarmUiState())
    val uiState: StateFlow<EditAlarmUiState> = _uiState.asStateFlow()

    // ── Events ─────────────────────────────────────────────────────────────

    sealed interface EditAlarmEvent {
        data object SaveSuccess : EditAlarmEvent
        data class SaveError(val message: String) : EditAlarmEvent
        data object DeleteSuccess : EditAlarmEvent
        data class DeleteError(val message: String) : EditAlarmEvent
        data object AlarmNotFound : EditAlarmEvent
    }

    private val _events = MutableSharedFlow<EditAlarmEvent>()
    val events: SharedFlow<EditAlarmEvent> = _events.asSharedFlow()

    // ── Init: Load alarm ──────────────────────────────────────────────────

    private val alarmId: String = savedStateHandle.get<String>(KEY_ALARM_ID)
        ?: savedStateHandle.get<String>("alarmId")
        ?: ""

    init {
        loadAlarm()
        loadSettings()
    }

    private fun loadAlarm() {
        if (alarmId.isBlank()) {
            viewModelScope.launch {
                _events.emit(EditAlarmEvent.AlarmNotFound)
            }
            return
        }

        viewModelScope.launch {
            try {
                alarmRepository.getAlarmById(alarmId).first { alarm ->
                    if (alarm != null) {
                        loadAlarmData(alarm)
                        true
                    } else {
                        _events.emit(EditAlarmEvent.AlarmNotFound)
                        true
                    }
                }
            } catch (e: Exception) {
                _events.emit(EditAlarmEvent.AlarmNotFound)
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = getSettingsUseCase().first()
                _uiState.value = _uiState.value.copy(
                    is24HourFormat = settings.is24HourFormat
                )
            } catch (_: Exception) {
                // Keep defaults
            }
        }
    }

    /**
     * Populates the UI state from an existing alarm.
     * Call this from the screen after loading the alarm from the repository.
     */
    fun loadAlarmData(alarm: Alarm) {
        _uiState.value = EditAlarmUiState(
            alarmId = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            label = alarm.label,
            repeatDays = alarm.repeatDays,
            soundUri = alarm.soundUri,
            vibrationEnabled = alarm.vibrationEnabled,
            gradualVolumeEnabled = alarm.gradualVolumeEnabled,
            gradualVolumeDuration = alarm.gradualVolumeDurationSeconds,
            missionType = alarm.missionType,
            difficulty = alarm.difficulty,
            snoozeInterval = alarm.snoozeIntervalMinutes,
            maxSnoozeCount = alarm.maxSnoozeCount,
            smartEscalationEnabled = alarm.smartEscalationEnabled,
            strictModeEnabled = alarm.strictModeEnabled,
            multiStepEnabled = alarm.multiStepEnabled,
            multiStepCount = alarm.multiStepCount,
            timedModeEnabled = alarm.timedModeEnabled,
            isLoading = false,
            is24HourFormat = _uiState.value.is24HourFormat
        )
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
        _uiState.value = _uiState.value.copy(repeatDays = DayOfWeek.entries)
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
        _uiState.value = _uiState.value.copy(snoozeInterval = interval.coerceIn(1, 30))
    }

    fun updateMaxSnoozeCount(count: Int) {
        _uiState.value = _uiState.value.copy(maxSnoozeCount = count.coerceIn(0, 10))
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
        _uiState.value = _uiState.value.copy(multiStepCount = count.coerceIn(2, 5))
    }

    // ── Save ───────────────────────────────────────────────────────────────

    fun saveAlarm() {
        val state = _uiState.value
        if (state.isSaving || state.alarmId.isBlank()) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val alarm = Alarm(
                    id = state.alarmId,
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
                updateAlarmUseCase(alarm)
                _events.emit(EditAlarmEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(EditAlarmEvent.SaveError(e.message ?: "Failed to update alarm"))
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    fun deleteAlarm() {
        val state = _uiState.value
        if (state.alarmId.isBlank()) return

        viewModelScope.launch {
            try {
                deleteAlarmUseCase(state.alarmId)
                _events.emit(EditAlarmEvent.DeleteSuccess)
            } catch (e: Exception) {
                _events.emit(EditAlarmEvent.DeleteError(e.message ?: "Failed to delete alarm"))
            }
        }
    }
}
