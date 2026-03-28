package com.wakeforge.app.domain.models

/**
 * Configuration for snooze behavior during an alarm.
 *
 * @property intervalMinutes Base number of minutes between snooze activations.
 * @property maxCount Maximum number of snoozes allowed before the alarm becomes final.
 * @property smartEscalationEnabled Whether snoozing increases mission difficulty.
 */
data class SnoozeConfig(
    val intervalMinutes: Int,
    val maxCount: Int,
    val smartEscalationEnabled: Boolean
) {

    companion object {
        /** Minimum snooze interval in seconds. */
        const val MIN_SNOOZE_INTERVAL_SECONDS = 30

        /** Maximum number of missions in a multi-step chain. */
        const val MAX_MISSION_CHAIN_LENGTH = 5

        /** Seconds to reduce from the interval per snooze when escalation is enabled. */
        private const val ESCALATION_REDUCTION_SECONDS = 30
    }

    /**
     * Calculates the snooze interval after escalation.
     *
     * Each successive snooze reduces the interval by 30 seconds,
     * down to a minimum of 30 seconds. This makes it progressively harder
     * to stay asleep.
     *
     * @param snoozeNumber The 1-based index of the current snooze.
     * @return The escalated interval in seconds.
     */
    fun calculateEscalatedInterval(snoozeNumber: Int): Int {
        val baseIntervalSeconds = intervalMinutes * 60
        val reduction = (snoozeNumber - 1) * ESCALATION_REDUCTION_SECONDS
        val escalated = baseIntervalSeconds - reduction
        return maxOf(escalated, MIN_SNOOZE_INTERVAL_SECONDS)
    }

    /**
     * Calculates the escalated difficulty based on snooze count.
     *
     * Each snooze advances the difficulty by one tier when smart escalation
     * is enabled, capped at [MissionDifficulty.EXTREME].
     *
     * @param baseDifficulty The original difficulty before escalation.
     * @param snoozeNumber The 1-based index of the current snooze.
     * @return The escalated [MissionDifficulty].
     */
    fun calculateEscalatedDifficulty(
        baseDifficulty: MissionDifficulty,
        snoozeNumber: Int
    ): MissionDifficulty {
        if (!smartEscalationEnabled) return baseDifficulty

        var difficulty = baseDifficulty
        repeat(snoozeNumber) {
            difficulty = difficulty.next()
        }
        return difficulty
    }

    /**
     * Calculates the escalated step count for multi-step missions.
     *
     * Each snooze increases the required step count by 1, capped at
     * [MAX_MISSION_CHAIN_LENGTH].
     *
     * @param baseCount The original step count before escalation.
     * @param snoozeNumber The 1-based index of the current snooze.
     * @return The escalated step count.
     */
    fun calculateEscalatedStepCount(baseCount: Int, snoozeNumber: Int): Int {
        val escalated = baseCount + snoozeNumber
        return minOf(escalated, MAX_MISSION_CHAIN_LENGTH)
    }
}
