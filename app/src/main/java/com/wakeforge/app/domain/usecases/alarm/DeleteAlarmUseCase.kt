package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.repositories.AlarmRepository
import javax.inject.Inject

/**
 * Use case for deleting an alarm.
 *
 * Cancels the alarm schedule and removes the alarm from persistent storage.
 *
 * @property repository The [AlarmRepository] used for deletion and cancellation.
 */
class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Deletes an alarm by its ID.
     *
     * The workflow is:
     * 1. Cancel the scheduled alarm to prevent future triggers.
     * 2. Delete the alarm from the data store.
     *
     * @param id The unique identifier of the alarm to delete.
     * @throws IllegalArgumentException if the ID is blank.
     */
    suspend operator fun invoke(id: String) {
        require(id.isNotBlank()) {
            "Alarm ID must not be blank for deletion."
        }

        repository.cancelAlarm(id)
        repository.deleteAlarm(id)
    }
}
