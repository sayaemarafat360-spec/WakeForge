package com.wakeforge.app.core.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer

// ──────────────────────────────────────────────────────────────────────────────
// Spring Configurations
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Default spring — smooth, natural motion for most UI transitions.
 */
val DefaultSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness    = Spring.StiffnessMedium,
)

/**
 * Default spring specs usable with `animateXxxAsState` APIs.
 */
val DefaultSpringSpec = Spring.Visual(
    dampingRatio = 0.85f,
    stiffness    = 300f,
)

/**
 * Bouncy spring — playful overshoot for celebrations and feedback.
 */
val BouncySpringSpec = Spring.Visual(
    dampingRatio = 0.5f,
    stiffness    = 400f,
)

/**
 * Gentle spring — slow, subtle motion for background changes.
 */
val GentleSpringSpec = Spring.Visual(
    dampingRatio = 0.9f,
    stiffness    = 200f,
)

// ──────────────────────────────────────────────────────────────────────────────
// Duration Constants (ms)
// ──────────────────────────────────────────────────────────────────────────────

/** Navigation screen transition. */
const val NavTransitionMs = 350

/** Card entrance animation. */
const val CardEntranceMs = 400

/** Button press / release feedback. */
const val ButtonPressMs = 150

/** Snackbar appearance duration. */
const val SnackbarMs = 300

/** General fade transition. */
const val FadeMs = 300

/** Celebration / success animation. */
const val CelebrationMs = 800

// ──────────────────────────────────────────────────────────────────────────────
// Easing Curves
// ──────────────────────────────────────────────────────────────────────────────

/** Material-standard easing. */
val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

/** Fast start, gentle end — good for entrances. */
val EaseOutCubic = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

/** Smooth start and end — good for shared transitions. */
val EaseInOutCubic = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

// ──────────────────────────────────────────────────────────────────────────────
// Convenient tween builders
// ──────────────────────────────────────────────────────────────────────────────

/** Standard tween using [StandardEasing] and [FadeMs]. */
fun standardTween() = tween<Float>(durationMillis = FadeMs, easing = StandardEasing)

/** Ease-out tween using [EaseOutCubic]. */
fun easeOutTween(durationMs: Int = FadeMs) = tween<Float>(durationMillis = durationMs, easing = EaseOutCubic)

/** Ease-in-out tween using [EaseInOutCubic]. */
fun easeInOutTween(durationMs: Int = NavTransitionMs) =
    tween<Float>(durationMillis = durationMs, easing = EaseInOutCubic)
