package com.wakeforge.app.domain.models

/**
 * Represents the types of missions/challenges a user must complete to dismiss an alarm.
 */
enum class MissionType(
    val displayName: String,
    val description: String,
    val isPremium: Boolean
) {
    MATH(
        displayName = "math_challenge",
        description = "Solve math problems of increasing complexity to dismiss the alarm. "
            + "Problems scale with difficulty from basic arithmetic to multi-step equations.",
        isPremium = false
    ),
    MEMORY(
        displayName = "memory_match",
        description = "Memorize and recall a pattern displayed on a grid. "
            + "Grid size and pattern length increase with difficulty.",
        isPremium = false
    ),
    TYPE_PHRASE(
        displayName = "type_phrase",
        description = "Type a displayed phrase accurately to dismiss the alarm. "
            + "Phrases become longer and more complex at higher difficulties.",
        isPremium = false
    ),
    SHAKE(
        displayName = "shake_it",
        description = "Shake your device vigorously for a required number of times. "
            + "The shake threshold and count increase with difficulty.",
        isPremium = true
    ),
    STEP(
        displayName = "step_out",
        description = "Walk a required number of steps to dismiss the alarm. "
            + "Step count increases with difficulty to ensure you're fully awake.",
        isPremium = true
    )
}
