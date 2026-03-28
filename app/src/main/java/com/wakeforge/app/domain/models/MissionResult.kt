package com.wakeforge.app.domain.models

import java.util.UUID

/**
 * Result of a single mission attempt.
 *
 * @property missionId The ID of the mission this result belongs to.
 * @property isCompleted Whether the mission was successfully completed.
 * @property completionTimeMs Time taken to complete (or fail) the mission in milliseconds.
 * @property attempts Number of attempts made before the final result.
 */
data class MissionResult(
    val missionId: String = UUID.randomUUID().toString(),
    val isCompleted: Boolean,
    val completionTimeMs: Long,
    val attempts: Int = 1
) {

    /**
     * Returns true if the mission was completed on the first attempt.
     */
    fun isCompletedOnFirstAttempt(): Boolean = isCompleted && attempts == 1

    /**
     * Returns the average time per attempt.
     */
    fun averageTimePerAttempt(): Long {
        return if (attempts > 0) completionTimeMs / attempts else 0L
    }
}
