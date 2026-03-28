package com.wakeforge.app.presentation.alarm_ringing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.usecases.alarm.GetAlarmsUseCase
import com.wakeforge.app.domain.usecases.mission.EscalateDifficultyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the alarm ringing screen.
 *
 * Manages the immersive alarm experience including real-time clock updates,
 * snooze tracking, and mission initiation with difficulty escalation.
 *
 * @param getAlarmsUseCase          Use case for fetching alarm data.
 * @param escalateDifficultyUseCase Use case for escalating difficulty based on snooze count.
 */
@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val escalateDifficultyUseCase: EscalateDifficultyUseCase,
) : ViewModel() {

    companion object {
        private const val KEY_ALARM_ID = "alarmId"
        private const val CLOCK_UPDATE_INTERVAL_MS = 1000L
    }

    // ── State ─────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(AlarmRingingUiState())
    val uiState: StateFlow<AlarmRingingUiState> = _uiState.asStateFlow()

    // ── Events ─────────────────────────────────────────────────────────────

    sealed interface AlarmRingingEvent {
        data class StartMission(
            val alarmId: String,
            val missionType: String,
            val escalatedDifficulty: String,
            val snoozeCount: Int,
            val isTimed: Boolean,
        ) : AlarmRingingEvent

        data class Snooze(
            val alarmId: String,
            val snoozeIntervalMinutes: Int,
        ) : AlarmRingingEvent
    }

    private val _events = MutableSharedFlow<AlarmRingingEvent>()
    val events: SharedFlow<AlarmRingingEvent> = _events.asSharedFlow()

    // ── Private state ─────────────────────────────────────────────────────

    private val alarmId: String = savedStateHandle.get<String>(KEY_ALARM_ID)
        ?: savedStateHandle.get<String>("alarmId")
        ?: ""

    private var clockUpdateJob: Job? = null
    private var baseDifficulty: MissionDifficulty = MissionDifficulty.MEDIUM
    private var smartEscalation: Boolean = true
    private var timedMode: Boolean = true
    private var snoozeIntervalMinutes: Int = 5

    // ── Init ───────────────────────────────────────────────────────────────

    init {
        loadAlarm()
    }

    private fun loadAlarm() {
        if (alarmId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Alarm ID not provided"
            )
            return
        }

        viewModelScope.launch {
            try {
                val alarms = getAlarmsUseCase().first()
                val alarm = alarms.find { it.id == alarmId }

                if (alarm == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Alarm not found"
                    )
                    return@launch
                }

                baseDifficulty = alarm.difficulty
                smartEscalation = alarm.smartEscalationEnabled
                timedMode = alarm.timedModeEnabled
                snoozeIntervalMinutes = alarm.snoozeIntervalMinutes

                val ringingInfo = AlarmRingingInfo(
                    id = alarm.id,
                    hour = alarm.hour,
                    minute = alarm.minute,
                    label = alarm.label,
                    missionType = alarm.missionType.name,
                    difficulty = alarm.difficulty.name,
                    snoozeIntervalMinutes = alarm.snoozeIntervalMinutes,
                    maxSnoozeCount = alarm.maxSnoozeCount,
                    smartEscalationEnabled = alarm.smartEscalationEnabled,
                    timedModeEnabled = alarm.timedModeEnabled,
                )

                _uiState.value = _uiState.value.copy(
                    alarm = ringingInfo,
                    maxSnoozeCount = alarm.maxSnoozeCount,
                    canSnooze = alarm.maxSnoozeCount > 0,
                    isLoading = false,
                )

                startClockUpdates()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load alarm"
                )
            }
        }
    }

    // ── Clock ──────────────────────────────────────────────────────────────

    private fun startClockUpdates() {
        clockUpdateJob?.cancel()
        clockUpdateJob = viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(
                    currentTime = LocalDateTime.now()
                )
                delay(CLOCK_UPDATE_INTERVAL_MS)
            }
        }
    }

    // ── Actions ────────────────────────────────────────────────────────────

    /**
     * Initiates the mission challenge with escalated difficulty based on
     * snooze count.
     */
    fun startMission() {
        val state = _uiState.value
        val alarm = state.alarm ?: return

        val escalatedDifficulty = escalateDifficultyUseCase(
            baseDifficulty = baseDifficulty,
            snoozeCount = state.snoozeCount,
            smartEscalation = smartEscalation,
        )

        _uiState.value = state.copy(isMissionActive = true)

        viewModelScope.launch {
            _events.emit(
                AlarmRingingEvent.StartMission(
                    alarmId = alarm.id,
                    missionType = alarm.missionType,
                    escalatedDifficulty = escalatedDifficulty.name,
                    snoozeCount = state.snoozeCount,
                    isTimed = timedMode,
                )
            )
        }
    }

    /**
     * Snoozes the alarm for the configured interval.
     * Increments snooze count and checks if further snoozes are allowed.
     */
    fun snooze() {
        val state = _uiState.value
        val alarm = state.alarm ?: return

        if (!state.canSnooze) return

        val newSnoozeCount = state.snoozeCount + 1
        val remaining = state.maxSnoozeCount - newSnoozeCount

        _uiState.value = state.copy(
            snoozeCount = newSnoozeCount,
            canSnooze = remaining > 0,
        )

        viewModelScope.launch {
            _events.emit(
                AlarmRingingEvent.Snooze(
                    alarmId = alarm.id,
                    snoozeIntervalMinutes = snoozeIntervalMinutes,
                )
            )
        }
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        clockUpdateJob?.cancel()
    }
}
