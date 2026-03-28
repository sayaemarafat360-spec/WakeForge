package com.wakeforge.app.domain.usecases.premium

import com.wakeforge.app.domain.models.PremiumStatus
import com.wakeforge.app.domain.repositories.PremiumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking the user's current premium subscription status.
 *
 * Provides reactive access to the premium status, allowing the UI to
 * update automatically when the subscription state changes (e.g., purchase,
 * expiry, or cancellation).
 *
 * @property repository The [PremiumRepository] used for status retrieval.
 */
class CheckPremiumStatusUseCase @Inject constructor(
    private val repository: PremiumRepository
) {

    /**
     * Returns a reactive [Flow] of the current [PremiumStatus].
     *
     * The emitted status can be:
     * - [PremiumStatus.Free]: No active subscription.
     * - [PremiumStatus.Premium]: Active premium subscription.
     * - [PremiumStatus.GracePeriod]: Subscription expired but within grace period.
     *
     * @return A [Flow] emitting the current [PremiumStatus].
     */
    operator fun invoke(): Flow<PremiumStatus> {
        return repository.getPremiumStatus()
    }
}
