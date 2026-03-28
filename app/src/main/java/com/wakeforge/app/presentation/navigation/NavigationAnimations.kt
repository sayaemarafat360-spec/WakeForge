package com.wakeforge.app.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Centralised navigation transition definitions for WakeForge.
 *
 * Every transition uses [spring] physics from [Spring.DampingRatioNoBouncy]
 * for natural-feeling motion, keeping duration consistent with [NavTransitionMs].
 */
object NavigationAnimations {

    // ──────────────────────────────────────────────────────────────────────────
    // Horizontal transitions (default forward / backward)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Slide in from the right edge + subtle fade.
     * Used as the default **enter** transition when navigating forward.
     */
    fun enterFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> (fullWidth * 0.30f).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    /**
     * Slide out to the left edge + subtle fade.
     * Used as the default **exit** transition when navigating forward.
     */
    fun exitToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth * 0.30f).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    /**
     * Slide in from the left edge + subtle fade.
     * Used as the **enter** transition when popping the back stack (navigating back).
     */
    fun enterFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth * 0.30f).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    /**
     * Slide out to the right edge + subtle fade.
     * Used as the **exit** transition when popping the back stack (navigating back).
     */
    fun exitToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * 0.30f).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Vertical transitions (modal / fullscreen overlays)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Slide up from the bottom + fade. Used for modal-style screens such as
     * [Route.AlarmRinging] and [Route.MissionChallenge].
     */
    fun enterFromBottom(): EnterTransition = slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    /**
     * Slide down to the bottom + fade. Used as the exit pair for [enterFromBottom].
     */
    fun exitToBottom(): ExitTransition = slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Scale transitions (celebratory overlays)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Scale-in + fade. Used for success / celebration screens such as
     * [Route.WakeSuccess].
     */
    fun scaleIn(): EnterTransition = scaleIn(
        initialScale = 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    /**
     * Scale-out + fade. Used as the exit pair for [scaleIn].
     */
    fun scaleOut(): ExitTransition = scaleOut(
        targetScale = 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Fade-only transitions
    // ──────────────────────────────────────────────────────────────────────────

    /** Simple fade-in used for the splash screen dissolve. */
    fun fadeIn(): EnterTransition = fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    /** Simple fade-out used for the splash screen dissolve. */
    fun fadeOut(): ExitTransition = fadeOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
}
