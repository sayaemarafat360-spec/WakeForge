package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.repositories.AlarmRepository
import javax.inject.Inject

/**
 * Use case for updating an existing alarm.
 *
 * Cancels the old alarm schedule, persists the updated alarm data,
 * and reschedules if the alarm is active.
 *
 * @property repository The [AlarmRepository] used for persistence and scheduling.
 */
class UpdateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Updates an existing alarm.
     *
     * The workflow is:
     * 1. Cancel the existing scheduled alarm.
     * 2. Update the alarm data with the new values.
     * 3. If the alarm is active, reschedule it.
     *
     * @param alarm The [Alarm] with updated fields. Must have a valid existing ID.
     * @throws IllegalArgumentException if the alarm ID is blank.
     */
    suspend operator fun invoke(alarm: Alarm) {
        require(alarm.id.isNotBlank()) {
            "Alarm ID must not be blank for an update operation."
        }

        // Cancel the old scheduled alarm first
        repository.cancelAlarm(alarm.id)

        // Persist the updated alarm
        repository.updateAlarm(alarm)

        // Reschedule if active
        if (alarm.isActive) {
            repository.scheduleAlarm(alarm)
        }
    }
}
