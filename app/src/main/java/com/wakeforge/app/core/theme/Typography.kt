package com.wakeforge.app.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Complete typography scale for WakeForge using the Inter font family.
 *
 * [TextStyles] provides named style tokens that map directly to UI roles.
 * [WakeForgeTypography] is the Material3 [Typography] instance used by the theme.
 */
object TextStyles {

    // â”€â”€ Display â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** 36 sp bold â€” Home screen clock, hero numbers. */
    val displayLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        letterSpacing = (-0.5).sp,
        lineHeight    = 42.sp,
    )

    // â”€â”€ Headlines â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** 24 sp semi-bold â€” Screen titles, large headers. */
    val headlineLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        letterSpacing = (-0.25).sp,
        lineHeight    = 30.sp,
    )

    /** 20 sp semi-bold â€” Section headers, card group titles. */
    val headlineMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        letterSpacing = (-0.15).sp,
        lineHeight    = 26.sp,
    )

    // â”€â”€ Titles â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** 18 sp medium â€” Alarm time display in cards, prominent labels. */
    val titleLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 18.sp,
        letterSpacing = (-0.1).sp,
        lineHeight    = 24.sp,
    )

    // â”€â”€ Body â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** 16 sp regular â€” Primary body content, descriptions. */
    val bodyLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        letterSpacing = 0.sp,
        lineHeight    = 22.sp,
    )

    /** 14 sp regular â€” Secondary body content, subtitles. */
    val bodyMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        letterSpacing = 0.1.sp,
        lineHeight    = 20.sp,
    )

    // â”€â”€ Labels â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** 14 sp medium â€” Button text, chips, call-to-action labels. */
    val labelLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        letterSpacing = 0.25.sp,
        lineHeight    = 18.sp,
    )

    /** 12 sp medium â€” Captions, metadata, timestamps. */
    val labelMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        letterSpacing = 0.3.sp,
        lineHeight    = 16.sp,
    )
}

/**
 * Material3 [Typography] populated from our custom [TextStyles].
 * This is passed to [androidx.compose.material3.MaterialTheme].
 */
val WakeForgeTypography = Typography(
    displayLarge  = TextStyles.displayLarge,
    headlineLarge = TextStyles.headlineLarge,
    headlineMedium = TextStyles.headlineMedium,
    titleLarge    = TextStyles.titleLarge,
    bodyLarge     = TextStyles.bodyLarge,
    bodyMedium    = TextStyles.bodyMedium,
    labelLarge    = TextStyles.labelLarge,
    labelMedium   = TextStyles.labelMedium,
    // Map remaining slots to closest semantic equivalents
    displayMedium  = TextStyles.headlineLarge,
    displaySmall   = TextStyles.titleLarge,
    headlineSmall  = TextStyles.titleLarge,
    titleMedium    = TextStyles.bodyLarge,
    titleSmall     = TextStyles.bodyMedium,
    bodySmall      = TextStyles.bodyMedium,
    labelSmall     = TextStyles.labelMedium,
)

