package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.repositories.AlarmRepository
import javax.inject.Inject

/**
 * Use case for toggling an alarm's active state.
 *
 * When activating: the alarm is scheduled with the system alarm manager.
 * When deactivating: the scheduled alarm is cancelled.
 *
 * @property repository The [AlarmRepository] used for state toggling and scheduling.
 */
class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Toggles the active state of an alarm.
     *
     * @param id The unique identifier of the alarm to toggle.
     * @param isActive The new active state to apply.
     * @throws IllegalArgumentException if the ID is blank.
     */
    suspend operator fun invoke(id: String, isActive: Boolean) {
        require(id.isNotBlank()) {
            "Alarm ID must not be blank."
        }

        repository.toggleAlarm(id, isActive)
    }
}
