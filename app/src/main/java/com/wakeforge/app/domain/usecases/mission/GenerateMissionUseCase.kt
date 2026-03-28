package com.wakeforge.app.domain.usecases.mission

import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.repositories.MissionRepository
import javax.inject.Inject

/**
 * Use case for generating a mission with an appropriate time limit.
 *
 * Generates a mission via the repository and applies a difficulty-scaled
 * time limit when timed mode is requested.
 *
 * @property repository The [MissionRepository] used for mission generation.
 */
class GenerateMissionUseCase @Inject constructor(
    private val repository: MissionRepository
) {

    companion object {
        /** Base time limit in milliseconds for a timed mission at MEDIUM difficulty. */
        private const val BASE_TIME_LIMIT_MS = 60_000L

        /** Minimum time limit in milliseconds (10 seconds). */
        private const val MIN_TIME_LIMIT_MS = 10_000L

        /** Maximum time limit in milliseconds (5 minutes). */
        private const val MAX_TIME_LIMIT_MS = 300_000L
    }

    /**
     * Generates a mission with the specified parameters.
     *
     * When [isTimed] is true, a time limit is calculated based on the
     * difficulty multiplier:
     * - TRIVIAL (0.5×): 30 seconds
     * - EASY (0.75×): 45 seconds
     * - MEDIUM (1.0×): 60 seconds
     * - HARD (1.5×): 90 seconds
     * - EXTREME (2.0×): 120 seconds
     *
     * @param type The [MissionType] to generate.
     * @param difficulty The [MissionDifficulty] tier.
     * @param isTimed Whether the mission should have a time limit. Defaults to true.
     * @return A [Mission] with the generated content and calculated time limit.
     */
    suspend operator fun invoke(
        type: MissionType,
        difficulty: MissionDifficulty,
        isTimed: Boolean = true
    ): Mission {
        val mission = repository.generateMission(type, difficulty, isTimed)

        if (!isTimed) return mission

        // Calculate time limit based on difficulty multiplier.
        // Higher difficulty = more time (harder problems take longer to solve).
        val timeLimitMs = (BASE_TIME_LIMIT_MS * difficulty.multiplier)
            .toLong()
            .coerceIn(MIN_TIME_LIMIT_MS, MAX_TIME_LIMIT_MS)

        return applyTimeLimit(mission, timeLimitMs)
    }

    /**
     * Applies a time limit to the generated mission by returning a copy with the limit set.
     */
    private fun applyTimeLimit(mission: Mission, timeLimitMs: Long): Mission {
        return when (mission) {
            is Mission.MathMission -> mission.copy(timeLimitMs = timeLimitMs, isTimed = true)
            is Mission.MemoryMission -> mission.copy(timeLimitMs = timeLimitMs, isTimed = true)
            is Mission.TypePhraseMission -> mission.copy(timeLimitMs = timeLimitMs, isTimed = true)
            is Mission.ShakeMission -> mission.copy(timeLimitMs = timeLimitMs, isTimed = true)
            is Mission.StepMission -> mission.copy(timeLimitMs = timeLimitMs, isTimed = true)
        }
    }
}
