package com.wakeforge.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.extensions.pressEffect
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.WakeForgeShapes

/**
 * Button type variants for [WFButton].
 */
enum class ButtonType {
    /** Solid fill using primaryAccent — main CTA. */
    Primary,
    /** Outlined style with transparent background and accent border. */
    Secondary,
    /** Text-only with no background or border. */
    Ghost,
    /** Solid fill using error color — destructive actions. */
    Danger,
}

/**
 * Premium button for WakeForge with four visual variants, press feedback,
 * loading state, and consistent typography.
 *
 * @param text      Button label.
 * @param onClick   Callback invoked when the button is clicked.
 * @param modifier  Outer modifier.
 * @param type      Visual variant: [ButtonType.Primary], [ButtonType.Secondary],
 *                  [ButtonType.Ghost], or [ButtonType.Danger].
 * @param enabled   Whether the button is interactive.
 * @param loading   When `true`, shows a spinner and disables clicks.
 * @param fullWidth When `true`, the button expands to fill its parent's width.
 */
@Composable
fun WFButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val interactionEnabled = enabled && !loading

    // Resolve colors based on type and state
    val bgColor: Color
    val contentColor: Color
    val borderColor: Color?

    when (type) {
        ButtonType.Primary -> {
            bgColor = if (interactionEnabled) colors.primaryAccent else colors.primaryAccent.copy(alpha = 0.4f)
            contentColor = Color.White
            borderColor = null
        }
        ButtonType.Secondary -> {
            bgColor = Color.Transparent
            contentColor = if (interactionEnabled) colors.primaryAccent else colors.primaryAccent.copy(alpha = 0.4f)
            borderColor = if (interactionEnabled) colors.primaryAccent else colors.primaryAccent.copy(alpha = 0.3f)
        }
        ButtonType.Ghost -> {
            bgColor = Color.Transparent
            contentColor = if (interactionEnabled) colors.primaryText else colors.secondaryText.copy(alpha = 0.4f)
            borderColor = null
        }
        ButtonType.Danger -> {
            bgColor = if (interactionEnabled) colors.error else colors.error.copy(alpha = 0.4f)
            contentColor = Color.White
            borderColor = null
        }
    }

    val shape = WakeForgeShapes.medium

    val effectiveModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .pressEffect()

    Box(
        modifier = effectiveModifier
            .clip(shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .background(bgColor, shape)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = typography.labelLarge.copy(
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}
