package com.wakeforge.app.domain.usecases.stats

import com.wakeforge.app.domain.models.AnalyticsData
import com.wakeforge.app.domain.repositories.StatsRepository
import javax.inject.Inject

/**
 * Use case for retrieving comprehensive wake-up analytics.
 *
 * Aggregates data from all wake records into a single analytics snapshot
 * including streaks, success rates, and usage patterns.
 *
 * @property repository The [StatsRepository] used for analytics retrieval.
 */
class GetAnalyticsUseCase @Inject constructor(
    private val repository: StatsRepository
) {

    /**
     * Returns comprehensive analytics data for the current user.
     *
     * The returned [AnalyticsData] includes:
     * - Current and longest streaks
     * - Weekly and monthly success rates
     * - Total wake-ups, snoozes, and failures
     * - Average snoozes per alarm
     * - Most used mission type and difficulty
     * - Daily breakdown for the last 7 days
     * - Best performing day of the week
     *
     * @return An [AnalyticsData] instance with all computed statistics.
     */
    suspend operator fun invoke(): AnalyticsData {
        return repository.getAnalyticsData()
    }
}
