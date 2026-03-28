package com.wakeforge.app.domain.models

/**
 * Sealed class representing the user's premium subscription status.
 */
sealed class PremiumStatus {

    /**
     * The user has no active premium subscription.
     */
    data object Free : PremiumStatus() {

        /**
         * Returns true if any premium features are available (always false for Free).
         */
        fun hasPremiumAccess(): Boolean = false
    }

    /**
     * The user has an active premium subscription.
     *
     * @property purchaseType The type of premium purchase.
     * @property expiryDate Timestamp when the subscription expires, or null for lifetime.
     */
    data class Premium(
        val purchaseType: PurchaseType,
        val expiryDate: Long? = null
    ) : PremiumStatus() {

        /**
         * Returns true if the premium subscription is still active.
         * Lifetime subscriptions never expire.
         */
        fun isActive(currentTime: Long = System.currentTimeMillis()): Boolean {
            if (purchaseType == PurchaseType.LIFETIME) return true
            return expiryDate?.let { it > currentTime } ?: false
        }
    }

    /**
     * The user's subscription has expired but is within the grace period.
     *
     * @property expiryDate Timestamp when the grace period ends.
     */
    data class GracePeriod(
        val expiryDate: Long
    ) : PremiumStatus() {

        companion object {
            /** Default grace period duration in milliseconds (7 days). */
            const val GRACE_PERIOD_DURATION_MS: Long = 7L * 24 * 60 * 60 * 1000
        }

        /**
         * Returns true if the grace period is still active.
         */
        fun isActive(currentTime: Long = System.currentTimeMillis()): Boolean {
            return expiryDate > currentTime
        }
    }
}

/**
 * Types of premium purchases available to the user.
 */
enum class PurchaseType {
    /** Monthly recurring subscription. */
    MONTHLY,

    /** Yearly recurring subscription. */
    YEARLY,

    /** One-time permanent purchase. */
    LIFETIME
}
