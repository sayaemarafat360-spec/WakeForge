package com.wakeforge.app.domain.models

import java.util.UUID

/**
 * Records the outcome of a single wake-up event.
 *
 * @property id Unique identifier for this record.
 * @property alarmId The ID of the alarm that triggered this wake event.
 * @property timestamp When the wake event occurred.
 * @property outcome The final [WakeOutcome] of the wake attempt.
 * @property snoozeCount Number of times the user snoozed before the final outcome.
 * @property missionType The type of mission assigned to this wake event.
 * @property difficulty The difficulty level of the mission.
 * @property completionTimeMs Time taken to complete (or fail) the mission, in milliseconds.
 * @property createdAt When this record was persisted.
 */
data class WakeRecord(
    val id: String = UUID.randomUUID().toString(),
    val alarmId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: WakeOutcome,
    val snoozeCount: Int = 0,
    val missionType: MissionType,
    val difficulty: MissionDifficulty,
    val completionTimeMs: Long,
    val createdAt: Long = System.currentTimeMillis()
) {

    /**
     * Returns true if the user successfully completed the mission and dismissed the alarm.
     */
    fun isSuccessful(): Boolean = outcome == WakeOutcome.SUCCESS

    /**
     * Returns true if the user failed the mission or the alarm timed out.
     */
    fun isFailure(): Boolean = outcome == WakeOutcome.FAILURE
}
