package com.wakeforge.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.WakeForgeShapes

/**
 * Premium dialog for WakeForge with rounded corners, custom theming,
 * and optional icon.
 *
 * @param onDismissRequest Called when the dialog is dismissed (e.g. tap outside).
 * @param title            Dialog title.
 * @param message          Dialog body message.
 * @param confirmText      Label for the confirm / primary action button.
 * @param dismissText      Label for the dismiss / secondary action button; `null` hides it.
 * @param onConfirm        Callback for the confirm action.
 * @param onDismiss        Callback for the dismiss action; `null` hides the button.
 * @param icon             Optional icon displayed above the title.
 */
@Composable
fun WFDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String? = "Cancel",
    onConfirm: () -> Unit = onDismissRequest,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector? = null,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = colors.surface,
        shape = WakeForgeShapes.medium,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 12.dp),
                    )
                }
                Text(
                    text = title,
                    style = typography.headlineMedium,
                    color = colors.primaryText,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Text(
                text = message,
                style = typography.bodyLarge,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            WFButton(
                text = confirmText,
                onClick = {
                    onConfirm()
                    onDismissRequest()
                },
                type = ButtonType.Primary,
            )
        },
        dismissButton = if (dismissText != null && onDismiss != null) {
            {
                WFButton(
                    text = dismissText,
                    onClick = {
                        onDismiss()
                        onDismissRequest()
                    },
                    type = ButtonType.Ghost,
                )
            }
        } else {
            null
        },
    )
}
