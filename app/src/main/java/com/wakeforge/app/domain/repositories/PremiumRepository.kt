package com.wakeforge.app.domain.repositories

import com.wakeforge.app.domain.models.PremiumFeature
import com.wakeforge.app.domain.models.PremiumStatus
import com.wakeforge.app.domain.models.PurchaseType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for premium status management and feature gating.
 */
interface PremiumRepository {

    /**
     * Observes the user's premium status reactively.
     *
     * @return A [Flow] emitting the current [PremiumStatus].
     */
    fun getPremiumStatus(): Flow<PremiumStatus>

    /**
     * Checks whether a specific premium feature is currently unlocked.
     *
     * A feature is unlocked if the user has an active premium subscription
     * or has active rewarded access for that specific feature.
     *
     * @param feature The [PremiumFeature] to check.
     * @return true if the feature is accessible, false otherwise.
     */
    suspend fun isFeatureUnlocked(feature: PremiumFeature): Boolean

    /**
     * Grants temporary access to a premium feature via a rewarded ad.
     *
     * @param feature The [PremiumFeature] to unlock temporarily.
     */
    suspend fun grantRewardedAccess(feature: PremiumFeature)

    /**
     * Checks whether rewarded (ad-based) access is still active for a feature.
     *
     * @param feature The [PremiumFeature] to check.
     * @return true if the rewarded access has not expired, false otherwise.
     */
    suspend fun isRewardedAccessActive(feature: PremiumFeature): Boolean

    /**
     * Activates a premium subscription.
     *
     * @param purchaseType The [PurchaseType] that was purchased.
     * @param expiryDate Timestamp when the subscription expires, or null for lifetime.
     */
    suspend fun activatePremium(purchaseType: PurchaseType, expiryDate: Long?)

    /**
     * Deactivates the user's premium subscription.
     *
     * This transitions the status to either [PremiumStatus.Free] or
     * [PremiumStatus.GracePeriod] depending on the subscription type.
     */
    suspend fun deactivatePremium()

    /**
     * Observes the list of currently locked premium features.
     *
     * @return A [Flow] emitting the list of [PremiumFeature]s that are not yet unlocked.
     */
    fun getLockedFeatures(): Flow<List<PremiumFeature>>
}
