package com.wakeforge.app.domain.usecases.premium

import com.wakeforge.app.domain.models.PremiumFeature
import com.wakeforge.app.domain.repositories.PremiumRepository
import javax.inject.Inject

/**
 * Use case for checking if a specific premium feature is unlocked.
 *
 * A feature is considered unlocked if the user has an active premium
 * subscription or has temporary rewarded access for that feature.
 *
 * @property repository The [PremiumRepository] used for feature gating.
 */
class UnlockFeatureUseCase @Inject constructor(
    private val repository: PremiumRepository
) {

    /**
     * Checks whether a specific premium feature is currently accessible.
     *
     * @param feature The [PremiumFeature] to check.
     * @return true if the feature is unlocked (via subscription or rewarded access).
     */
    suspend operator fun invoke(feature: PremiumFeature): Boolean {
        return repository.isFeatureUnlocked(feature)
    }

    /**
     * Grants temporary rewarded access to a premium feature.
     *
     * This is typically called after the user watches a rewarded ad.
     * The access duration is managed by the repository implementation.
     *
     * @param feature The [PremiumFeature] to temporarily unlock.
     */
    suspend fun grantRewardedAccess(feature: PremiumFeature) {
        repository.grantRewardedAccess(feature)
    }

    /**
     * Checks whether rewarded (ad-based) access is still active for a feature.
     *
     * @param feature The [PremiumFeature] to check.
     * @return true if rewarded access has not yet expired.
     */
    suspend fun isRewardedAccessActive(feature: PremiumFeature): Boolean {
        return repository.isRewardedAccessActive(feature)
    }
}
