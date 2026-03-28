package com.wakeforge.app.domain.models

/**
 * Aggregated analytics data for the user's wake-up history.
 *
 * @property currentStreak Current consecutive successful wake-ups.
 * @property longestStreak All-time highest consecutive successes.
 * @property weeklySuccessRate Percentage of successful wake-ups in the last 7 days (0.0–1.0).
 * @property totalWakeUps Total number of alarm events (all outcomes).
 * @property totalSnoozes Total number of snooze events across all alarms.
 * @property totalFailures Total number of failed wake-up attempts.
 * @property averageSnoozePerAlarm Average snoozes per alarm event.
 * @property mostUsedMissionType The most frequently assigned mission type.
 * @property mostUsedDifficulty The most frequently used difficulty tier.
 * @property weeklyData Daily statistics for the last 7 days.
 * @property monthlySuccessRate Percentage of successful wake-ups in the last 30 days (0.0–1.0).
 * @property bestDayOfWeek The day of the week with the highest success rate.
 */
data class AnalyticsData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklySuccessRate: Float = 0f,
    val totalWakeUps: Int = 0,
    val totalSnoozes: Int = 0,
    val totalFailures: Int = 0,
    val averageSnoozePerAlarm: Float = 0f,
    val mostUsedMissionType: MissionType? = null,
    val mostUsedDifficulty: MissionDifficulty? = null,
    val weeklyData: List<DailyStats> = emptyList(),
    val monthlySuccessRate: Float = 0f,
    val bestDayOfWeek: DayOfWeek? = null
) {

    /**
     * Returns the total number of successful wake-ups.
     */
    val totalSuccesses: Int
        get() = maxOf(0, totalWakeUps - totalFailures - totalSnoozes)
}

/**
 * Daily wake-up statistics for a single day.
 *
 * @property date Timestamp representing the start of the day.
 * @property dayOfWeek Day of the week as a [Calendar] constant (1=Sunday through 7=Saturday).
 * @property successes Number of successful mission completions.
 * @property failures Number of failed mission attempts.
 * @property snoozes Number of snooze events.
 */
data class DailyStats(
    val date: Long,
    val dayOfWeek: Int,
    val successes: Int,
    val failures: Int,
    val snoozes: Int
) {

    /**
     * Total wake-up events for this day.
     */
    val total: Int
        get() = successes + failures

    /**
     * Success rate for this day (0.0–1.0). Returns 0 if no events occurred.
     */
    val successRate: Float
        get() = if (total > 0) successes.toFloat() / total else 0f
}
