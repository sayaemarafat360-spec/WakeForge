package com.wakeforge.app.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Premium top app bar for WakeForge with optional navigation icon,
 * title, and action icons.
 *
 * @param title         Title text displayed in the center.
 * @param modifier      Outer modifier. If [scrollBehavior] is provided,
 *                      consider wrapping with [nestedScroll].
 * @param navigationIcon Optional icon for the navigation slot (e.g. back arrow).
 *                      When null, the slot is hidden.
 * @param onNavigationClick Callback when the navigation icon is tapped.
 * @param actions       Optional action icons displayed on the trailing side.
 *                      Each entry provides the icon and its click callback.
 * @param scrollBehavior Optional scroll behavior for collapsing / expanding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WFTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopBarAction> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val resolvedModifier = if (scrollBehavior != null) {
        modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        modifier
    }

    androidx.compose.material3.TopAppBar(
        title = {
            Text(
                text = title,
                style = typography.headlineMedium,
                color = colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = resolvedModifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigate back",
                        tint = colors.primaryText,
                    )
                }
            } else {
                // Reserve space for consistent layout
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = action.tint ?: colors.secondaryText,
                    )
                }
            }
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = colors.background,
            scrolledContainerColor = colors.surface,
            titleContentColor = colors.primaryText,
            navigationIconContentColor = colors.primaryText,
            actionIconContentColor = colors.secondaryText,
        ),
        scrollBehavior = scrollBehavior,
    )
}

/**
 * Represents an action icon in [WFTopBar].
 *
 * @param icon               The icon to display.
 * @param contentDescription Accessibility description.
 * @param onClick            Callback when the icon is tapped.
 * @param tint               Override tint; defaults to palette's secondaryText.
 */
data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val tint: Color? = null,
)
