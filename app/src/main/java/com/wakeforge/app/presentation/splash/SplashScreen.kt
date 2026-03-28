package com.wakeforge.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.BackgroundDark
import kotlinx.coroutines.delay

/**
 * Full-screen splash screen displayed at app launch.
 *
 * Features:
 * - Dark background with the WakeForge logo centered
 * - Animated pulsing glow effect on the logo text
 * - Subtle geometric pattern decoration (concentric circles)
 * - Automatic navigation after the ViewModel determines the destination
 * - Fade-out animation (300ms) before navigating away
 *
 * @param navController Controller for navigation after splash completes.
 * @param viewModel The [SplashViewModel] managing splash state.
 */
@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val splashState by viewModel.state.collectAsStateWithLifecycle()

    // Infinite transition for pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")

    // Glow alpha oscillates between 0.3 and 1.0
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splashGlowAlpha",
    )

    // Overall screen alpha for fade-out before navigation
    val screenAlpha = remember { Animatable(1f) }
    var hasNavigated by remember { androidx.compose.runtime.mutableStateOf(false) }

    // Handle navigation state — trigger fade-out then navigate
    LaunchedEffect(splashState) {
        if (splashState is SplashViewModel.SplashUiState.Navigate && !hasNavigated) {
            val destination = (splashState as SplashViewModel.SplashUiState.Navigate).destination

            // Fade out over 300ms
            screenAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            )

            hasNavigated = true

            // Navigate and remove splash from back stack
            navController.navigate(destination) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center,
    ) {
        // Background layer — geometric pattern decoration
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Draw concentric circles radiating outward with low opacity
            val circleCount = 6
            val maxRadius = size.width * 0.45f

            for (i in 1..circleCount) {
                val radius = maxRadius * (i.toFloat() / circleCount)
                val alpha = 0.04f * (1f - i.toFloat() / circleCount) * glowAlpha

                drawCircle(
                    color = colors.primaryAccent.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.dp.toPx(),
                    ),
                )
            }

            // Radial glow behind the logo text
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primaryAccent.copy(alpha = 0.15f * glowAlpha),
                        colors.primaryAccent.copy(alpha = 0.05f * glowAlpha),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = size.width * 0.3f,
                ),
                center = Offset(cx, cy),
                radius = size.width * 0.3f,
            )
        }

        // Foreground layer — logo text and subtitle
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // "WakeForge" logo using displayLarge typography with primaryAccent
            Text(
                text = "WakeForge",
                style = typography.displayLarge.copy(
                    color = colors.primaryAccent.copy(alpha = glowAlpha),
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtle tagline below the logo
            Text(
                text = "Forge Your Mornings",
                style = typography.bodyMedium.copy(
                    color = colors.secondaryText.copy(alpha = 0.5f * glowAlpha),
                ),
            )
        }
    }
}
