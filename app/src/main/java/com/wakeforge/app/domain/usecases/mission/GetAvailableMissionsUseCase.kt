package com.wakeforge.app.domain.usecases.mission

import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.repositories.MissionRepository
import javax.inject.Inject

/**
 * Use case for retrieving the list of available mission types.
 *
 * Returns only the missions the current user is allowed to access.
 * Premium missions are excluded unless the user has an active subscription
 * or temporary rewarded access.
 *
 * @property repository The [MissionRepository] used to query available missions.
 */
class GetAvailableMissionsUseCase @Inject constructor(
    private val repository: MissionRepository
) {

    /**
     * Returns the list of mission types available to the current user.
     *
     * @return A list of [MissionType] values the user can select from.
     */
    suspend operator fun invoke(): List<MissionType> {
        return repository.getAvailableMissions()
    }
}
