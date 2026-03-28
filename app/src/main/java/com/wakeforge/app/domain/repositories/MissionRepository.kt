package com.wakeforge.app.domain.repositories

import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionResult
import com.wakeforge.app.domain.models.MissionType

/**
 * Repository interface for mission generation, validation, and availability queries.
 */
interface MissionRepository {

    /**
     * Generates a new mission of the specified type and difficulty.
     *
     * @param type The [MissionType] to generate.
     * @param difficulty The [MissionDifficulty] tier for the mission.
     * @param isTimed Whether the mission should have a time limit.
     * @return A fully constructed [Mission] instance ready to be presented to the user.
     */
    suspend fun generateMission(
        type: MissionType,
        difficulty: MissionDifficulty,
        isTimed: Boolean
    ): Mission

    /**
     * Validates whether a mission result meets the completion criteria.
     *
     * @param mission The [Mission] that was attempted.
     * @param result The [MissionResult] containing the user's performance data.
     * @return true if the mission was successfully completed according to its rules.
     */
    suspend fun validateCompletion(
        mission: Mission,
        result: MissionResult
    ): Boolean

    /**
     * Returns all mission types available to the current user.
     *
     * Premium mission types are only included if the user has an active subscription.
     *
     * @return A list of available [MissionType] values.
     */
    suspend fun getAvailableMissions(): List<MissionType>

    /**
     * Returns all difficulty tiers available for a given mission type.
     *
     * @param type The [MissionType] to query difficulties for.
     * @return A list of available [MissionDifficulty] values.
     */
    suspend fun getAvailableDifficulties(type: MissionType): List<MissionDifficulty>
}
