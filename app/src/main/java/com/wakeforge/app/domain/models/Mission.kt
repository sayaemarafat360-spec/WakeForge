package com.wakeforge.app.domain.models

import java.util.UUID

/**
 * Sealed class representing all mission types that can be assigned to an alarm.
 *
 * @property id Unique identifier for this mission instance.
 * @property type The [MissionType] of this mission.
 * @property difficulty The [MissionDifficulty] tier for this mission.
 * @property isTimed Whether the mission has a time limit.
 * @property timeLimitMs Time limit in milliseconds (0 if not timed).
 */
sealed class Mission(
    val id: String = UUID.randomUUID().toString(),
    val type: MissionType,
    val difficulty: MissionDifficulty,
    val isTimed: Boolean = true,
    val timeLimitMs: Long = 0L
) {

    /**
     * Math-based mission requiring the user to solve arithmetic problems.
     *
     * @property problems List of [MathProblem] instances to solve.
     */
    data class MathMission(
        val id: String = UUID.randomUUID().toString(),
        val type: MissionType = MissionType.MATH,
        val difficulty: MissionDifficulty,
        val isTimed: Boolean = true,
        val timeLimitMs: Long = 0L,
        val problems: List<MathProblem>
    ) : Mission(id, type, difficulty, isTimed, timeLimitMs)

    /**
     * Memory-based mission requiring pattern recall on a grid.
     *
     * @property gridSize The number of cells per row/column of the grid.
     * @property pattern The sequence of cell indices to memorize.
     * @property patternLength The number of cells in the pattern.
     */
    data class MemoryMission(
        val id: String = UUID.randomUUID().toString(),
        val type: MissionType = MissionType.MEMORY,
        val difficulty: MissionDifficulty,
        val isTimed: Boolean = true,
        val timeLimitMs: Long = 0L,
        val gridSize: Int,
        val pattern: List<Int>,
        val patternLength: Int
    ) : Mission(id, type, difficulty, isTimed, timeLimitMs)

    /**
     * Typing-based mission requiring accurate phrase reproduction.
     *
     * @property phrase The text the user must type.
     * @property requiredAccuracy Minimum accuracy (0.0–1.0) to pass.
     */
    data class TypePhraseMission(
        val id: String = UUID.randomUUID().toString(),
        val type: MissionType = MissionType.TYPE_PHRASE,
        val difficulty: MissionDifficulty,
        val isTimed: Boolean = true,
        val timeLimitMs: Long = 0L,
        val phrase: String,
        val requiredAccuracy: Float = 0.95f
    ) : Mission(id, type, difficulty, isTimed, timeLimitMs)

    /**
     * Shake-based mission requiring physical device movement.
     *
     * @property requiredShakes Total shakes needed to complete the mission.
     * @property currentShakes Number of shakes registered so far.
     * @property shakeThreshold Acceleration threshold to register a valid shake.
     */
    data class ShakeMission(
        val id: String = UUID.randomUUID().toString(),
        val type: MissionType = MissionType.SHAKE,
        val difficulty: MissionDifficulty,
        val isTimed: Boolean = true,
        val timeLimitMs: Long = 0L,
        val requiredShakes: Int,
        val currentShakes: Int = 0,
        val shakeThreshold: Float = 2.5f
    ) : Mission(id, type, difficulty, isTimed, timeLimitMs) {

        /**
         * Returns a copy with the shake count incremented by 1.
         */
        fun incrementShake(): ShakeMission = copy(currentShakes = currentShakes + 1)

        /**
         * Returns true if the required number of shakes has been reached.
         */
        fun isComplete(): Boolean = currentShakes >= requiredShakes
    }

    /**
     * Step-based mission requiring walking to dismiss the alarm.
     *
     * @property requiredSteps Total steps needed to complete the mission.
     * @property currentSteps Number of steps registered so far.
     */
    data class StepMission(
        val id: String = UUID.randomUUID().toString(),
        val type: MissionType = MissionType.STEP,
        val difficulty: MissionDifficulty,
        val isTimed: Boolean = true,
        val timeLimitMs: Long = 0L,
        val requiredSteps: Int,
        val currentSteps: Int = 0
    ) : Mission(id, type, difficulty, isTimed, timeLimitMs) {

        /**
         * Returns a copy with the step count incremented by the given amount.
         */
        fun addSteps(count: Int): StepMission = copy(currentSteps = currentSteps + count)

        /**
         * Returns true if the required number of steps has been reached.
         */
        fun isComplete(): Boolean = currentSteps >= requiredSteps
    }
}

/**
 * A single math problem consisting of a question string and its integer answer.
 */
data class MathProblem(
    val question: String,
    val answer: Int
)
