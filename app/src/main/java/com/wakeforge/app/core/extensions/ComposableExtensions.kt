import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.rememberDismissState
package com.wakeforge.app.core.extensions

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.wakeforge.app.core.theme.ButtonPressMs
import com.wakeforge.app.core.theme.DefaultSpringSpec
import com.wakeforge.app.core.theme.FadeMs

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Pulse Animation
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Applies a continuous pulsing alpha + slight scale animation to the element.
 * Useful for "live" indicators, online dots, or attention-drawing elements.
 */
fun Modifier.pulseAnimation(
    minAlpha: Float = 0.6f,
    maxAlpha: Float = 1f,
    minScale: Float = 0.97f,
    maxScale: Float = 1.03f,
    durationMillis: Int = 1000,
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    this
        .graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Press Effect
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Scales the element down on press (0.96Ã—) and springs back on release.
 * Gives tactile, material-like press feedback without ripple noise.
 */
fun Modifier.pressEffect(): Modifier = composed {
    val interactionScale = androidx.compose.animation.core.animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = DefaultSpringSpec.dampingRatio, stiffness = DefaultSpringSpec.stiffness),
        label = "pressScale",
    )

    this
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    interactionScale.animateTo(0.96f)
                    tryAwaitRelease()
                    interactionScale.animateTo(1f)
                },
            )
        }
        .graphicsLayer {
            scaleX = interactionScale.value
            scaleY = interactionScale.value
        }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Shimmer Effect
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Applies a loading shimmer gradient that sweeps horizontally across the element.
 * The element should have a solid background color; the shimmer overlays on top.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    this.drawBehind {
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            start = Offset(translateX, 0f),
            end = Offset(translateX + size.width / 3f, 0f)
        )
        drawRect(brush = brush)
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Slide Transitions
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/** Slide in from the right edge (e.g. forward navigation). */
fun slideInFromRight(
    durationMillis: Int = FadeMs,
): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
) + fadeIn(
    animationSpec = tween(durationMillis / 2),
)

/** Slide out to the left edge (e.g. forward navigation exit). */
fun slideOutToLeft(
    durationMillis: Int = FadeMs,
): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -(fullWidth * 0.25f).toInt() },
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
) + fadeOut(
    animationSpec = tween(durationMillis / 2),
)

/** Slide in from the bottom (e.g. bottom sheet or list items). */
fun slideInFromBottom(
    durationMillis: Int = FadeMs,
): EnterTransition = slideInVertically(
    initialOffsetY = { fullHeight -> (fullHeight * 0.3f).toInt() },
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
) + fadeIn(
    animationSpec = tween(durationMillis / 2),
)

/** Slide out to the bottom (e.g. bottom sheet dismiss). */
fun slideOutToBottom(
    durationMillis: Int = FadeMs,
): ExitTransition = slideOutVertically(
    targetOffsetY = { fullHeight -> (fullHeight * 0.3f).toInt() },
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
) + fadeOut(
    animationSpec = tween(durationMillis / 2),
)

/**
 * Fade in with a configurable delay (staggered entrance).
 */
fun Modifier.fadeInWithDelay(
    delayMs: Int,
    durationMillis: Int = FadeMs,
): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis,
        delayMillis = delayMs,
        easing = FastOutSlowInEasing,
    ),
)

