package com.wakeforge.app.data.repository

import com.wakeforge.app.data.mission.MissionEngine
import com.wakeforge.app.data.premium.PremiumManager
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionResult
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.repositories.MissionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionRepositoryImpl @Inject constructor(
    private val missionEngine: MissionEngine,
    private val premiumManager: PremiumManager
) : MissionRepository {

    override suspend fun generateMission(
        type: MissionType,
        difficulty: MissionDifficulty,
        isTimed: Boolean
    ): Mission {
        return missionEngine.generateMission(type, difficulty, isTimed)
    }

    override suspend fun validateCompletion(
        mission: Mission,
        result: MissionResult
    ): Boolean {
        return missionEngine.validateCompletion(mission, result)
    }

    override suspend fun getAvailableMissions(): List<MissionType> {
        val isPremium = try {
            premiumManager.isPremiumOnce()
        } catch (e: Exception) {
            false
        }
        return if (isPremium) {
            MissionType.entries.toList()
        } else {
            MissionType.entries.filter { it != MissionType.SHAKE && it != MissionType.STEP }
        }
    }

    override suspend fun getAvailableDifficulties(type: MissionType): List<MissionDifficulty> {
        return MissionDifficulty.entries.toList()
    }
}
