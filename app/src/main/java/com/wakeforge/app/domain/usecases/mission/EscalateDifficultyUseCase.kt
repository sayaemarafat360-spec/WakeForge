package com.wakeforge.app.domain.usecases.mission

import com.wakeforge.app.domain.models.MissionDifficulty
import javax.inject.Inject

/**
 * Use case for escalating mission difficulty based on snooze count.
 *
 * When smart escalation is enabled, each snooze increases the difficulty
 * by one tier, making it progressively harder to go back to sleep.
 * The difficulty is capped at [MissionDifficulty.EXTREME].
 *
 * @property repository No repository dependency needed — this is pure domain logic.
 */
class EscalateDifficultyUseCase @Inject constructor() {

    /**
     * Calculates the escalated difficulty based on the number of snoozes.
     *
     * @param baseDifficulty The original [MissionDifficulty] of the alarm.
     * @param snoozeCount The number of times the user has snoozed.
     * @param smartEscalation Whether smart escalation is enabled for this alarm.
     * @return The escalated [MissionDifficulty], or the base difficulty if escalation is disabled.
     */
    operator fun invoke(
        baseDifficulty: MissionDifficulty,
        snoozeCount: Int,
        smartEscalation: Boolean
    ): MissionDifficulty {
        if (!smartEscalation) return baseDifficulty
        if (snoozeCount <= 0) return baseDifficulty

        var difficulty = baseDifficulty
        repeat(snoozeCount) {
            difficulty = difficulty.next()
        }
        return difficulty
    }
}
