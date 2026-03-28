package com.wakeforge.app.domain.usecases.stats

import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.WakeOutcome
import com.wakeforge.app.domain.repositories.StatsRepository
import javax.inject.Inject

/**
 * Use case for recording a wake-up event and updating the streak.
 *
 * Delegates to the [StatsRepository] to persist the wake record
 * and update streak tracking.
 *
 * @property repository The [StatsRepository] used for recording and streak management.
 */
class RecordWakeUseCase @Inject constructor(
    private val repository: StatsRepository
) {

    /**
     * Records a wake-up event with the given details.
     *
     * This operation:
     * 1. Persists the wake record.
     * 2. Updates the streak counter (increment on success, reset on failure).
     * 3. Aggregates into daily and weekly statistics.
     *
     * @param alarmId The ID of the alarm that triggered this wake event.
     * @param outcome The final [WakeOutcome] (SUCCESS, SNOOZE, or FAILURE).
     * @param snoozeCount Number of times the user snoozed before the final outcome.
     * @param missionType The [MissionType] that was assigned.
     * @param difficulty The [MissionDifficulty] tier of the mission.
     * @param completionTimeMs Time taken to complete or fail the mission.
     */
    suspend operator fun invoke(
        alarmId: String,
        outcome: WakeOutcome,
        snoozeCount: Int = 0,
        missionType: MissionType,
        difficulty: MissionDifficulty,
        completionTimeMs: Long
    ) {
        require(alarmId.isNotBlank()) {
            "Alarm ID must not be blank when recording a wake event."
        }

        require(completionTimeMs >= 0) {
            "Completion time must be non-negative, but was $completionTimeMs."
        }

        require(snoozeCount >= 0) {
            "Snooze count must be non-negative, but was $snoozeCount."
        }

        repository.recordWake(
            alarmId = alarmId,
            outcome = outcome,
            snoozeCount = snoozeCount,
            missionType = missionType,
            difficulty = difficulty,
            completionTimeMs = completionTimeMs
        )
    }
}
