package com.wakeforge.app.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Complete color system for WakeForge.
 * [ThemePalette] holds all semantic color tokens.
 * Use [DarkColorPalette] or [LightColorPalette] directly, or derive from a [PremiumPalette].
 */

// ──────────────────────────────────────────────────────────────────────────────
// Raw color constants
// ──────────────────────────────────────────────────────────────────────────────

val BackgroundDark       = Color(0xFF0B0F14)
val SurfaceDark          = Color(0xFF121821)
val CardDark             = Color(0xFF171F2B)

val PrimaryAccent        = Color(0xFF7C5CFF)
val SecondaryAccent      = Color(0xFF00D4FF)

val Success              = Color(0xFF36D98A)
val Warning              = Color(0xFFFFB84D)
val Error                = Color(0xFFFF5C7A)

val PrimaryText          = Color(0xFFFFFFFF)
val SecondaryText        = Color(0xFFA8B3C7)

val BackgroundLight      = Color(0xFFF5F7FA)
val SurfaceLight         = Color(0xFFFFFFFF)
val CardLight            = Color(0xFFF0F2F5)
val LightText            = Color(0xFF1A1A2E)
val LightSecondary       = Color(0xFF6B7280)

// ──────────────────────────────────────────────────────────────────────────────
// Theme Palette
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Semantic color tokens consumed by the rest of the UI.
 * Every color in the app should reference these tokens — never raw hex values.
 */
data class ThemePalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val cardSurface: Color,
    val toggleActive: Color,
    val toggleInactive: Color,
    val border: Color,
    val scrim: Color,
)

/** Dark color palette — default theme. */
val DarkColorPalette = ThemePalette(
    background      = BackgroundDark,
    surface         = SurfaceDark,
    surfaceVariant  = CardDark.copy(alpha = 0.85f),
    primaryAccent   = PrimaryAccent,
    secondaryAccent = SecondaryAccent,
    success         = Success,
    warning         = Warning,
    error           = Error,
    primaryText     = PrimaryText,
    secondaryText   = SecondaryText,
    cardSurface     = CardDark,
    toggleActive    = PrimaryAccent,
    toggleInactive  = Color(0xFF2A3444),
    border          = Color(0xFF1E2A3A),
    scrim           = Color(0x99000000),
)

/** Light color palette. */
val LightColorPalette = ThemePalette(
    background      = BackgroundLight,
    surface         = SurfaceLight,
    surfaceVariant  = CardLight.copy(alpha = 0.85f),
    primaryAccent   = PrimaryAccent,
    secondaryAccent = SecondaryAccent,
    success         = Success,
    warning         = Warning,
    error           = Error,
    primaryText     = LightText,
    secondaryText   = LightSecondary,
    cardSurface     = CardLight,
    toggleActive    = PrimaryAccent,
    toggleInactive  = Color(0xFFD1D5DB),
    border          = Color(0xFFE5E7EB),
    scrim           = Color(0x73000000),
)

// ──────────────────────────────────────────────────────────────────────────────
// Premium Palettes
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Sealed interface for premium color palettes that override the accent colors.
 * Each palette provides a unique primary and secondary accent color pair.
 */
sealed interface PremiumPalette {

    /** Display name shown in the palette picker UI. */
    val displayName: String

    /** Primary accent color for this palette. */
    val primaryAccent: Color

    /** Secondary accent color for this palette. */
    val secondaryAccent: Color

    /**
     * Apply this premium palette to a base [ThemePalette].
     * Only the accent colors are overridden; everything else stays the same.
     */
    fun applyTo(base: ThemePalette): ThemePalette = base.copy(
        primaryAccent   = primaryAccent,
        secondaryAccent = secondaryAccent,
        toggleActive    = primaryAccent,
    )

    // ── Concrete palettes ──────────────────────────────────────────────────

    data object OceanBlue : PremiumPalette {
        override val displayName = "Ocean Blue"
        override val primaryAccent   = Color(0xFF2196F3)
        override val secondaryAccent = Color(0xFF00BCD4)
    }

    data object SunsetOrange : PremiumPalette {
        override val displayName = "Sunset Orange"
        override val primaryAccent   = Color(0xFFFF7043)
        override val secondaryAccent = Color(0xFFFFAB40)
    }

    data object ForestGreen : PremiumPalette {
        override val displayName = "Forest Green"
        override val primaryAccent   = Color(0xFF4CAF50)
        override val secondaryAccent = Color(0xFF8BC34A)
    }

    data object RoseGold : PremiumPalette {
        override val displayName = "Rose Gold"
        override val primaryAccent   = Color(0xFFE91E63)
        override val secondaryAccent = Color(0xFFFF80AB)
    }

    companion object {
        /** All available premium palettes, in order for UI listing. */
        val all: List<PremiumPalette> = listOf(
            OceanBlue,
            SunsetOrange,
            ForestGreen,
            RoseGold,
        )

        /**
         * Resolve a [PremiumPalette] from its [displayName], or null if not found.
         */
        fun fromDisplayName(name: String): PremiumPalette? = all.find { it.displayName == name }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Convenience extension — build Material3 ColorScheme from a ThemePalette
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Helper to extract an approximate Material3 [androidx.compose.material3.ColorScheme]
 * from our custom [ThemePalette]. This is used internally by [WakeForgeTheme].
 */
// (Implemented in Theme.kt to keep Color.kt free of Material3 imports.)
