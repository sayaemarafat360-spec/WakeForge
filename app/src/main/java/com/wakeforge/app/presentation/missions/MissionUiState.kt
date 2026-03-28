package com.wakeforge.app.presentation.missions

import com.wakeforge.app.domain.models.MathProblem
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType

/**
 * UI state for the mission challenge screen.
 *
 * Holds all transient state required by the various mission challenge composables
 * (math, memory, type, shake, step) and the overall mission flow.
 *
 * @property mission              The current [Mission] being attempted.
 * @property currentStepIndex     Zero-based index of the current step in a multi-step chain.
 * @property totalSteps           Total number of steps in the chain (1 for single-step).
 * @property currentProblem       The [MathProblem] currently displayed (math missions only).
 * @property mathProblemIndex     Index of the current problem within the math mission's problem list.
 * @property userAnswer           Text entered by the user for the current math problem.
 * @property answerFeedback       Feedback state for the last submitted answer.
 * @property memoryPattern        The sequence of tile indices the user must memorize.
 * @property userPattern          The sequence of tile indices the user has tapped so far.
 * @property isShowingPattern     Whether the memory pattern is currently being shown.
 * @property revealedTileIndex    Index of the tile currently being revealed (–1 if none).
 * @property memoryErrorIndex     Index of the last incorrectly tapped tile (–1 if none).
 * @property targetPhrase         The phrase the user must type (type missions only).
 * @property typedPhrase          What the user has typed so far.
 * @property showHint             Whether the first-letter hint is visible.
 * @property shakeProgress        Number of shakes detected so far.
 * @property shakeTarget          Total shakes required to complete the mission.
 * @property stepProgress         Number of steps detected so far.
 * @property stepTarget           Total steps required to complete the mission.
 * @property stepSensorAvailable  Whether the step sensor is available on the device.
 * @property timeRemainingMs      Milliseconds remaining on the mission timer.
 * @property totalTimeMs          Total time allocated for the timed mission.
 * @property isCompleted          Whether the mission was completed successfully.
 * @property isFailed             Whether the mission timed out or was abandoned.
 * @property isLoading            Whether the mission is being generated / loaded.
 * @property missionType          The [MissionType] of the current mission.
 * @property difficulty           The [MissionDifficulty] tier.
 * @property attempts             Number of failed attempts on the current sub-challenge.
 */
data class MissionUiState(
    val mission: Mission? = null,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 1,
    val currentProblem: MathProblem? = null,
    val mathProblemIndex: Int = 0,
    val userAnswer: String = "",
    val answerFeedback: AnswerFeedback = AnswerFeedback.Idle,
    val memoryPattern: List<Int> = emptyList(),
    val userPattern: List<Int> = emptyList(),
    val isShowingPattern: Boolean = false,
    val revealedTileIndex: Int = -1,
    val memoryErrorIndex: Int = -1,
    val targetPhrase: String = "",
    val typedPhrase: String = "",
    val showHint: Boolean = false,
    val shakeProgress: Int = 0,
    val shakeTarget: Int = 0,
    val stepProgress: Int = 0,
    val stepTarget: Int = 0,
    val stepSensorAvailable: Boolean = true,
    val timeRemainingMs: Long = 0L,
    val totalTimeMs: Long = 0L,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val isLoading: Boolean = true,
    val missionType: MissionType = MissionType.MATH,
    val difficulty: MissionDifficulty = MissionDifficulty.MEDIUM,
    val attempts: Int = 0,
)

/**
 * Feedback state after the user submits a math answer.
 */
enum class AnswerFeedback {
    /** No answer has been submitted yet. */
    Idle,
    /** The submitted answer was correct. */
    Correct,
    /** The submitted answer was incorrect. */
    Incorrect,
}

/**
 * One-shot events emitted by [MissionViewModel] that the UI layer reacts to.
 */
sealed interface MissionEvent {

    /** The entire multi-step mission chain is complete. */
    data object MissionCompleted : MissionEvent

    /** The mission timed out or was abandoned. */
    data object MissionFailed : MissionEvent

    /** One step in a multi-step chain completed; the next mission is ready. */
    data class StepCompleted(val nextMission: Mission) : MissionEvent
}
