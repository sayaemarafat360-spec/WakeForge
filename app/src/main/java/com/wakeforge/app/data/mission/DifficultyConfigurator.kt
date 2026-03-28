package com.wakeforge.app.data.mission

import com.wakeforge.app.domain.models.MissionDifficulty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DifficultyConfigurator @Inject constructor() {

    fun getMathProblemCount(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 3
            MissionDifficulty.EASY -> 4
            MissionDifficulty.MEDIUM -> 5
            MissionDifficulty.HARD -> 7
            MissionDifficulty.EXTREME -> 10
        }
    }

    fun getMemoryGridSize(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 3
            MissionDifficulty.EASY -> 3
            MissionDifficulty.MEDIUM -> 4
            MissionDifficulty.HARD -> 5
            MissionDifficulty.EXTREME -> 6
        }
    }

    fun getMemoryPatternLength(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 3
            MissionDifficulty.EASY -> 4
            MissionDifficulty.MEDIUM -> 5
            MissionDifficulty.HARD -> 7
            MissionDifficulty.EXTREME -> 10
        }
    }

    fun getPhraseLength(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 3
            MissionDifficulty.EASY -> 5
            MissionDifficulty.MEDIUM -> 8
            MissionDifficulty.HARD -> 12
            MissionDifficulty.EXTREME -> 20
        }
    }

    fun getShakeCount(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 5
            MissionDifficulty.EASY -> 10
            MissionDifficulty.MEDIUM -> 20
            MissionDifficulty.HARD -> 40
            MissionDifficulty.EXTREME -> 80
        }
    }

    fun getStepCount(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 5
            MissionDifficulty.EASY -> 10
            MissionDifficulty.MEDIUM -> 25
            MissionDifficulty.HARD -> 50
            MissionDifficulty.EXTREME -> 100
        }
    }

    fun getMathTimeLimit(difficulty: MissionDifficulty): Long {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 30_000L
            MissionDifficulty.EASY -> 45_000L
            MissionDifficulty.MEDIUM -> 60_000L
            MissionDifficulty.HARD -> 90_000L
            MissionDifficulty.EXTREME -> 120_000L
        }
    }

    fun getShakeTimeLimit(difficulty: MissionDifficulty): Long {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 30_000L
            MissionDifficulty.EASY -> 25_000L
            MissionDifficulty.MEDIUM -> 20_000L
            MissionDifficulty.HARD -> 15_000L
            MissionDifficulty.EXTREME -> 12_000L
        }
    }

    fun getStepTimeLimit(difficulty: MissionDifficulty): Long {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 60_000L
            MissionDifficulty.EASY -> 90_000L
            MissionDifficulty.MEDIUM -> 120_000L
            MissionDifficulty.HARD -> 180_000L
            MissionDifficulty.EXTREME -> 300_000L
        }
    }

    fun getMathMaxOperand(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 9
            MissionDifficulty.EASY -> 20
            MissionDifficulty.MEDIUM -> 50
            MissionDifficulty.HARD -> 100
            MissionDifficulty.EXTREME -> 200
        }
    }

    fun getMathOperationTypes(difficulty: MissionDifficulty): List<MathOperation> {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> listOf(MathOperation.ADD, MathOperation.SUBTRACT)
            MissionDifficulty.EASY -> listOf(MathOperation.ADD, MathOperation.SUBTRACT, MathOperation.MULTIPLY)
            MissionDifficulty.MEDIUM -> listOf(
                MathOperation.ADD,
                MathOperation.SUBTRACT,
                MathOperation.MULTIPLY,
                MathOperation.DIVIDE
            )
            MissionDifficulty.HARD -> listOf(
                MathOperation.ADD,
                MathOperation.SUBTRACT,
                MathOperation.MULTIPLY,
                MathOperation.DIVIDE
            )
            MissionDifficulty.EXTREME -> MathOperation.values().toList()
        }
    }

    fun getMathChainLength(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 1
            MissionDifficulty.EASY -> 1
            MissionDifficulty.MEDIUM -> 1
            MissionDifficulty.HARD -> 2
            MissionDifficulty.EXTREME -> 3
        }
    }
}

enum class MathOperation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    MODULO
}
