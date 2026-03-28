package com.wakeforge.app.data.mission

import com.wakeforge.app.data.mission.generators.MathGenerator
import com.wakeforge.app.data.mission.generators.MemoryGenerator
import com.wakeforge.app.data.mission.generators.PhraseGenerator
import com.wakeforge.app.data.mission.generators.ShakeEvaluator
import com.wakeforge.app.data.mission.generators.StepEvaluator
import com.wakeforge.app.domain.models.MathProblem
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionResult
import com.wakeforge.app.domain.models.MissionType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionEngine @Inject constructor(
    private val mathGenerator: MathGenerator,
    private val memoryGenerator: MemoryGenerator,
    private val phraseGenerator: PhraseGenerator,
    private val shakeEvaluator: ShakeEvaluator,
    private val stepEvaluator: StepEvaluator,
    private val difficultyConfigurator: DifficultyConfigurator
) {

    fun generateMission(
        type: MissionType,
        difficulty: MissionDifficulty,
        isTimed: Boolean
    ): Mission {
        val missionId = UUID.randomUUID().toString()
        val timeLimitMs = if (isTimed) getTimeLimit(difficulty) else 0L

        return when (type) {
            MissionType.MATH -> {
                val problems = mathGenerator.generate(difficulty)
                Mission.MathMission(
                    id = missionId,
                    type = MissionType.MATH,
                    difficulty = difficulty,
                    isTimed = isTimed,
                    timeLimitMs = timeLimitMs,
                    problems = problems
                )
            }

            MissionType.MEMORY -> {
                val memory = memoryGenerator.generate(difficulty)
                Mission.MemoryMission(
                    id = missionId,
                    type = MissionType.MEMORY,
                    difficulty = difficulty,
                    isTimed = isTimed,
                    timeLimitMs = timeLimitMs,
                    gridSize = memory.gridSize,
                    pattern = memory.pattern,
                    patternLength = memory.patternLength
                )
            }

            MissionType.TYPE_PHRASE -> {
                val phrase = phraseGenerator.generate(difficulty)
                Mission.TypePhraseMission(
                    id = missionId,
                    type = MissionType.TYPE_PHRASE,
                    difficulty = difficulty,
                    isTimed = isTimed,
                    timeLimitMs = timeLimitMs,
                    phrase = phrase.phrase,
                    requiredAccuracy = phrase.requiredAccuracy
                )
            }

            MissionType.SHAKE -> {
                val shake = shakeEvaluator.createShakeMission(difficulty)
                Mission.ShakeMission(
                    id = missionId,
                    type = MissionType.SHAKE,
                    difficulty = difficulty,
                    isTimed = isTimed,
                    timeLimitMs = timeLimitMs,
                    requiredShakes = shake.requiredShakes,
                    currentShakes = 0,
                    shakeThreshold = shake.shakeThreshold
                )
            }

            MissionType.STEP -> {
                val step = stepEvaluator.createStepMission(difficulty)
                Mission.StepMission(
                    id = missionId,
                    type = MissionType.STEP,
                    difficulty = difficulty,
                    isTimed = isTimed,
                    timeLimitMs = timeLimitMs,
                    requiredSteps = step.requiredSteps,
                    currentSteps = 0
                )
            }
        }
    }

    fun getTimeLimit(difficulty: MissionDifficulty): Long {
        return when (difficulty) {
            MissionDifficulty.TRIVIAL -> 120_000L
            MissionDifficulty.EASY -> 90_000L
            MissionDifficulty.MEDIUM -> 60_000L
            MissionDifficulty.HARD -> 45_000L
            MissionDifficulty.EXTREME -> 30_000L
        }
    }

    fun validateCompletion(mission: Mission, result: MissionResult): Boolean {
        return when (mission) {
            is Mission.MathMission -> {
                result.isCompleted
            }
            is Mission.MemoryMission -> {
                result.isCompleted
            }
            is Mission.TypePhraseMission -> {
                result.isCompleted
            }
            is Mission.ShakeMission -> {
                mission.currentShakes >= mission.requiredShakes
            }
            is Mission.StepMission -> {
                mission.currentSteps >= mission.requiredSteps
            }
        }
    }
}
