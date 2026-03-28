package com.wakeforge.app.domain.models

/**
 * Represents the difficulty tier for alarm missions.
 * Higher tiers introduce more challenging parameters through the multiplier.
 */
enum class MissionDifficulty(
    val tier: Int,
    val displayName: String,
    val description: String,
    val multiplier: Float
) {
    TRIVIAL(
        tier = 1,
        displayName = "trivial",
        description = "A gentle wake-up. Minimal effort required — perfect for light sleepers.",
        multiplier = 0.5f
    ),
    EASY(
        tier = 2,
        displayName = "easy",
        description = "A modest challenge. Just enough to get your brain started.",
        multiplier = 0.75f
    ),
    MEDIUM(
        tier = 3,
        displayName = "medium",
        description = "A balanced challenge. Requires focus but not overwhelming.",
        multiplier = 1.0f
    ),
    HARD(
        tier = 4,
        displayName = "hard",
        description = "A serious challenge. Demands full attention and effort.",
        multiplier = 1.5f
    ),
    EXTREME(
        tier = 5,
        displayName = "extreme",
        description = "The ultimate wake-up test. Only for the truly committed.",
        multiplier = 2.0f
    );

    /**
     * Returns the next higher difficulty level, capped at [EXTREME].
     */
    fun next(): MissionDifficulty {
        return when (this) {
            TRIVIAL -> EASY
            EASY -> MEDIUM
            MEDIUM -> HARD
            HARD -> EXTREME
            EXTREME -> EXTREME
        }
    }
}
