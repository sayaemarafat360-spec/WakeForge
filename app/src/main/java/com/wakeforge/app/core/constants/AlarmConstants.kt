package com.wakeforge.app.core.constants

/**
 * Constants related to alarm scheduling, dispatch, and the ringing lifecycle.
 */
object AlarmConstants {

    // ── Intent / Bundle Extras ─────────────────────────────────────────────

    /** Key for the alarm ID passed through intents. */
    const val ALARM_ACTION_EXTRA_ALARM_ID = "extra_alarm_id"

    /** Action string: start ringing the alarm. */
    const val ALARM_ACTION_RING = "com.wakeforge.app.ACTION_ALARM_RING"

    /** Action string: dismiss / turn off the alarm. */
    const val ALARM_ACTION_DISMISS = "com.wakeforge.app.action.DISMISS"

    /** Action string: snooze the alarm. */
    const val ALARM_ACTION_SNOOZE = "com.wakeforge.app.action.SNOOZE"

    // ── Foreground Service ────────────────────────────────────────────────

    /** Notification ID for the alarm foreground service. */
    const val FOREGROUND_NOTIFICATION_ID = 2001

    // ── Mission Chain / Escalation ────────────────────────────────────────

    /** Maximum number of missions in a single alarm's chain. */
    const val MAX_MISSION_CHAIN_LENGTH = 4

    /** By how many ms the interval decreases each escalation step. */
    const val ESCALATION_INTERVAL_DECREASE_MS = 30_000L

    /** Minimum possible alarm interval after full escalation (30 s). */
    const val ESCALATION_MIN_INTERVAL_MS = 30_000L

    // ── Gradual Volume ────────────────────────────────────────────────────

    /** Default duration over which volume ramps from silent to max (seconds). */
    const val DEFAULT_GRADUAL_VOLUME_DURATION_SECONDS = 60
}
