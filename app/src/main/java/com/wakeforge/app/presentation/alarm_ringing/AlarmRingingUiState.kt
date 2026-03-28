package com.wakeforge.app.presentation.alarm_ringing

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * UI state for the alarm ringing screen.
 *
 * Contains all data needed to render the immersive full-screen alarm
 * experience, including the alarm details, current time, and snooze state.
 */
data class AlarmRingingUiState(
    /** The alarm that is currently ringing. */
    val alarm: AlarmRingingInfo? = null,
    /** The current wall-clock time, updated every second. */
    val currentTime: LocalDateTime = LocalDateTime.now(),
    /** Number of times the user has snoozed this alarm. */
    val snoozeCount: Int = 0,
    /** Maximum number of snoozes allowed for this alarm. */
    val maxSnoozeCount: Int = 3,
    /** Whether the user can still snooze. */
    val canSnooze: Boolean = true,
    /** Whether the mission challenge is currently active. */
    val isMissionActive: Boolean = false,
    /** Whether the screen is in a loading state. */
    val isLoading: Boolean = true,
    /** Error message if alarm loading fails. */
    val errorMessage: String? = null,
)

/**
 * Minimal alarm info needed for the ringing screen display.
 * Kept separate from the full domain [com.wakeforge.app.domain.models.Alarm]
 * to allow decoupled data flow for the UI layer.
 */
data class AlarmRingingInfo(
    val id: String,
    val hour: Int,
    val minute: Int,
    val label: String,
    val missionType: String,
    val difficulty: String,
    val snoozeIntervalMinutes: Int,
    val maxSnoozeCount: Int,
    val smartEscalationEnabled: Boolean,
    val timedModeEnabled: Boolean,
)
