package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.repositories.AlarmRepository
import javax.inject.Inject

/**
 * Use case for manually scheduling an alarm.
 *
 * Validates the alarm before scheduling it through the repository.
 * Typically used after modifying an alarm or when re-enabling a cancelled alarm.
 *
 * @property repository The [AlarmRepository] used for scheduling.
 */
class ScheduleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Validates and schedules an alarm.
     *
     * @param alarm The [Alarm] to schedule.
     * @throws IllegalArgumentException if the alarm is not active or has invalid fields.
     */
    suspend operator fun invoke(alarm: Alarm) {
        require(alarm.isActive) {
            "Cannot schedule an inactive alarm."
        }

        require(alarm.hour in 0..23) {
            "Alarm hour must be between 0 and 23, but was ${alarm.hour}."
        }

        require(alarm.minute in 0..59) {
            "Alarm minute must be between 0 and 59, but was ${alarm.minute}."
        }

        repository.scheduleAlarm(alarm)
    }
}
