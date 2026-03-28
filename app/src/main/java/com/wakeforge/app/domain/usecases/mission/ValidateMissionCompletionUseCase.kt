package com.wakeforge.app.domain.usecases.mission

import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionResult
import com.wakeforge.app.domain.repositories.MissionRepository
import javax.inject.Inject

/**
 * Use case for validating whether a mission was completed successfully.
 *
 * Delegates to the repository for mission-specific validation logic
 * (e.g., checking math answers, memory patterns, shake counts, etc.).
 *
 * @property repository The [MissionRepository] used for validation.
 */
class ValidateMissionCompletionUseCase @Inject constructor(
    private val repository: MissionRepository
) {

    /**
     * Validates a mission result against the mission requirements.
     *
     * @param mission The [Mission] that was attempted.
     * @param result The [MissionResult] containing the user's performance data.
     * @return true if the mission was completed successfully, false otherwise.
     */
    suspend operator fun invoke(mission: Mission, result: MissionResult): Boolean {
        // Pre-checks before delegating to the repository.
        if (!result.isCompleted) return false
        if (result.missionId != mission.id) return false

        return repository.validateCompletion(mission, result)
    }
}
