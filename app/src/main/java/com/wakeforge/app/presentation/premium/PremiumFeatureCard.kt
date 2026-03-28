package com.wakeforge.app.presentation.premium

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.domain.models.PremiumFeature

/**
 * Feature card used in the premium screen's feature grid.
 *
 * Displays:
 * - Feature icon (based on PremiumFeature.icon string)
 * - Feature display name
 * - Feature description
 * - Lock or check overlay based on unlock status
 *
 * @param feature     The premium feature definition.
 * @param isUnlocked  Whether the feature is currently accessible.
 * @param onClick     Callback when the card is tapped.
 * @param modifier    Outer modifier.
 */
@Composable
fun PremiumFeatureCard(
    feature: PremiumFeature,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val icon = mapFeatureIcon(feature.icon)
    val displayName = feature.displayName.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    val description = buildFeatureDescription(feature)

    WFCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon with colored background
                val iconBgColor by animateColorAsState(
                    targetValue = if (isUnlocked) colors.success.copy(alpha = 0.15f)
                    else colors.surfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "iconBg"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isUnlocked) colors.success else colors.secondaryText,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "iconTint"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = displayName,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feature name
                Text(
                    text = displayName,
                    style = typography.labelLarge,
                    color = colors.primaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Short description
                Text(
                    text = description,
                    style = typography.labelMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.dp
                )
            }

            // Status overlay (top-right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                if (isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Unlocked",
                        tint = colors.success,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = colors.secondaryText.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Maps PremiumFeature icon string names to Material Icons [ImageVector].
 */
private fun mapFeatureIcon(iconName: String): ImageVector {
    return when (iconName) {
        "vibration" -> Icons.Default.Lock // Closest for shake
        "directions_walk" -> androidx.compose.material.icons.filled.DirectionsWalk
        "view_list" -> androidx.compose.material.icons.filled.ViewList
        "lock" -> Icons.Default.Lock
        "timer" -> androidx.compose.material.icons.filled.Timer
        "analytics" -> androidx.compose.material.icons.filled.Analytics
        "palette" -> androidx.compose.material.icons.filled.Palette
        "ad_free" -> androidx.compose.material.icons.filled.AdUnits
        else -> Icons.Default.Lock
    }
}

/**
 * Builds a short, human-readable description for each premium feature.
 */
private fun buildFeatureDescription(feature: PremiumFeature): String {
    return when (feature) {
        PremiumFeature.SHAKE_CHALLENGE -> "Shake your phone to dismiss alarms"
        PremiumFeature.STEP_CHALLENGE -> "Walk steps to complete missions"
        PremiumFeature.MULTI_STEP_MISSIONS -> "Chain multiple challenges together"
        PremiumFeature.STRICT_MODE -> "No snooze, no escape options"
        PremiumFeature.CUSTOM_TIME_LIMITS -> "Set your own time constraints"
        PremiumFeature.ADVANCED_ANALYTICS -> "Deep insights into your patterns"
        PremiumFeature.CUSTOM_THEMES -> "Personalize your app appearance"
        PremiumFeature.NO_ADS -> "Distraction-free experience"
    }
}
