package com.wakeforge.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.WakeForgeShapes

/**
 * Premium card surface for WakeForge.
 *
 * Features:
 * - Configurable background, border, corner radius, and elevation.
 * - Subtle glass-like alpha overlay for depth.
 * - Uses [Surface] internally for proper Material theming.
 *
 * @param modifier          Outer modifier.
 * @param backgroundColor   Card background color; defaults to palette's [cardSurface].
 * @param borderColor       Optional border color; `null` hides the border.
 * @param borderWidth       Border stroke width when [borderColor] is set.
 * @param cornerRadius      Corner radius for the card shape.
 * @param elevation         Shadow elevation behind the card.
 * @param content           Card content.
 */
@Composable
fun WFCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalWakeForgeColors.current.cardSurface,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 12.dp,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .then(
                if (borderColor != null) {
                    Modifier.border(borderWidth, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .clip(shape),
        color = Color.Transparent,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        // Layered background for glass-like depth
        Box {
            // Base layer — solid background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor, shape),
            )
            // Subtle overlay — slight lighter tint for glass feel
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.03f),
                            ),
                        ),
                        shape = shape,
                    ),
            )
            // Content on top
            Box(
                modifier = Modifier.matchParentSize(),
                content = content,
            )
        }
    }
}
