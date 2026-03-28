package com.wakeforge.app.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape system for WakeForge.
 *
 * Four size tiers provide consistent rounding across the entire UI:
 * - **extraSmall** â€” Chips, tags, small badges (4 dp)
 * - **medium**     â€” Cards, dialogs, input fields (12 dp)
 * - **large**      â€” Bottom sheets, modals, full-width panels (20 dp)
 * - **extraLarge** â€” Alarm ringing overlay, success screen (28 dp)
 */
object WakeForgeShapes {

    /** 4 dp â€” Chips, tags, small badges. */
    val extraSmall = RoundedCornerShape(4.dp)

    /** 12 dp â€” Cards, dialogs, input fields. */
    val medium = RoundedCornerShape(12.dp)

    /** 20 dp â€” Bottom sheets, modals, full-width panels. */
    val large = RoundedCornerShape(20.dp)

    /** 28 dp â€” Alarm ringing overlay, success screen, hero containers. */
    val extraLarge = RoundedCornerShape(28.dp)
}

/**
 * Material3 [Shapes] instance consumed by [MaterialTheme].
 * Defaults to [medium] corners for most elements.
 */
val WakeForgeMaterialShapes = Shapes(
    extraSmall = WakeForgeShapes.extraSmall,
    small      = WakeForgeShapes.extraSmall,
    medium     = WakeForgeShapes.medium,
    large      = WakeForgeShapes.large,
    extraLarge = WakeForgeShapes.extraLarge,
)

