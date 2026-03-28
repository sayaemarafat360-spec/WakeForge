package com.wakeforge.app.core.constants

/**
 * Constants for mission types, sensor thresholds, and difficulty configurations.
 */
object MissionConstants {

    // ── Mission Type Keys ─────────────────────────────────────────────────

    const val MATH       = "math"
    const val MEMORY     = "memory"
    const val TYPE_PHRASE = "type_phrase"
    const val SHAKE      = "shake"
    const val STEP       = "step"

    /** All valid mission type keys. */
    val ALL_MISSION_TYPES = listOf(MATH, MEMORY, TYPE_PHRASE, SHAKE, STEP)

    // ── Shake Mission ─────────────────────────────────────────────────────

    /** Minimum acceleration magnitude (m/s²) to register as a valid shake. */
    const val SHAKE_THRESHOLD_ACCELERATION = 12f

    /** Minimum time between two consecutive shake registrations (ms). */
    const val SHAKE_COOLDOWN_MS = 500L

    // ── Step Mission ──────────────────────────────────────────────────────

    /** Interval at which the step counter sensor is polled (ms). */
    const val STEP_CHECK_INTERVAL_MS = 1_000L

    // ── Difficulty Configuration ──────────────────────────────────────────
    //
    // Each difficulty tier defines min/max bounds for mission parameters.
    // Tiers: EASY, MEDIUM, HARD, EXTREME.
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Math mission difficulty:
     * - problemCount: number of arithmetic problems to solve
     * - maxOperand:   largest number in the problem
     */
    object MathDifficulty {
        data class Tier(val problemCount: IntRange, val maxOperand: IntRange)

        val EASY    = Tier(problemCount = 1..2,  maxOperand = 10..20)
        val MEDIUM  = Tier(problemCount = 2..3,  maxOperand = 20..50)
        val HARD    = Tier(problemCount = 3..5,  maxOperand = 50..100)
        val EXTREME = Tier(problemCount = 5..7,  maxOperand = 100..200)
    }

    /**
     * Memory mission difficulty:
     * - gridSize:       NxN grid (represented as total cell count)
     * - sequenceLength: number of tiles to memorize
     */
    object MemoryDifficulty {
        data class Tier(val gridSize: IntRange, val sequenceLength: IntRange)

        val EASY    = Tier(gridSize = 4..6,   sequenceLength = 3..4)
        val MEDIUM  = Tier(gridSize = 6..9,   sequenceLength = 4..6)
        val HARD    = Tier(gridSize = 9..12,  sequenceLength = 6..8)
        val EXTREME = Tier(gridSize = 12..16, sequenceLength = 8..12)
    }

    /**
     * Type-phrase mission difficulty:
     * - phraseLength: number of words in the phrase
     * - minWordLength: shortest allowed word
     */
    object TypePhraseDifficulty {
        data class Tier(val phraseLength: IntRange, val minWordLength: IntRange)

        val EASY    = Tier(phraseLength = 3..5,   minWordLength = 3..4)
        val MEDIUM  = Tier(phraseLength = 5..8,   minWordLength = 4..6)
        val HARD    = Tier(phraseLength = 8..12,  minWordLength = 5..8)
        val EXTREME = Tier(phraseLength = 12..18, minWordLength = 6..10)
    }

    /**
     * Shake mission difficulty:
     * - shakeCount: number of shakes required
     */
    object ShakeDifficulty {
        data class Tier(val shakeCount: IntRange)

        val EASY    = Tier(shakeCount = 5..10)
        val MEDIUM  = Tier(shakeCount = 10..20)
        val HARD    = Tier(shakeCount = 20..35)
        val EXTREME = Tier(shakeCount = 35..50)
    }

    /**
     * Step mission difficulty:
     * - stepCount: number of steps required
     */
    object StepDifficulty {
        data class Tier(val stepCount: IntRange)

        val EASY    = Tier(stepCount = 10..20)
        val MEDIUM  = Tier(stepCount = 20..40)
        val HARD    = Tier(stepCount = 40..70)
        val EXTREME = Tier(stepCount = 70..100)
    }
}
