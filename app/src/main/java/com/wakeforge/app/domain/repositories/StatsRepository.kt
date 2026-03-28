package com.wakeforge.app.domain.repositories

import com.wakeforge.app.domain.models.AnalyticsData
import com.wakeforge.app.domain.models.DailyStats
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.Streak
import com.wakeforge.app.domain.models.WakeOutcome
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for wake-up statistics, streaks, and analytics.
 */
interface StatsRepository {

    /**
     * Time periods for which statistics can be queried.
     */
    enum class Period {
        /** Last 7 days. */
        WEEK,

        /** Last 30 days. */
        MONTH,

        /** All recorded data. */
        ALL_TIME
    }

    /**
     * Observes the current streak reactively.
     *
     * @return A [Flow] emitting the current [Streak] whenever it changes.
     */
    fun getStreak(): Flow<Streak>

    /**
     * Records a wake-up event and updates the streak and statistics.
     *
     * @param alarmId The ID of the alarm that triggered the wake event.
     * @param outcome The final [WakeOutcome].
     * @param snoozeCount Number of times the user snoozed.
     * @param missionType The type of mission that was assigned.
     * @param difficulty The difficulty level of the mission.
     * @param completionTimeMs Time taken to resolve the wake event.
     */
    suspend fun recordWake(
        alarmId: String,
        outcome: WakeOutcome,
        snoozeCount: Int,
        missionType: MissionType,
        difficulty: MissionDifficulty,
        completionTimeMs: Long
    )

    /**
     * Returns the current streak count.
     *
     * @return The number of consecutive successful wake-ups.
     */
    suspend fun getStreakCount(): Int

    /**
     * Retrieves daily statistics for the last 7 days.
     *
     * @return A list of [DailyStats] entries, one per day.
     */
    suspend fun getWeeklyStats(): List<DailyStats>

    /**
     * Retrieves comprehensive analytics data aggregated from all wake records.
     *
     * @return An [AnalyticsData] instance with all computed statistics.
     */
    suspend fun getAnalyticsData(): AnalyticsData

    /**
     * Calculates the success rate for a given time period.
     *
     * @param period The [Period] to calculate the rate for.
     * @return A float between 0.0 and 1.0 representing the success rate.
     */
    suspend fun getSuccessRate(period: Period): Float

    /**
     * Returns the total number of recorded wake-up events.
     *
     * @return The total count of all wake events regardless of outcome.
     */
    suspend fun getTotalWakeUps(): Int
}
