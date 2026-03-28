package com.wakeforge.app.presentation.create_alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.components.WFToggle
import com.wakeforge.app.core.components.TopBarAction
import com.wakeforge.app.core.components.WFTopBar
import com.wakeforge.app.core.extensions.observeEventsWithLifecycle
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType

/**
 * Full-screen alarm creation form.
 *
 * Presents every configurable alarm property in a scrollable form, organized
 * into visually distinct card sections. The user can set time, label, repeat
 * days, sound, vibration, mission type/difficulty, snooze behavior, and
 * strict-mode options.
 *
 * @param navController Navigation controller for back-navigation on save.
 * @param viewModel     ViewModel injected by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAlarmScreen(
    navController: NavController,
    viewModel: CreateAlarmViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    // Handle events
    viewModel.events.observeEventsWithLifecycle { event ->
        when (event) {
            is CreateAlarmViewModel.CreateAlarmEvent.SaveSuccess -> {
                navController.popBackStack()
            }
            is CreateAlarmViewModel.CreateAlarmEvent.SaveError -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            WFTopBar(
                title = "Create Alarm",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { navController.popBackStack() },
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Check,
                        contentDescription = "Save alarm",
                        onClick = { viewModel.saveAlarm() },
                        tint = colors.primaryAccent,
                    )
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        ) {

            // ── Section 1: Time Picker ─────────────────────────────────────
            item {
                TimePickerSection(
                    hour = state.hour,
                    minute = state.minute,
                    is24Hour = state.is24HourFormat,
                    onHourChange = viewModel::updateHour,
                    onMinuteChange = viewModel::updateMinute,
                )
            }

            // ── Section 2: Label ───────────────────────────────────────────
            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Label",
                            style = typography.labelMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        OutlinedTextField(
                            value = state.label,
                            onValueChange = viewModel::updateLabel,
                            placeholder = {
                                Text(
                                    "Alarm label",
                                    color = colors.secondaryText.copy(alpha = 0.5f),
                                )
                            },
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.primaryText,
                                unfocusedTextColor = colors.primaryText,
                                focusedBorderColor = colors.primaryAccent,
                                unfocusedBorderColor = colors.border,
                                focusedPlaceholderColor = colors.secondaryText,
                                unfocusedPlaceholderColor = colors.secondaryText,
                                cursorColor = colors.primaryAccent,
                                containerColor = colors.surfaceVariant,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "${state.label.length}/50",
                            style = typography.labelMedium,
                            color = colors.secondaryText,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }
            }

            // ── Section 3: Repeat Days ─────────────────────────────────────
            item {
                RepeatDaysSection(
                    selectedDays = state.repeatDays,
                    onDayToggle = viewModel::toggleRepeatDay,
                    onSetAll = viewModel::setAllDays,
                    onClearAll = viewModel::clearAllDays,
                    onSetWeekdays = viewModel::setWeekdays,
                    onSetWeekends = viewModel::setWeekends,
                )
            }

            // ── Section 4: Sound Selection ─────────────────────────────────
            item {
                SoundSelectorSection(
                    currentSound = state.soundUri,
                    onSoundSelected = viewModel::updateSound,
                    previewSound = { /* handled by SoundManager */ },
                )
            }

            // ── Section 5: Vibration & Volume ──────────────────────────────
            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "Sound & Vibration",
                            style = typography.labelMedium,
                            color = colors.secondaryText,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vibration toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vibration",
                                    style = typography.bodyLarge,
                                    color = colors.primaryText,
                                )
                                Text(
                                    text = "Vibrate device when alarm rings",
                                    style = typography.bodyMedium,
                                    color = colors.secondaryText,
                                )
                            }
                            WFToggle(
                                checked = state.vibrationEnabled,
                                onCheckedChange = { viewModel.toggleVibration() },
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            color = colors.border,
                            thickness = 1.dp,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gradual Volume toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gradual Volume",
                                    style = typography.bodyLarge,
                                    color = colors.primaryText,
                                )
                                Text(
                                    text = "Ramp volume up slowly for a gentler wake-up",
                                    style = typography.bodyMedium,
                                    color = colors.secondaryText,
                                )
                            }
                            WFToggle(
                                checked = state.gradualVolumeEnabled,
                                onCheckedChange = { viewModel.toggleGradualVolume() },
                            )
                        }

                        // Gradual volume duration slider (visible when enabled)
                        if (state.gradualVolumeEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))

                            val durationSteps = listOf(30, 60, 120, 300)
                            val durationLabels = listOf("30s", "1m", "2m", "5m")
                            val sliderPosition = durationSteps.indexOf(state.gradualVolumeDuration)
                                .coerceAtLeast(0)

                            Text(
                                text = "Ramp Duration",
                                style = typography.bodyMedium,
                                color = colors.secondaryText,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Slider(
                                value = sliderPosition.toFloat(),
                                onValueChange = { pos ->
                                    val idx = pos.toInt().coerceIn(0, durationSteps.size - 1)
                                    viewModel.updateGradualVolumeDuration(durationSteps[idx])
                                },
                                steps = durationSteps.size - 2,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.primaryAccent,
                                    activeTrackColor = colors.primaryAccent,
                                    inactiveTrackColor = colors.toggleInactive,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                durationLabels.forEach { label ->
                                    Text(
                                        text = label,
                                        style = typography.labelMedium,
                                        color = colors.secondaryText,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 6: Mission & Difficulty ────────────────────────────
            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "Dismissal Mission",
                            style = typography.labelMedium,
                            color = colors.secondaryText,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mission type chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MissionType.entries.forEach { type ->
                                val isPremiumLocked = type.isPremium
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    WFChip(
                                        label = type.displayName,
                                        selected = state.missionType == type,
                                        onClick = {
                                            if (!isPremiumLocked) {
                                                viewModel.setMissionType(type)
                                            }
                                        },
                                    )
                                    if (isPremiumLocked) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Premium",
                                            tint = colors.warning,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Difficulty selector
                        DifficultySelectorSection(
                            difficulty = state.difficulty,
                            onDifficultyChange = viewModel::setDifficulty,
                            missionType = state.missionType,
                        )
                    }
                }
            }

            // ── Section 7: Snooze Settings ─────────────────────────────────
            item {
                SnoozeSettingsSection(
                    snoozeInterval = state.snoozeInterval,
                    maxSnoozeCount = state.maxSnoozeCount,
                    smartEscalation = state.smartEscalationEnabled,
                    onIntervalChange = viewModel::updateSnoozeInterval,
                    onMaxCountChange = viewModel::updateMaxSnoozeCount,
                    onEscalationToggle = viewModel::toggleSmartEscalation,
                )
            }

            // ── Section 8: Strict Mode ─────────────────────────────────────
            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "Advanced Options",
                            style = typography.labelMedium,
                            color = colors.secondaryText,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Strict Mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Strict Mode",
                                        style = typography.bodyLarge,
                                        color = colors.primaryText,
                                    )
                                    Text(
                                        text = "Cannot dismiss alarm without completing mission",
                                        style = typography.bodyMedium,
                                        color = colors.secondaryText,
                                    )
                                }
                            }
                            WFToggle(
                                checked = state.strictModeEnabled,
                                onCheckedChange = { viewModel.toggleStrictMode() },
                            )
                        }

                        // Multi-Step (visible when strict mode is on)
                        if (state.strictModeEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = colors.border, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Multi-Step Missions",
                                        style = typography.bodyLarge,
                                        color = colors.primaryText,
                                    )
                                    Text(
                                        text = "Complete multiple missions to dismiss",
                                        style = typography.bodyMedium,
                                        color = colors.secondaryText,
                                    )
                                }
                                WFToggle(
                                    checked = state.multiStepEnabled,
                                    onCheckedChange = { viewModel.toggleMultiStep() },
                                )
                            }

                            if (state.multiStepEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Mission Steps",
                                    style = typography.bodyMedium,
                                    color = colors.secondaryText,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    listOf(2, 3, 4).forEach { count ->
                                        WFChip(
                                            label = "$count steps",
                                            selected = state.multiStepCount == count,
                                            onClick = { viewModel.updateMultiStepCount(count) },
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = colors.border, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Timed Mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Timed Mode",
                                    style = typography.bodyLarge,
                                    color = colors.primaryText,
                                )
                                Text(
                                    text = "Add a countdown timer to the mission",
                                    style = typography.bodyMedium,
                                    color = colors.secondaryText,
                                )
                            }
                            WFToggle(
                                checked = state.timedModeEnabled,
                                onCheckedChange = { viewModel.toggleTimedMode() },
                            )
                        }
                    }
                }
            }

            // ── Bottom safe area spacing ───────────────────────────────────
            item {
                Spacer(
                    Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
                )
            }
        }
    }
}
