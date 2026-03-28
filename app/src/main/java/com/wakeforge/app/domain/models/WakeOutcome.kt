package com.wakeforge.app.domain.models

/**
 * Represents the possible outcomes of a wake-up attempt.
 */
enum class WakeOutcome(val displayName: String) {
    SUCCESS("success"),
    SNOOZE("snooze"),
    FAILURE("failure");

    companion object {
        /**
         * Returns true if this outcome represents a final resolution (no more snoozes).
         */
        fun WakeOutcome.isFinal(): Boolean = this == SUCCESS || this == FAILURE
    }
}

/**
 * Extension property to check if this outcome is a final resolution.
 */
val WakeOutcome.isFinal: Boolean
    get() = this == WakeOutcome.SUCCESS || this == WakeOutcome.FAILURE
