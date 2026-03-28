package com.wakeforge.app.domain.usecases.premium

import com.wakeforge.app.domain.models.PremiumFeature
import com.wakeforge.app.domain.repositories.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving all premium features with their locked/unlocked status.
 *
 * Useful for displaying the premium features screen, showing which features
 * are available and which require purchase or a rewarded ad.
 *
 * @property repository The [PremiumRepository] used for feature status queries.
 */
class GetPremiumFeaturesUseCase @Inject constructor(
    private val repository: PremiumRepository
) {

    /**
     * Represents a premium feature with its current unlock status.
     *
     * @property feature The [PremiumFeature] definition.
     * @property isUnlocked Whether the feature is currently accessible.
     * @property isRewarded Whether the feature was unlocked via a rewarded ad.
     */
    data class FeatureStatus(
        val feature: PremiumFeature,
        val isUnlocked: Boolean,
        val isRewarded: Boolean = false
    )

    /**
     * Returns all premium features with their current locked/unlocked status.
     *
     * Features are returned in their enum declaration order. Each feature
     * includes whether it is unlocked and whether the unlock came from
     * a rewarded ad versus a subscription.
     *
     * @return A [Flow] emitting the list of [FeatureStatus] entries.
     */
    operator fun invoke(): Flow<List<FeatureStatus>> {
        return repository.getLockedFeatures().map { lockedFeatures ->
            PremiumFeature.entries.map { feature ->
                val isLocked = lockedFeatures.contains(feature)
                FeatureStatus(
                    feature = feature,
                    isUnlocked = !isLocked,
                    isRewarded = false // Will be resolved by the repository implementation
                )
            }
        }
    }

    /**
     * Returns all premium features as a static list without unlock status.
     *
     * Useful for displaying the features catalog before checking status.
     *
     * @return The list of all [PremiumFeature] enum values.
     */
    fun getAllFeatures(): List<PremiumFeature> {
        return PremiumFeature.entries
    }
}
