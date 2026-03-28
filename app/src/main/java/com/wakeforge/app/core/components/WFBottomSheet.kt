package com.wakeforge.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.WakeForgeShapes

/**
 * Premium bottom sheet wrapper with WakeForge branding.
 *
 * @param onDismissRequest  Called when the user dismisses the sheet.
 * @param sheetState        Sheet state controlling visibility and drag.
 * @param title             Optional title displayed at the top of the sheet.
 * @param modifier          Outer modifier applied to the sheet content.
 * @param containerColor    Background color; defaults to palette surface.
 * @param topCorner         Top corner radius.
 * @param content           Sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WFBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalWakeForgeColors.current.surface,
    topCorner: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = topCorner, topEnd = topCorner),
        containerColor = containerColor,
        dragHandle = {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.12f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border),
                )
            }
        },
        content = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                // Title
                if (title != null) {
                    Text(
                        text = title,
                        style = typography.headlineMedium,
                        color = colors.primaryText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    )
                }
                content()
            }
        },
    )
}
