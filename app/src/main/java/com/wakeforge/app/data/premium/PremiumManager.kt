package com.wakeforge.app.data.premium

import com.wakeforge.app.domain.models.PremiumFeature
import com.wakeforge.app.domain.models.PremiumStatus
import com.wakeforge.app.domain.models.PurchaseType
import com.wakeforge.app.domain.repositories.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    private val premiumRepository: PremiumRepository
) {

    fun isPremium(): Flow<Boolean> {
        return premiumRepository.getPremiumStatus().map { status ->
            when (status) {
                is PremiumStatus.Free -> false
                is PremiumStatus.Premium -> status.isActive()
                is PremiumStatus.GracePeriod -> status.isActive()
            }
        }
    }

    fun getStatus(): Flow<PremiumStatus> {
        return premiumRepository.getPremiumStatus()
    }

    suspend fun activate(purchaseType: PurchaseType, expiryDate: Long?) {
        premiumRepository.activatePremium(purchaseType, expiryDate)
    }

    suspend fun deactivate() {
        premiumRepository.deactivatePremium()
    }

    fun isFeatureAvailable(feature: PremiumFeature): Flow<Boolean> = flow {
        emit(premiumRepository.isFeatureUnlocked(feature))
    }

    suspend fun grantRewardedAccess(feature: PremiumFeature) {
        premiumRepository.grantRewardedAccess(feature)
    }

    suspend fun isPremiumOnce(): Boolean {
        val status = premiumRepository.getPremiumStatus().first()
        return when (status) {
            is PremiumStatus.Free -> false
            is PremiumStatus.Premium -> status.isActive()
            is PremiumStatus.GracePeriod -> status.isActive()
        }
    }

    suspend fun isFeatureUnlocked(feature: PremiumFeature): Boolean {
        return premiumRepository.isFeatureUnlocked(feature)
    }

    /**
     * Restores previously purchased premium status.
     *
     * In a real app, this would query Google Play Billing for past purchases.
     * Currently, it triggers a premium status check by reading the current status
     * from the repository, which re-evaluates any previously activated purchases.
     */
    suspend fun restorePurchases() {
        // Read the current premium status to trigger a refresh.
        // In production, this would call BillingClient.queryPurchasesAsync()
        // and re-activate premium if a valid purchase is found.
        premiumRepository.getPremiumStatus().first()
    }
}
