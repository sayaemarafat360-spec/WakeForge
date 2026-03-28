package com.wakeforge.app.data.mission.generators

import com.wakeforge.app.data.mission.DifficultyConfigurator
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryGenerator @Inject constructor(
    private val configurator: DifficultyConfigurator
) {

    fun generate(difficulty: MissionDifficulty): Mission.MemoryMission {
        val gridSize = configurator.getMemoryGridSize(difficulty)
        val patternLength = configurator.getMemoryPatternLength(difficulty)

        val totalTiles = gridSize * gridSize
        require(patternLength <= totalTiles) {
            "Pattern length ($patternLength) cannot exceed total tiles ($totalTiles) for grid size $gridSize"
        }

        val pattern = generateUniquePositions(totalTiles, patternLength)

        return Mission.MemoryMission(
            id = java.util.UUID.randomUUID().toString(),
            type = com.wakeforge.app.domain.models.MissionType.MEMORY,
            difficulty = difficulty,
            isTimed = true,
            timeLimitMs = configurator.getMathTimeLimit(difficulty),
            gridSize = gridSize,
            pattern = pattern,
            patternLength = patternLength
        )
    }

    private fun generateUniquePositions(totalTiles: Int, count: Int): List<Int> {
        val positions = mutableListOf<Int>()
        val available = (0 until totalTiles).toMutableList()

        repeat(count) {
            val index = Random.nextInt(available.size)
            positions.add(available.removeAt(index))
        }

        return positions.sorted()
    }
}
