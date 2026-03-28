package com.wakeforge.app.presentation.premium

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFTopBar
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.TextStyles
import com.wakeforge.app.domain.models.PremiumFeature

/**
 * Premium upgrade screen showcasing all premium features, pricing plans,
 * and purchase/restore options.
 *
 * Sections:
 * 1. Hero banner with gradient background
 * 2. Features grid (2 columns)
 * 3. Pricing options (3 plans)
 * 4. Upgrade CTA button
 * 5. Rewarded ad trial button
 * 6. Restore purchase link
 *
 * If the user already has premium, shows a success state instead.
 */
@Composable
fun PremiumScreen(
    navController: NavController? = null,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Column(modifier = Modifier.fillMaxSize()) {
        WFTopBar(title = "Go Premium")

        if (uiState.isPremium) {
            // ── Already Premium State ────────────────────────────────
            AlreadyPremiumState(
                onRestore = { viewModel.restorePurchase() },
                isRestoring = uiState.isRestoring
            )
        } else {
            // ── Upgrade Flow ─────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Hero Section
                item {
                    HeroSection(colors = colors, typography = typography)
                }

                // Features Grid
                item {
                    Text(
                        text = "Features",
                        style = typography.titleLarge,
                        color = colors.primaryText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Use a nested LazyVerticalGrid requires specific handling,
                // so we render the grid items as a grid manually.
                val gridItems = uiState.features
                val half = (gridItems.size + 1) / 2
                items(gridItems.chunked(2)) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { featureStatus ->
                            PremiumFeatureCard(
                                feature = featureStatus.feature,
                                isUnlocked = featureStatus.isUnlocked,
                                onClick = { },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining space if odd number
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Pricing Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose Your Plan",
                        style = typography.titleLarge,
                        color = colors.primaryText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(PremiumViewModel.PricingPlan.entries) { plan ->
                    PricingPlanCard(
                        plan = plan,
                        isSelected = uiState.selectedPlan == plan,
                        onSelect = { viewModel.selectPlan(plan) },
                        colors = colors,
                        typography = typography
                    )
                }

                // Purchase Error
                if (uiState.purchaseError != null) {
                    item {
                        WFCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = uiState.purchaseError!!,
                                style = typography.bodyMedium,
                                color = colors.error,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Upgrade Button
                item {
                    WFButton(
                        text = "Upgrade Now",
                        onClick = { viewModel.purchaseSelectedPlan() },
                        type = com.wakeforge.app.core.components.ButtonType.Primary,
                        fullWidth = true,
                        modifier = Modifier.height(52.dp)
                    )
                }

                // Rewarded Ad Section
                item {
                    WFButton(
                        text = "Watch an Ad to Try Free",
                        onClick = { /* Trigger rewarded ad */ },
                        type = com.wakeforge.app.core.components.ButtonType.Secondary,
                        fullWidth = true,
                        modifier = Modifier.height(48.dp)
                    )
                }

                // Restore Purchase
                item {
                    WFButton(
                        text = "Restore Purchase",
                        onClick = { viewModel.restorePurchase() },
                        type = com.wakeforge.app.core.components.ButtonType.Ghost,
                        fullWidth = true,
                        loading = uiState.isRestoring
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Hero section with "WakeForge Pro" title and decorative gradient.
 */
@Composable
private fun HeroSection(
    colors: com.wakeforge.app.core.theme.ThemePalette,
    typography: TextStyles
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Decorative gradient background using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.primaryAccent.copy(alpha = 0.25f),
                        colors.secondaryAccent.copy(alpha = 0.15f),
                        colors.primaryAccent.copy(alpha = 0.08f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )

            // Decorative circles
            drawCircle(
                color = colors.primaryAccent.copy(alpha = 0.08f),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
            drawCircle(
                color = colors.secondaryAccent.copy(alpha = 0.06f),
                radius = size.width * 0.25f,
                center = Offset(size.width * 0.15f, size.height * 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Star icon
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = colors.primaryAccent,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "WakeForge Pro",
                style = typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Unlock the full potential of your mornings",
                style = typography.bodyMedium,
                color = colors.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Premium "already subscribed" success state.
 */
@Composable
private fun AlreadyPremiumState(
    onRestore: () -> Unit,
    isRestoring: Boolean
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WFCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.success.copy(alpha = 0.08f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You're Premium!",
                    style = typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.success,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All premium features are unlocked. Enjoy your enhanced wake-up experience!",
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        WFButton(
            text = "Restore Purchase",
            onClick = onRestore,
            type = com.wakeforge.app.core.components.ButtonType.Ghost,
            loading = isRestoring
        )
    }
}

/**
 * Pricing plan card with radio-style selection and badge support.
 */
@Composable
private fun PricingPlanCard(
    plan: PremiumViewModel.PricingPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colors: com.wakeforge.app.core.theme.ThemePalette,
    typography: TextStyles
) {
    WFCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isSelected) colors.primaryAccent else null,
        borderWidth = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colors.primaryAccent
                        else colors.border
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colors.primaryAccent
                        else colors.cardSurface
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.displayName,
                        style = typography.titleLarge,
                        color = colors.primaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge
                    if (plan == PremiumViewModel.PricingPlan.MONTHLY) {
                        Box(
                            modifier = Modifier
                                .background(
                                    colors.primaryAccent.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Popular",
                                style = typography.labelMedium,
                                color = colors.primaryAccent,
                                fontSize = 10.sp
                            )
                        }
                    }
                    if (plan == PremiumViewModel.PricingPlan.LIFETIME) {
                        Box(
                            modifier = Modifier
                                .background(
                                    colors.success.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Best Value",
                                style = typography.labelMedium,
                                color = colors.success,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text(
                    text = plan.subtitle,
                    style = typography.bodyMedium,
                    color = colors.secondaryText
                )
            }

            Text(
                text = plan.price,
                style = typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) colors.primaryAccent else colors.primaryText
            )
        }
    }
}
