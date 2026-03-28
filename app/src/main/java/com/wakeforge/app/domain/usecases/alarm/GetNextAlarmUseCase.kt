package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.repositories.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving the next upcoming active alarm.
 *
 * This is useful for displaying the next alarm countdown on the home screen
 * or for the lock screen widget.
 *
 * @property repository The [AlarmRepository] used for data retrieval.
 */
class GetNextAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Returns a reactive [Flow] of the next alarm that will fire.
     *
     * The alarm must be active ([Alarm.isActive] = true) and must have the
     * nearest upcoming fire time. Returns null if no active alarms exist.
     *
     * @return A [Flow] emitting the next [Alarm] or null.
     */
    operator fun invoke(): Flow<Alarm?> {
        return repository.getNextActiveAlarm()
    }
}
