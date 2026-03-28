package com.wakeforge.app.presentation.missions

import android.hardware.SensorEventListener
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.data.mission.generators.ShakeEvaluator
import com.wakeforge.app.data.mission.generators.StepEvaluator
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.MissionResult
import com.wakeforge.app.domain.usecases.mission.GenerateMissionUseCase
import com.wakeforge.app.domain.usecases.mission.ValidateMissionCompletionUseCase
import com.wakeforge.app.domain.usecases.stats.RecordWakeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel that orchestrates mission challenge logic.
 *
 * Manages the full lifecycle of a mission: generation, timer, user interaction
 * for each mission type, multi-step chaining, and completion/failure events.
 *
 * @param generateMissionUseCase          Generates missions based on type and difficulty.
 * @param validateMissionCompletionUseCase Validates whether a mission was completed.
 * @param recordWakeUseCase               Records the wake outcome to the stats repository.
 * @param shakeEvaluator                  Evaluates device shake sensor input.
 * @param stepEvaluator                   Evaluates step counter sensor input.
 * @param savedStateHandle                Navigation arguments (alarmId, missionType, difficulty, snoozeCount).
 */
@HiltViewModel
class MissionViewModel @Inject constructor(
    private val generateMissionUseCase: GenerateMissionUseCase,
    private val validateMissionCompletionUseCase: ValidateMissionCompletionUseCase,
    private val recordWakeUseCase: RecordWakeUseCase,
    private val shakeEvaluator: ShakeEvaluator,
    private val stepEvaluator: StepEvaluator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val KEY_ALARM_ID = "alarmId"
        private const val KEY_MISSION_TYPE = "missionType"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_SNOOZE_COUNT = "snoozeCount"

        /** Timer tick interval in milliseconds. */
        private const val TIMER_TICK_MS = 100L

        /** Memory pattern reveal delay per tile in milliseconds. */
        private const val MEMORY_REVEAL_DELAY_MS = 800L

        /** Delay before auto-advancing to the next math problem after a correct answer. */
        private const val MATH_ADVANCE_DELAY_MS = 500L

        /** Delay before showing the first-letter hint for type missions. */
        private const val HINT_DELAY_FRACTION = 0.6f
    }

    // ── State ─────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MissionEvent>()
    val events: SharedFlow<MissionEvent> = _events.asSharedFlow()

    // ── Navigation args ──────────────────────────────────────────────────

    private val alarmId: String = savedStateHandle[KEY_ALARM_ID] ?: ""
    private val missionTypeName: String = savedStateHandle[KEY_MISSION_TYPE] ?: MissionType.MATH.name
    private val difficultyName: String = savedStateHandle[KEY_DIFFICULTY] ?: MissionDifficulty.MEDIUM.name
    private val snoozeCount: Int = savedStateHandle[KEY_SNOOZE_COUNT] ?: 0

    private val missionType: MissionType = try {
        MissionType.valueOf(missionTypeName)
    } catch (_: IllegalArgumentException) {
        MissionType.MATH
    }

    private val difficulty: MissionDifficulty = try {
        MissionDifficulty.valueOf(difficultyName)
    } catch (_: IllegalArgumentException) {
        MissionDifficulty.MEDIUM
    }

    // ── Private fields ───────────────────────────────────────────────────

    private var timerJob: Job? = null
    private var memoryRevealJob: Job? = null
    private var hintJob: Job? = null
    private var missionStartTime: Long = 0L

    private var shakeListener: SensorEventListener? = null
    private var stepListener: SensorEventListener? = null

    // ── Init ─────────────────────────────────────────────────────────────

    init {
        Timber.d("MissionViewModel init: type=$missionType, difficulty=$difficulty, snooze=$snoozeCount")
        generateMission()
    }

    // ── Mission Generation ───────────────────────────────────────────────

    private fun generateMission() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val mission = generateMissionUseCase(
                    type = missionType,
                    difficulty = difficulty,
                    isTimed = true,
                )

                missionStartTime = System.currentTimeMillis()

                _uiState.value = _uiState.value.copy(
                    mission = mission,
                    isLoading = false,
                    missionType = mission.type,
                    difficulty = mission.difficulty,
                    totalTimeMs = mission.timeLimitMs,
                    timeRemainingMs = mission.timeLimitMs,
                    attempts = 0,
                    answerFeedback = AnswerFeedback.Idle,
                    userAnswer = "",
                    userPattern = emptyList(),
                    typedPhrase = "",
                    shakeProgress = 0,
                    stepProgress = 0,
                    isCompleted = false,
                    isFailed = false,
                    showHint = false,
                    memoryErrorIndex = -1,
                )

                // Initialise type-specific state
                when (mission) {
                    is Mission.MathMission -> initMathMission(mission)
                    is Mission.MemoryMission -> initMemoryMission(mission)
                    is Mission.TypePhraseMission -> initTypeMission(mission)
                    is Mission.ShakeMission -> initShakeMission(mission)
                    is Mission.StepMission -> initStepMission(mission)
                }

                // Start timer if mission is timed
                if (mission.isTimed && mission.timeLimitMs > 0) {
                    startTimer()
                }

                Timber.d("Mission generated: $mission")
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate mission")
                _uiState.value = _uiState.value.copy(isLoading = false, isFailed = true)
            }
        }
    }

    // ── Math ──────────────────────────────────────────────────────────────

    private fun initMathMission(mission: Mission.MathMission) {
        if (mission.problems.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentProblem = mission.problems[0],
                mathProblemIndex = 0,
            )
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val problem = state.currentProblem ?: return

        val userAnswerInt = state.userAnswer.toIntOrNull()
        if (userAnswerInt == null) {
            _uiState.value = state.copy(answerFeedback = AnswerFeedback.Incorrect, attempts = state.attempts + 1)
            return
        }

        if (userAnswerInt == problem.answer) {
            _uiState.value = state.copy(answerFeedback = AnswerFeedback.Correct)
            // Auto-advance after a brief delay
            viewModelScope.launch {
                delay(MATH_ADVANCE_DELAY_MS)
                nextProblem()
            }
        } else {
            _uiState.value = state.copy(answerFeedback = AnswerFeedback.Incorrect, attempts = state.attempts + 1)
        }
    }

    private fun nextProblem() {
        val state = _uiState.value
        val mission = state.mission as? Mission.MathMission ?: return

        val nextIndex = state.mathProblemIndex + 1
        if (nextIndex < mission.problems.size) {
            _uiState.value = state.copy(
                currentProblem = mission.problems[nextIndex],
                mathProblemIndex = nextIndex,
                userAnswer = "",
                answerFeedback = AnswerFeedback.Idle,
            )
        } else {
            // All math problems solved — mission step complete
            completeCurrentStep()
        }
    }

    fun updateUserAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(userAnswer = answer.filter { it.isDigit() || it == '-' })
    }

    // ── Memory ────────────────────────────────────────────────────────────

    private fun initMemoryMission(mission: Mission.MemoryMission) {
        _uiState.value = _uiState.value.copy(
            memoryPattern = mission.pattern,
            isShowingPattern = true,
        )
        startMemoryReveal(mission.pattern)
    }

    private fun startMemoryReveal(pattern: List<Int>) {
        memoryRevealJob?.cancel()
        memoryRevealJob = viewModelScope.launch {
            // Brief pause before showing pattern
            delay(500L)

            for (i in pattern.indices) {
                if (!isActive) return@launch
                _uiState.value = _uiState.value.copy(revealedTileIndex = pattern[i])
                delay(MEMORY_REVEAL_DELAY_MS)
            }

            // Done showing pattern
            _uiState.value = _uiState.value.copy(
                isShowingPattern = false,
                revealedTileIndex = -1,
            )
        }
    }

    fun onTileTap(index: Int) {
        val state = _uiState.value
        if (state.isShowingPattern || state.isCompleted || state.isFailed) return

        val expectedIndex = state.memoryPattern.getOrNull(state.userPattern.size) ?: return

        if (index == expectedIndex) {
            // Correct tap
            val newUserPattern = state.userPattern + index
            _uiState.value = state.copy(
                userPattern = newUserPattern,
                memoryErrorIndex = -1,
            )

            // Check if entire pattern reproduced
            if (newUserPattern.size == state.memoryPattern.size) {
                completeCurrentStep()
            }
        } else {
            // Wrong tap
            _uiState.value = state.copy(
                memoryErrorIndex = index,
                attempts = state.attempts + 1,
            )
            // Clear error after a brief flash
            viewModelScope.launch {
                delay(600L)
                _uiState.value = _uiState.value.copy(memoryErrorIndex = -1)
            }
        }
    }

    // ── Type Phrase ───────────────────────────────────────────────────────

    private fun initTypeMission(mission: Mission.TypePhraseMission) {
        _uiState.value = _uiState.value.copy(targetPhrase = mission.phrase)

        // Schedule hint after 60% of time has elapsed
        if (mission.isTimed && mission.timeLimitMs > 0) {
            hintJob = viewModelScope.launch {
                delay((mission.timeLimitMs * HINT_DELAY_FRACTION).toLong())
                _uiState.value = _uiState.value.copy(showHint = true)
            }
        }
    }

    fun updateTypedText(text: String) {
        val state = _uiState.value
        _uiState.value = state.copy(typedPhrase = text)

        // Auto-check on exact length match
        if (text.length == state.targetPhrase.length && text.isNotEmpty()) {
            checkTypeComplete()
        }
    }

    private fun checkTypeComplete() {
        val state = _uiState.value
        if (state.typedPhrase == state.targetPhrase) {
            completeCurrentStep()
        }
    }

    // ── Shake ─────────────────────────────────────────────────────────────

    private fun initShakeMission(mission: Mission.ShakeMission) {
        _uiState.value = _uiState.value.copy(
            shakeProgress = mission.currentShakes,
            shakeTarget = mission.requiredShakes,
        )
        startShakeListener()
    }

    private fun startShakeListener() {
        shakeListener = shakeEvaluator.startListening {
            onShakeDetected()
        }
    }

    fun onShakeDetected() {
        val state = _uiState.value
        val newProgress = state.shakeProgress + 1
        _uiState.value = state.copy(shakeProgress = newProgress)

        if (newProgress >= state.shakeTarget) {
            completeCurrentStep()
        }
    }

    // ── Step ──────────────────────────────────────────────────────────────

    private fun initStepMission(mission: Mission.StepMission) {
        val sensorAvailable = stepEvaluator.isSensorAvailable()
        _uiState.value = _uiState.value.copy(
            stepProgress = mission.currentSteps,
            stepTarget = mission.requiredSteps,
            stepSensorAvailable = sensorAvailable,
        )
        if (sensorAvailable) {
            startStepListener()
        }
    }

    private fun startStepListener() {
        stepListener = stepEvaluator.startListening {
            onStepDetected()
        }
    }

    fun onStepDetected() {
        val state = _uiState.value
        val newProgress = state.stepProgress + 1
        _uiState.value = state.copy(stepProgress = newProgress)

        if (newProgress >= state.stepTarget) {
            completeCurrentStep()
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state.isCompleted || state.isFailed) break

                val newRemaining = state.timeRemainingMs - TIMER_TICK_MS
                if (newRemaining <= 0) {
                    _uiState.value = state.copy(timeRemainingMs = 0L, isFailed = true)
                    cancelTimer()
                    emitEvent(MissionEvent.MissionFailed)
                    break
                }
                _uiState.value = state.copy(timeRemainingMs = newRemaining)
                delay(TIMER_TICK_MS)
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // ── Step Completion / Multi-Step ──────────────────────────────────────

    private fun completeCurrentStep() {
        cancelTimer()
        hintJob?.cancel()

        val state = _uiState.value
        val totalSteps = state.totalSteps
        val currentStep = state.currentStepIndex

        if (currentStep + 1 < totalSteps) {
            // More steps remain — generate a different mission type for next step
            viewModelScope.launch {
                try {
                    val nextType = pickDifferentType(state.missionType)
                    val nextMission = generateMissionUseCase(
                        type = nextType,
                        difficulty = state.difficulty,
                        isTimed = true,
                    )
                    emitEvent(MissionEvent.StepCompleted(nextMission))
                } catch (e: Exception) {
                    Timber.e(e, "Failed to generate next step mission")
                    emitEvent(MissionEvent.MissionCompleted)
                }
            }
        } else {
            // All steps done
            _uiState.value = state.copy(isCompleted = true)
            recordWakeSuccess()
            emitEvent(MissionEvent.MissionCompleted)
        }
    }

    /**
     * Picks a [MissionType] different from [current] for multi-step variety.
     */
    private fun pickDifferentType(current: MissionType): MissionType {
        val types = MissionType.entries.filter { it != current }
        return types.randomOrNull() ?: MissionType.MATH
    }

    /**
     * Called by the UI layer when transitioning to a new step in a multi-step chain.
     */
    fun loadNextStepMission(nextMission: Mission) {
        val state = _uiState.value
        missionStartTime = System.currentTimeMillis()

        _uiState.value = state.copy(
            mission = nextMission,
            currentStepIndex = state.currentStepIndex + 1,
            isLoading = false,
            missionType = nextMission.type,
            totalTimeMs = nextMission.timeLimitMs,
            timeRemainingMs = nextMission.timeLimitMs,
            attempts = 0,
            answerFeedback = AnswerFeedback.Idle,
            userAnswer = "",
            userPattern = emptyList(),
            typedPhrase = "",
            shakeProgress = 0,
            stepProgress = 0,
            isCompleted = false,
            isFailed = false,
            showHint = false,
            memoryErrorIndex = -1,
            revealedTileIndex = -1,
            isShowingPattern = false,
        )

        when (nextMission) {
            is Mission.MathMission -> initMathMission(nextMission)
            is Mission.MemoryMission -> initMemoryMission(nextMission)
            is Mission.TypePhraseMission -> initTypeMission(nextMission)
            is Mission.ShakeMission -> initShakeMission(nextMission)
            is Mission.StepMission -> initStepMission(nextMission)
        }

        if (nextMission.isTimed && nextMission.timeLimitMs > 0) {
            startTimer()
        }
    }

    // ── Recording ─────────────────────────────────────────────────────────

    private fun recordWakeSuccess() {
        val state = _uiState.value
        val completionTimeMs = System.currentTimeMillis() - missionStartTime

        viewModelScope.launch {
            try {
                recordWakeUseCase(
                    alarmId = alarmId,
                    outcome = com.wakeforge.app.domain.models.WakeOutcome.SUCCESS,
                    snoozeCount = snoozeCount,
                    missionType = state.missionType,
                    difficulty = state.difficulty,
                    completionTimeMs = completionTimeMs,
                )
                Timber.d("Wake recorded successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to record wake")
            }
        }
    }

    fun recordWakeFailure() {
        val state = _uiState.value
        val completionTimeMs = System.currentTimeMillis() - missionStartTime

        viewModelScope.launch {
            try {
                recordWakeUseCase(
                    alarmId = alarmId,
                    outcome = com.wakeforge.app.domain.models.WakeOutcome.FAILURE,
                    snoozeCount = snoozeCount,
                    missionType = state.missionType,
                    difficulty = state.difficulty,
                    completionTimeMs = completionTimeMs,
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to record wake failure")
            }
        }
    }

    // ── Event Emission ────────────────────────────────────────────────────

    private fun emitEvent(event: MissionEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    // ── Public getters for screen use ─────────────────────────────────────

    /** The alarm ID associated with this mission. */
    fun getAlarmId(): String = alarmId

    /** The number of times the user snoozed. */
    fun getSnoozeCount(): Int = snoozeCount

    /** Current [MissionType]. */
    fun getMissionType(): MissionType = missionType

    /** Current [MissionDifficulty]. */
    fun getDifficulty(): MissionDifficulty = difficulty

    // ── Cleanup ───────────────────────────────────────────────────────────

    /**
     * Stop all sensor listeners and timers. Called from the UI layer's
     * DisposableEffect or onCleared.
     */
    fun cleanup() {
        cancelTimer()
        memoryRevealJob?.cancel()
        hintJob?.cancel()
        shakeListener?.let { shakeEvaluator.stopListening(it) }
        stepListener?.let { stepEvaluator.stopListening(it) }
        shakeListener = null
        stepListener = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
        Timber.d("MissionViewModel cleared")
    }
}
