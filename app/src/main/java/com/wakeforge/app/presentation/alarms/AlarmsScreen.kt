package com.wakeforge.app.presentation.alarms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wakeforge.app.R
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.EmptyStateIllustrations
import com.wakeforge.app.core.components.WFTopBar
import com.wakeforge.app.core.theme.Error
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.PrimaryAccent
import kotlinx.coroutines.launch

/**
 * Alarm list screen with swipe-to-dismiss, undo snackbar, and empty state.
 *
 * Features:
 * - WFTopBar with "Alarms" title and "+" action button
 * - LazyColumn of [AlarmCard] items with swipe-to-dismiss
 * - SwipeToDismiss with red background, trash icon, and "Delete" text
 * - Undo Snackbar (5 second duration) calling [AlarmsViewModel.undoDelete]
 * - Empty state with crescent moon illustration when no alarms exist
 * - FAB for quick alarm creation
 * - Staggered card entrance animations
 *
 * @param navController Controller for navigation.
 * @param viewModel The [AlarmsViewModel] managing alarm state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    navController: NavController,
    viewModel: AlarmsViewModel = hiltViewModel(),
) {
    val colors = LocalWakeForgeColors.current
    val typography = com.wakeforge.app.core.theme.LocalWakeForgeTypography.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show undo snackbar when a delete is staged
    LaunchedEffect(uiState.showDeleteUndo) {
        if (uiState.showDeleteUndo) {
            val result = coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Alarm deleted",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short,
                )
            }
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                // User dismissed; permanent delete will happen via the timer
            }
        }
    }

    // Listen for navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AlarmsViewModel.Event.NavigateToEdit -> {
                    navController.navigate("edit_alarm/${event.alarmId}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_alarm") },
                containerColor = PrimaryAccent,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.create_alarm),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Top bar with "+" action
            WFTopBar(
                title = stringResource(id = R.string.nav_alarms),
                actions = listOf(
                    com.wakeforge.app.core.components.TopBarAction(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_alarm),
                        onClick = { navController.navigate("create_alarm") },
                    ),
                ),
            )

            if (uiState.alarms.isEmpty() && !uiState.showDeleteUndo) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(600),
                        ) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing,
                            ),
                        ),
                    ) {
                        com.wakeforge.app.core.components.WFEmptyState(
                            icon = {
                                EmptyStateIllustrations.NoAlarms(
                                    modifier = Modifier.size(120.dp),
                                )
                            },
                            title = stringResource(id = R.string.empty_no_alarms),
                            subtitle = stringResource(id = R.string.empty_no_alarms_description),
                            actionLabel = stringResource(id = R.string.create_alarm),
                            onAction = { navController.navigate("create_alarm") },
                        )
                    }
                }
            } else {
                // Alarm list with swipe-to-dismiss
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.alarms,
                        key = { alarm -> alarm.id },
                    ) { alarm ->
                        SwipeToDismissAlarmCard(
                            alarm = alarm,
                            onToggle = { alarmId, isActive ->
                                viewModel.toggleAlarm(alarmId, isActive)
                            },
                            onClick = { alarmId ->
                                viewModel.navigateToEdit(alarmId)
                            },
                            onDismiss = { alarmId ->
                                viewModel.deleteAlarm(alarmId)
                            },
                        )
                    }

                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

/**
 * A swipe-to-dismiss wrapper around [AlarmCard].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissAlarmCard(
    alarm: com.wakeforge.app.domain.models.Alarm,
    onToggle: (String, Boolean) -> Unit,
    onClick: (String) -> Unit,
    onDismiss: (String) -> Unit,
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            when (it) {
                androidx.compose.material3.DismissValue.DismissedToStart -> {
                    onDismiss(alarm.id)
                    true
                }
                else -> false
            }
        },
    )

    SwipeToDismiss(
        state = dismissState,
        background = {
            // Red background with trash icon
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    androidx.compose.material3.DismissValue.DismissedToStart -> Error
                    else -> Color.Transparent
                },
                label = "dismissBg",
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = com.wakeforge.app.core.theme.LocalWakeForgeTypography.current.labelMedium,
                        color = Color.White,
                    )
                }
            }
        },
        dismissContent = {
            // Staggered entrance animation for the card
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = (alarm.hour * 20).coerceAtMost(300), // Stagger by time
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 300,
                        delayMillis = (alarm.hour * 20).coerceAtMost(300),
                    ),
                ),
                exit = fadeOut(),
            ) {
                AlarmCard(
                    alarm = alarm,
                    onToggle = onToggle,
                    onClick = onClick,
                )
            }
        },
    )
}
