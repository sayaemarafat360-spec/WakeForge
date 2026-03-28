package com.wakeforge.app.domain.usecases.stats

import com.wakeforge.app.domain.models.DailyStats
import com.wakeforge.app.domain.repositories.StatsRepository
import javax.inject.Inject

/**
 * Use case for retrieving weekly wake-up statistics.
 *
 * Returns daily statistics for the last 7 days, useful for
 * charting and trend analysis.
 *
 * @property repository The [StatsRepository] used for stats retrieval.
 */
class GetWeeklyStatsUseCase @Inject constructor(
    private val repository: StatsRepository
) {

    /**
     * Returns daily statistics for the last 7 days.
     *
     * Each entry represents one day with its success/failure/snooze counts.
     * Days with no events are included with zero counts to ensure a
     * continuous 7-day view.
     *
     * @return A list of [DailyStats] entries, ordered from oldest to newest.
     */
    suspend operator fun invoke(): List<DailyStats> {
        return repository.getWeeklyStats()
    }
}
