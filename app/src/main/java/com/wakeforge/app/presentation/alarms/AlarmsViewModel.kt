package com.wakeforge.app.presentation.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.usecases.alarm.CreateAlarmUseCase
import com.wakeforge.app.domain.usecases.alarm.DeleteAlarmUseCase
import com.wakeforge.app.domain.usecases.alarm.GetAlarmsUseCase
import com.wakeforge.app.domain.usecases.alarm.ToggleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Alarms list screen.
 *
 * Manages the alarm list, handles toggle/delete operations,
 * and provides undo functionality for accidental deletions.
 */
@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val createAlarmUseCase: CreateAlarmUseCase,
) : ViewModel() {

    data class AlarmsUiState(
        val alarms: List<Alarm> = emptyList(),
        val showDeleteUndo: Boolean = false,
        val deletedAlarm: Alarm? = null,
    )

    /** One-shot events for the UI layer. */
    sealed interface Event {
        data class NavigateToEdit(val alarmId: String) : Event
    }

    private val _state = MutableStateFlow(AlarmsUiState())
    val state: StateFlow<AlarmsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    private var undoJob: Job? = null

    init {
        loadAlarms()
    }

    /**
     * Observes the alarm list reactively.
     */
    private fun loadAlarms() {
        viewModelScope.launch {
            getAlarmsUseCase()
                .distinctUntilChanged()
                .collect { alarms ->
                    _state.value = _state.value.copy(alarms = alarms)
                }
        }
    }

    /**
     * Toggles the active state of an alarm.
     *
     * @param alarmId The alarm to toggle.
     * @param isActive The new active state.
     */
    fun toggleAlarm(alarmId: String, isActive: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(alarmId, isActive)
        }
    }

    /**
     * Deletes an alarm by ID and shows an undo snackbar.
     *
     * The deletion is staged for 5 seconds. If the user calls [undoDelete]
     * within that window, the alarm is re-inserted. Otherwise, the delete
     * is finalized.
     *
     * @param alarmId The ID of the alarm to delete.
     */
    fun deleteAlarm(alarmId: String) {
        val currentAlarms = _state.value.alarms
        val alarmToDelete = currentAlarms.find { it.id == alarmId } ?: return

        // Remove from visible list immediately
        _state.value = _state.value.copy(
            alarms = currentAlarms.filter { it.id != alarmId },
            showDeleteUndo = true,
            deletedAlarm = alarmToDelete,
        )

        // Start 5-second timer for permanent deletion
        undoJob?.cancel()
        undoJob = viewModelScope.launch {
            delay(5000L)
            // Time expired — permanently delete
            try {
                deleteAlarmUseCase(alarmId)
            } catch (_: Exception) {
                // Silently handle
            }
            _state.value = _state.value.copy(
                showDeleteUndo = false,
                deletedAlarm = null,
            )
        }
    }

    /**
     * Re-inserts the previously deleted alarm and cancels the permanent delete timer.
     */
    fun undoDelete() {
        undoJob?.cancel()
        val deletedAlarm = _state.value.deletedAlarm ?: return

        viewModelScope.launch {
            try {
                createAlarmUseCase(deletedAlarm)
            } catch (_: Exception) {
                // Silently handle
            }
        }

        _state.value = _state.value.copy(
            showDeleteUndo = false,
            deletedAlarm = null,
        )
    }

    /**
     * Navigates to the edit screen for the given alarm.
     *
     * @param alarmId The ID of the alarm to edit.
     */
    fun navigateToEdit(alarmId: String) {
        viewModelScope.launch {
            _events.emit(Event.NavigateToEdit(alarmId))
        }
    }

    override fun onCleared() {
        super.onCleared()
        undoJob?.cancel()
    }
}
