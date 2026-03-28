package com.wakeforge.app.domain.usecases.stats

import com.wakeforge.app.domain.models.Streak
import com.wakeforge.app.domain.repositories.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing the current wake-up streak.
 *
 * The streak is updated reactively whenever a new wake event is recorded.
 *
 * @property repository The [StatsRepository] used for streak retrieval.
 */
class GetStreakUseCase @Inject constructor(
    private val repository: StatsRepository
) {

    /**
     * Returns a reactive [Flow] of the current [Streak].
     *
     * @return A [Flow] emitting the current [Streak] whenever it changes.
     */
    operator fun invoke(): Flow<Streak> {
        return repository.getStreak()
    }
}
