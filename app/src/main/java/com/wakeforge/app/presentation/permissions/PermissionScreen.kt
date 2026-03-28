package com.wakeforge.app.presentation.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wakeforge.app.R
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFTopBar
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.Success
import com.wakeforge.app.core.theme.Warning

/**
 * Permission setup screen with a list of required permissions.
 *
 * Each permission is displayed as a card with an icon, name, description,
 * and a grant/checkmark status indicator. Cards animate in with a staggered
 * entrance effect.
 *
 * @param navController Controller for back navigation.
 * @param viewModel The [PermissionViewModel] managing permission state.
 */
@Composable
fun PermissionScreen(
    navController: NavController,
    viewModel: PermissionViewModel = hiltViewModel(),
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    // Launcher for runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        // Refresh state after permission result
        viewModel.refreshState()
    }

    // Refresh permissions when screen resumes
    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top bar with back button
        WFTopBar(
            title = "Permission Setup",
            navigationIcon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = { navController.popBackStack() },
        )

        // Subtitle explaining importance
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "WakeForge needs these permissions to ensure alarms work reliably.",
                style = typography.bodyMedium,
                color = colors.secondaryText,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Permission cards list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(
                items = uiState.permissions,
                key = { _, item -> item.permission },
            ) { index, permissionItem ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 400,
                            delayMillis = index * 100,
                            easing = FastOutSlowInEasing,
                        ),
                    ) + fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 300,
                            delayMillis = index * 100,
                        ),
                    ),
                    exit = fadeOut(),
                ) {
                    PermissionItemCard(
                        item = permissionItem,
                        onRequest = {
                            if (permissionItem.isSpecial && activity != null) {
                                viewModel.requestPermission(permissionItem.permission, activity)
                            } else {
                                permissionLauncher.launch(arrayOf(permissionItem.permission))
                            }
                        },
                    )
                }
            }

            // Bottom spacing for the continue button
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Continue button at the bottom
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        WFButton(
            text = "Continue",
            onClick = {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            },
            type = ButtonType.Primary,
            fullWidth = true,
            enabled = uiState.allGranted,
        )
    }
}

/**
 * A single permission card showing name, description, icon, and grant status.
 */
@Composable
private fun PermissionItemCard(
    item: PermissionViewModel.PermissionItem,
    onRequest: () -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // Resolve icon based on permission type
    val icon: ImageVector = when (item.permission) {
        android.Manifest.permission.POST_NOTIFICATIONS -> Icons.Default.Notifications
        "android.permission.SCHEDULE_EXACT_ALARM" -> Icons.Default.Timer
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" -> Icons.Default.BatteryChargingFull
        android.Manifest.permission.ACTIVITY_RECOGNITION -> Icons.Default.Security
        else -> Icons.Default.Security
    }

    WFCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Permission icon
            Icon(
                imageVector = icon,
                contentDescription = item.name,
                tint = if (item.isGranted) Success else colors.secondaryAccent,
                modifier = Modifier.size(28.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Name and description
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.name,
                    style = typography.titleLarge,
                    color = colors.primaryText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status: green checkmark if granted, "Grant" button if not
            if (item.isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Success,
                    modifier = Modifier.size(28.dp),
                )
            } else {
                WFButton(
                    text = "Grant",
                    onClick = onRequest,
                    type = ButtonType.Secondary,
                )
            }
        }
    }
}
