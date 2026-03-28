package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.repositories.AlarmRepository
import javax.inject.Inject

/**
 * Use case for creating a new alarm with validation.
 *
 * Validates all alarm fields before persisting and scheduling the alarm.
 * Throws [IllegalArgumentException] if validation fails.
 *
 * @property repository The [AlarmRepository] used for persistence and scheduling.
 */
class CreateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Creates and schedules a new alarm after validating its fields.
     *
     * @param alarm The [Alarm] to create. Fields are validated as follows:
     *   - [Alarm.hour] must be in range 0–23
     *   - [Alarm.minute] must be in range 0–59
     *   - [Alarm.snoozeIntervalMinutes] must be in range 1–30
     *   - [Alarm.maxSnoozeCount] must be in range 0–10
     *   - [Alarm.multiStepCount] must be in range 2–5 when [Alarm.multiStepEnabled] is true
     * @throws IllegalArgumentException if any field fails validation.
     */
    suspend operator fun invoke(alarm: Alarm) {
        validate(alarm)
        repository.createAlarm(alarm)
        if (alarm.isActive) {
            repository.scheduleAlarm(alarm)
        }
    }

    private fun validate(alarm: Alarm) {
        require(alarm.hour in 0..23) {
            "Alarm hour must be between 0 and 23, but was ${alarm.hour}."
        }

        require(alarm.minute in 0..59) {
            "Alarm minute must be between 0 and 59, but was ${alarm.minute}."
        }

        require(alarm.snoozeIntervalMinutes in 1..30) {
            "Snooze interval must be between 1 and 30 minutes, but was ${alarm.snoozeIntervalMinutes}."
        }

        require(alarm.maxSnoozeCount in 0..10) {
            "Max snooze count must be between 0 and 10, but was ${alarm.maxSnoozeCount}."
        }

        require(!alarm.multiStepEnabled || alarm.multiStepCount in 2..5) {
            "Multi-step count must be between 2 and 5 when multi-step is enabled, but was ${alarm.multiStepCount}."
        }

        require(alarm.gradualVolumeDurationSeconds in 0..300) {
            "Gradual volume duration must be between 0 and 300 seconds, but was ${alarm.gradualVolumeDurationSeconds}."
        }
    }
}
