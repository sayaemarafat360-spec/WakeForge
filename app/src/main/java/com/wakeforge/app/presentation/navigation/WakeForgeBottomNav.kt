package com.wakeforge.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import com.wakeforge.app.core.theme.LocalWakeForgeColors

/**
 * Bottom navigation bar for WakeForge's 4 primary tabs.
 *
 * @param currentRoute  The route of the currently visible screen (or null).
 * @param onNavigate    Callback invoked with the target route string when a tab is tapped.
 */
@Composable
fun WakeForgeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val colors = LocalWakeForgeColors.current

    NavigationBar(
        containerColor = colors.surfaceVariant,
        contentColor = colors.secondaryText,
        tonalElevation = 0.dp,
        modifier = Modifier,
    ) {
        BottomNavTab(
            selected = currentRoute == Route.Home.route,
            label = "Home",
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            onClick = { onNavigate(Route.Home.route) },
            selectedColor = colors.primaryAccent,
            unselectedColor = colors.secondaryText,
        )
        BottomNavTab(
            selected = currentRoute == Route.Alarms.route,
            label = "Alarms",
            icon = { Icon(Icons.Default.Alarm, contentDescription = "Alarms") },
            onClick = { onNavigate(Route.Alarms.route) },
            selectedColor = colors.primaryAccent,
            unselectedColor = colors.secondaryText,
        )
        BottomNavTab(
            selected = currentRoute == Route.Stats.route,
            label = "Stats",
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            onClick = { onNavigate(Route.Stats.route) },
            selectedColor = colors.primaryAccent,
            unselectedColor = colors.secondaryText,
        )
        BottomNavTab(
            selected = currentRoute == Route.Settings.route,
            label = "Settings",
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            onClick = { onNavigate(Route.Settings.route) },
            selectedColor = colors.primaryAccent,
            unselectedColor = colors.secondaryText,
        )
    }
}

/**
 * Single tab item used inside [WakeForgeBottomBar].
 */
@Composable
private fun BottomNavTab(
    selected: Boolean,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = selectedColor.copy(alpha = 0.12f),
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
        ),
    )
}
