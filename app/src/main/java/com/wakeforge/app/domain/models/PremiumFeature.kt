package com.wakeforge.app.domain.models

/**
 * Enum representing premium features that can be unlocked through purchase or rewarded ads.
 *
 * @property isPremium Whether this feature requires a premium subscription.
 * @property displayName String resource name for the feature's user-facing title.
 * @property description String resource name for the feature's description text.
 * @property icon Material icon name for UI rendering.
 */
enum class PremiumFeature(
    val isPremium: Boolean,
    val displayName: String,
    val description: String,
    val icon: String
) {
    SHAKE_CHALLENGE(
        isPremium = true,
        displayName = "shake_challenge",
        description = "shake_challenge_description",
        icon = "vibration"
    ),
    STEP_CHALLENGE(
        isPremium = true,
        displayName = "step_challenge",
        description = "step_challenge_description",
        icon = "directions_walk"
    ),
    MULTI_STEP_MISSIONS(
        isPremium = true,
        displayName = "multi_step_missions",
        description = "multi_step_missions_description",
        icon = "view_list"
    ),
    STRICT_MODE(
        isPremium = true,
        displayName = "strict_mode",
        description = "strict_mode_description",
        icon = "lock"
    ),
    CUSTOM_TIME_LIMITS(
        isPremium = true,
        displayName = "custom_time_limits",
        description = "custom_time_limits_description",
        icon = "timer"
    ),
    ADVANCED_ANALYTICS(
        isPremium = true,
        displayName = "advanced_analytics",
        description = "advanced_analytics_description",
        icon = "analytics"
    ),
    CUSTOM_THEMES(
        isPremium = true,
        displayName = "custom_themes",
        description = "custom_themes_description",
        icon = "palette"
    ),
    NO_ADS(
        isPremium = true,
        displayName = "no_ads",
        description = "no_ads_description",
        icon = "ad_free"
    )
}
