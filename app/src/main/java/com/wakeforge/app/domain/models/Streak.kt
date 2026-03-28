package com.wakeforge.app.domain.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Tracks the user's consecutive success streak for waking up.
 *
 * @property id Identifier for the streak record (defaults to "main" for single-user apps).
 * @property currentStreak Number of consecutive successful wake-ups.
 * @property longestStreak All-time highest consecutive successes.
 * @property lastSuccessDate Timestamp of the most recent successful wake-up.
 * @property totalSuccesses Cumulative count of all successful wake-ups.
 * @property totalFailures Cumulative count of all failed wake-ups.
 * @property totalSnoozes Cumulative count of all snooze events.
 */
data class Streak(
    val id: String = "main",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastSuccessDate: Long? = null,
    val totalSuccesses: Int = 0,
    val totalFailures: Int = 0,
    val totalSnoozes: Int = 0
) {

    companion object {
        private const val RESET_THRESHOLD_HOURS = 36L
        private const val DATE_FORMAT = "MMM dd, yyyy"
    }

    /**
     * Determines whether the streak should be reset based on the given timestamp.
     *
     * A streak resets if more than [RESET_THRESHOLD_HOURS] hours have elapsed since
     * the last successful wake-up. This allows for time zone changes and oversleeping
     * up to a reasonable threshold.
     *
     * @param timestamp The current timestamp to compare against.
     * @return true if the streak should be reset, false otherwise.
     */
    fun shouldReset(timestamp: Long): Boolean {
        if (lastSuccessDate == null) return false

        val hoursSinceLastSuccess = TimeUnit.MILLISECONDS.toHours(timestamp - lastSuccessDate)
        return hoursSinceLastSuccess > RESET_THRESHOLD_HOURS
    }

    /**
     * Generates a formatted statistics summary string.
     *
     * @return Multi-line string with streak and overall statistics.
     */
    fun toFormattedStats(): String {
        val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val lastSuccessStr = lastSuccessDate?.let {
            dateFormatter.format(Date(it))
        } ?: "Never"

        val successRate = if (totalSuccesses + totalFailures > 0) {
            (totalSuccesses.toFloat() / (totalSuccesses + totalFailures) * 100).toInt()
        } else {
            0
        }

        return buildString {
            appendLine("🔥 Current Streak: $currentStreak day${if (currentStreak != 1) "s" else ""}")
            appendLine("⭐ Longest Streak: $longestStreak day${if (longestStreak != 1) "s" else ""}")
            appendLine("📅 Last Success: $lastSuccessStr")
            appendLine("✅ Total Successes: $totalSuccesses")
            appendLine("❌ Total Failures: $totalFailures")
            appendLine("😴 Total Snoozes: $totalSnoozes")
            appendLine("📊 Success Rate: $successRate%")
        }
    }
}
