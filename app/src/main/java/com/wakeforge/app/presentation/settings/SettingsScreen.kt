package com.wakeforge.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.components.WFDialog
import com.wakeforge.app.core.components.WFToggle
import com.wakeforge.app.core.components.WFTopBar
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.WakeForgeShapes
import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.presentation.navigation.Route

/**
 * Clean, elegant settings screen organized into clearly labeled sections.
 *
 * Sections:
 * 1. **Appearance**: Theme mode selection (Dark/Light/System)
 * 2. **Alarm Defaults**: Snooze interval, max snooze, mission type, difficulty, strict mode
 * 3. **Sound & Vibration**: Volume slider, vibration intensity slider
 * 4. **Premium**: Upgrade link, premium status
 * 5. **About**: Privacy policy, app version, open source licenses
 * 6. **Danger Zone**: Reset settings, clear all data (with confirmation dialogs)
 */
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    // Dialog states
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showMissionDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsViewModel.SettingsEvent.NavigateToPremium -> {
                    navController?.navigate(Route.Premium.route)
                }
                SettingsViewModel.SettingsEvent.ShowAbout -> {
                    // Could show an about dialog or navigate
                }
                SettingsViewModel.SettingsEvent.ResetSettings -> {
                    showResetDialog = false
                }
                SettingsViewModel.SettingsEvent.ClearAllData -> {
                    showClearDataDialog = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WFTopBar(title = "Settings")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Appearance Section ──────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Appearance")
            }

            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsRow(
                            label = "Theme",
                            value = null,
                            onClick = null,
                            leadingIcon = {
                                Icon(
                                    imageVector = when (uiState.themeMode) {
                                        AppSettings.ThemeMode.DARK -> Icons.Default.DarkMode
                                        AppSettings.ThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppSettings.ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                    },
                                    contentDescription = null,
                                    tint = colors.secondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WFChip(
                                    label = "Dark",
                                    selected = uiState.themeMode == AppSettings.ThemeMode.DARK,
                                    onClick = { viewModel.updateThemeMode(AppSettings.ThemeMode.DARK) }
                                )
                                WFChip(
                                    label = "Light",
                                    selected = uiState.themeMode == AppSettings.ThemeMode.LIGHT,
                                    onClick = { viewModel.updateThemeMode(AppSettings.ThemeMode.LIGHT) }
                                )
                                WFChip(
                                    label = "System",
                                    selected = uiState.themeMode == AppSettings.ThemeMode.SYSTEM,
                                    onClick = { viewModel.updateThemeMode(AppSettings.ThemeMode.SYSTEM) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Alarm Defaults Section ──────────────────────────────────
            item {
                SettingsSectionHeader(title = "Alarm Defaults")
            }

            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // Default Snooze Interval
                        SettingsRow(
                            label = "Snooze Interval",
                            value = "${uiState.defaultSnoozeInterval} min",
                            onClick = { showSnoozeDialog = true }
                        )

                        SettingsDivider(colors = colors)

                        // Default Max Snooze
                        SettingsRow(
                            label = "Max Snooze Count",
                            value = "${uiState.defaultMaxSnoozeCount}",
                            onClick = { showSnoozeDialog = true }
                        )

                        SettingsDivider(colors = colors)

                        // Default Mission Type
                        SettingsRow(
                            label = "Mission Type",
                            value = formatMissionType(uiState.defaultMissionType),
                            onClick = { showMissionDialog = true }
                        )

                        SettingsDivider(colors = colors)

                        // Default Difficulty
                        SettingsRow(
                            label = "Difficulty",
                            value = formatDifficulty(uiState.defaultDifficulty),
                            onClick = { showDifficultyDialog = true }
                        )

                        SettingsDivider(colors = colors)

                        // Strict Mode Default
                        SettingsToggleRow(
                            label = "Strict Mode Default",
                            subtitle = "Disable snooze by default",
                            checked = uiState.strictModeDefault,
                            onCheckedChange = { viewModel.updateStrictModeDefault(it) }
                        )
                    }
                }
            }

            // ── Sound & Vibration Section ──────────────────────────────
            item {
                SettingsSectionHeader(title = "Sound & Vibration")
            }

            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Alarm Volume
                        Text(
                            text = "Alarm Volume",
                            style = typography.labelLarge,
                            color = colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = uiState.soundVolume,
                            onValueChange = { viewModel.updateSoundVolume(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primaryAccent,
                                activeTrackColor = colors.primaryAccent,
                                inactiveTrackColor = colors.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${(uiState.soundVolume * 100).toInt()}%",
                            style = typography.labelMedium,
                            color = colors.secondaryText
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Vibration Intensity
                        Text(
                            text = "Vibration Intensity",
                            style = typography.labelLarge,
                            color = colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = uiState.vibrationIntensity.toFloat(),
                            onValueChange = { viewModel.updateVibrationIntensity(it.toInt()) },
                            colors = SliderDefaults.colors(
                                thumbColor = colors.secondaryAccent,
                                activeTrackColor = colors.secondaryAccent,
                                inactiveTrackColor = colors.surfaceVariant
                            ),
                            valueRange = 0f..100f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${uiState.vibrationIntensity}%",
                            style = typography.labelMedium,
                            color = colors.secondaryText
                        )
                    }
                }
            }

            // ── Premium Section ─────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Premium")
            }

            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow(
                        label = "Upgrade to Premium",
                        value = if (uiState.isPremium) "Active" else null,
                        onClick = { viewModel.navigateToPremium() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (uiState.isPremium) colors.success else colors.warning,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                }
            }

            // ── About Section ───────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "About")
            }

            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            label = "Privacy Policy",
                            value = null,
                            onClick = { /* Open privacy policy URL */ },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = colors.secondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )

                        SettingsDivider(colors = colors)

                        SettingsRow(
                            label = "About WakeForge",
                            value = "v${uiState.appVersion}",
                            onClick = { viewModel.showAbout() },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.secondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )

                        SettingsDivider(colors = colors)

                        SettingsRow(
                            label = "Open Source Licenses",
                            value = null,
                            onClick = { /* Show licenses screen */ },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = colors.secondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )
                    }
                }
            }

            // ── Danger Zone Section ─────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Danger Zone")
            }

            item {
                WFCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // Reset All Settings
                        SettingsRow(
                            label = "Reset All Settings",
                            value = null,
                            onClick = { showResetDialog = true },
                            labelColor = colors.error
                        )

                        SettingsDivider(colors = colors)

                        // Clear All Data
                        SettingsRow(
                            label = "Clear All Data",
                            value = null,
                            onClick = { showClearDataDialog = true },
                            labelColor = colors.error
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────

    // Reset Settings Confirmation Dialog
    if (showResetDialog) {
        WFDialog(
            onDismissRequest = { showResetDialog = false },
            title = "Reset Settings",
            message = "This will reset all settings to their default values. Your alarms and wake-up history will not be affected.",
            confirmText = "Reset",
            dismissText = "Cancel",
            onConfirm = { viewModel.resetSettings() },
            onDismiss = { showResetDialog = false }
        )
    }

    // Clear All Data Confirmation Dialog
    if (showClearDataDialog) {
        WFDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = "Clear All Data",
            message = "This will permanently delete all your alarms, wake-up history, and settings. This action cannot be undone.",
            confirmText = "Clear",
            dismissText = "Cancel",
            onConfirm = { viewModel.clearAllData() },
            onDismiss = { showClearDataDialog = false }
        )
    }

    // Snooze Configuration Dialog
    if (showSnoozeDialog) {
        SnoozeConfigDialog(
            currentInterval = uiState.defaultSnoozeInterval,
            currentMaxCount = uiState.defaultMaxSnoozeCount,
            onDismiss = { showSnoozeDialog = false },
            onConfirm = { interval, maxCount ->
                viewModel.updateSnoozeDefaults(interval, maxCount)
                showSnoozeDialog = false
            }
        )
    }

    // Mission Type Selection Dialog
    if (showMissionDialog) {
        MissionTypeDialog(
            currentType = uiState.defaultMissionType,
            onDismiss = { showMissionDialog = false },
            onSelect = { type ->
                viewModel.updateMissionDefaults(type, uiState.defaultDifficulty)
                showMissionDialog = false
            }
        )
    }

    // Difficulty Selection Dialog
    if (showDifficultyDialog) {
        DifficultyDialog(
            currentDifficulty = uiState.defaultDifficulty,
            onDismiss = { showDifficultyDialog = false },
            onSelect = { difficulty ->
                viewModel.updateMissionDefaults(uiState.defaultMissionType, difficulty)
                showDifficultyDialog = false
            }
        )
    }
}

/**
 * Dialog for configuring snooze interval and max snooze count.
 * Shows two sliders: interval (1–30 min) and max count (0–10).
 */
@Composable
private fun SnoozeConfigDialog(
    currentInterval: Int,
    currentMaxCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    var interval by remember { mutableStateOf(currentInterval.toFloat()) }
    var maxCount by remember { mutableStateOf(currentMaxCount.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = WakeForgeShapes.medium,
        title = {
            Text(
                text = "Snooze Settings",
                style = typography.headlineMedium,
                color = colors.primaryText
            )
        },
        text = {
            Column {
                Text(
                    text = "Snooze Interval",
                    style = typography.labelLarge,
                    color = colors.primaryText
                )
                Slider(
                    value = interval,
                    onValueChange = { interval = it },
                    valueRange = 1f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primaryAccent,
                        activeTrackColor = colors.primaryAccent,
                        inactiveTrackColor = colors.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${interval.toInt()} min",
                    style = typography.labelMedium,
                    color = colors.secondaryText
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Max Snooze Count",
                    style = typography.labelLarge,
                    color = colors.primaryText
                )
                Slider(
                    value = maxCount,
                    onValueChange = { maxCount = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primaryAccent,
                        activeTrackColor = colors.primaryAccent,
                        inactiveTrackColor = colors.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${maxCount.toInt()} times",
                    style = typography.labelMedium,
                    color = colors.secondaryText
                )
            }
        },
        confirmButton = {
            WFButton(
                text = "Save",
                onClick = {
                    onConfirm(interval.toInt(), maxCount.toInt())
                    onDismiss()
                },
                type = ButtonType.Primary
            )
        },
        dismissButton = {
            WFButton(
                text = "Cancel",
                onClick = onDismiss,
                type = ButtonType.Ghost
            )
        }
    )
}

/**
 * Dialog for selecting the default mission type.
 * Shows a column of selectable items for each MissionType enum value.
 */
@Composable
private fun MissionTypeDialog(
    currentType: MissionType,
    onDismiss: () -> Unit,
    onSelect: (MissionType) -> Unit
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val types = MissionType.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = WakeForgeShapes.medium,
        title = {
            Text(
                text = "Default Mission Type",
                style = typography.headlineMedium,
                color = colors.primaryText
            )
        },
        text = {
            Column {
                types.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(type)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMissionType(type),
                            style = typography.bodyLarge,
                            color = if (type == currentType) colors.primaryAccent else colors.primaryText,
                            fontWeight = if (type == currentType) FontWeight.Bold else FontWeight.Normal
                        )
                        if (type == currentType) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "●",
                                color = colors.primaryAccent,
                                style = typography.labelLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            WFButton(
                text = "Cancel",
                onClick = onDismiss,
                type = ButtonType.Ghost
            )
        }
    )
}

/**
 * Dialog for selecting the default difficulty level.
 * Shows a column of selectable items for each MissionDifficulty enum value.
 */
@Composable
private fun DifficultyDialog(
    currentDifficulty: MissionDifficulty,
    onDismiss: () -> Unit,
    onSelect: (MissionDifficulty) -> Unit
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val difficulties = MissionDifficulty.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = WakeForgeShapes.medium,
        title = {
            Text(
                text = "Default Difficulty",
                style = typography.headlineMedium,
                color = colors.primaryText
            )
        },
        text = {
            Column {
                difficulties.forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(difficulty)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDifficulty(difficulty),
                            style = typography.bodyLarge,
                            color = if (difficulty == currentDifficulty) colors.primaryAccent else colors.primaryText,
                            fontWeight = if (difficulty == currentDifficulty) FontWeight.Bold else FontWeight.Normal
                        )
                        if (difficulty == currentDifficulty) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "●",
                                color = colors.primaryAccent,
                                style = typography.labelLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            WFButton(
                text = "Cancel",
                onClick = onDismiss,
                type = ButtonType.Ghost
            )
        }
    )
}

// ── Helper Functions ───────────────────────────────────────────────────

private fun formatMissionType(type: MissionType): String {
    return type.displayName.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

private fun formatDifficulty(difficulty: MissionDifficulty): String {
    return difficulty.displayName.replaceFirstChar { it.uppercase() }
}

/**
 * Thin divider line between settings rows.
 */
@Composable
private fun SettingsDivider(
    colors: com.wakeforge.app.core.theme.ThemePalette
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(horizontal = 16.dp)
    )
}

/**
 * A single settings row with label, optional value, optional icon, and optional trailing content.
 */
@Composable
private fun SettingsRow(
    label: String,
    value: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    labelColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                } else {
                    Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = label,
            style = typography.bodyLarge,
            color = labelColor ?: colors.primaryText,
            modifier = Modifier.weight(1f)
        )

        if (trailing != null) {
            trailing()
        } else if (value != null) {
            Text(
                text = value,
                style = typography.bodyMedium,
                color = colors.secondaryText
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.secondaryText.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.secondaryText.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Settings row with a toggle control on the right side.
 */
@Composable
private fun SettingsToggleRow(
    label: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.primaryText
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = typography.labelMedium,
                    color = colors.secondaryText
                )
            }
        }

        WFToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Section header label for settings groups.
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    val typography = LocalWakeForgeTypography.current
    val colors = LocalWakeForgeColors.current

    Text(
        text = title,
        style = typography.labelLarge,
        color = colors.secondaryText,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 4.dp)
    )
}
