package com.wakeforge.app.core.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ──────────────────────────────────────────────────────────────────────────────
// Composition Locals
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Provides the current [ThemePalette] to the composition tree.
 * Prefer reading this over raw Material3 colors when you need WakeForge-specific
 * semantic tokens (e.g. `toggleActive`, `scrim`).
 */
val LocalWakeForgeColors = compositionLocalOf { DarkColorPalette }

/**
 * Provides the current [TextStyles] wrapper to the composition tree.
 * This is a convenience accessor so composables can use named style tokens
 * without importing [TextStyles] directly.
 */
val LocalWakeForgeTypography = staticCompositionLocalOf { TextStyles }

// ──────────────────────────────────────────────────────────────────────────────
// Accessor object
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Static accessor for WakeForge theme values outside of composition.
 * Inside composables, prefer the composition locals above.
 */
object WakeForgeTheme {

    /** Current palette — only valid inside a [WakeForgeTheme] composable. */
    val colors: ThemePalette
        @Composable get() = LocalWakeForgeColors.current

    /** Current typography styles — only valid inside a [WakeForgeTheme] composable. */
    val typography: TextStyles
        @Composable get() = LocalWakeForgeTypography.current

    /** Material3 [MaterialTheme] colors delegate. */
    val materialColors: androidx.compose.material3.ColorScheme
        @Composable get() = MaterialTheme.colorScheme

    /** Material3 [MaterialTheme] typography delegate. */
    val materialTypography: androidx.compose.material3.Typography
        @Composable get() = MaterialTheme.typography

    /** Material3 [MaterialTheme] shapes delegate. */
    val materialShapes: androidx.compose.material3.Shapes
        @Composable get() = MaterialTheme.shapes
}

// ──────────────────────────────────────────────────────────────────────────────
// Theme Composable
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Root theme composable for the WakeForge application.
 *
 * @param darkTheme      When `true` the dark palette is used; otherwise light.
 *                       Defaults to `true`.
 * @param premiumPalette An optional [PremiumPalette] that overrides accent colors.
 *                       Pass `null` for the default WakeForge accent scheme.
 * @param dynamicColor   When `true`, Material You dynamic color is attempted
 *                       (Android 12+). Defaults to `false` because WakeForge has
 *                       its own curated palette.
 * @param content        The composable content tree.
 */
@Composable
fun WakeForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    premiumPalette: PremiumPalette? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // 1. Pick base palette by theme mode
    val basePalette = if (darkTheme) DarkColorPalette else LightColorPalette

    // 2. Apply premium palette overlay when provided
    val resolvedPalette = premiumPalette?.applyTo(basePalette) ?: basePalette

    // 3. Build a Material3 ColorScheme from the resolved palette
    //    We still use dynamic color when explicitly requested and available.
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> darkColorScheme(
            primary       = resolvedPalette.primaryAccent,
            secondary     = resolvedPalette.secondaryAccent,
            tertiary      = resolvedPalette.success,
            background    = resolvedPalette.background,
            surface       = resolvedPalette.surface,
            surfaceVariant = resolvedPalette.surfaceVariant,
            error         = resolvedPalette.error,
            onPrimary     = Color.White,
            onSecondary   = Color.White,
            onBackground  = resolvedPalette.primaryText,
            onSurface     = resolvedPalette.primaryText,
            onSurfaceVariant = resolvedPalette.secondaryText,
            onError       = Color.White,
            outline       = resolvedPalette.border,
            scrim         = resolvedPalette.scrim,
        )
        else -> lightColorScheme(
            primary       = resolvedPalette.primaryAccent,
            secondary     = resolvedPalette.secondaryAccent,
            tertiary      = resolvedPalette.success,
            background    = resolvedPalette.background,
            surface       = resolvedPalette.surface,
            surfaceVariant = resolvedPalette.surfaceVariant,
            error         = resolvedPalette.error,
            onPrimary     = Color.White,
            onSecondary   = Color.White,
            onBackground  = resolvedPalette.primaryText,
            onSurface     = resolvedPalette.primaryText,
            onSurfaceVariant = resolvedPalette.secondaryText,
            onError       = Color.White,
            outline       = resolvedPalette.border,
            scrim         = resolvedPalette.scrim,
        )
    }

    // 4. Wrap content with composition locals + MaterialTheme
    CompositionLocalProvider(
        LocalWakeForgeColors provides resolvedPalette,
        LocalWakeForgeTypography provides TextStyles,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = WakeForgeTypography,
            shapes      = WakeForgeMaterialShapes,
            content     = content,
        )
    }
}
