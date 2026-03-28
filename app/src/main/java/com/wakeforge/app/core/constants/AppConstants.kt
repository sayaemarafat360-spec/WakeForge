package com.wakeforge.app.core.constants

/**
 * Application-wide constants for WakeForge.
 * Grouped by domain: package info, notification channels, alarm defaults, billing.
 */
object AppConstants {

    // ── Package ────────────────────────────────────────────────────────────

    const val PACKAGE_NAME = "com.wakeforge.app"

    // ── Notification Channel IDs ──────────────────────────────────────────

    /** Channel for the foreground service while an alarm is ringing. */
    const val NOTIFICATION_CHANNEL_ALARM_ACTIVE = "alarm_active"

    /** Channel for notifications shown while an alarm is snoozed. */
    const val NOTIFICATION_CHANNEL_ALARM_SNOOZE = "alarm_snooze"

    /** Channel for mission-related reminders (if any). */
    const val NOTIFICATION_CHANNEL_MISSION_REMINDER = "mission_reminder"

    /** Channel for "next alarm scheduled" informational notifications. */
    const val NOTIFICATION_CHANNEL_SCHEDULED = "scheduled_alarm"

    // ── Alarm Defaults ────────────────────────────────────────────────────

    /** Base request code for alarm PendingIntent — real code is base + alarmIndex. */
    const val ALARM_REQUEST_CODE_BASE = 1000

    /** Default snooze interval in minutes. */
    const val DEFAULT_SNOOZE_INTERVAL_MINUTES = 5

    /** Default maximum number of snoozes allowed before the alarm is force-dismissed. */
    const val DEFAULT_MAX_SNOOZE_COUNT = 3

    /** Default mission timeout in milliseconds (5 minutes). */
    const val DEFAULT_MISSION_TIMEOUT_MS = 300_000L

    /** Maximum snooze interval the user can configure (minutes). */
    const val MAX_SNOOZE_INTERVAL = 30

    /** Maximum snooze count the user can configure. */
    const val MAX_SNOOZE_COUNT = 10

    // ── Billing / Premium ─────────────────────────────────────────────────

    /** Cooldown between rewarded ad views in milliseconds (5 minutes). */
    const val REWARDED_AD_COOLDOWN_MS = 300_000L

    /** Number of grace days after which the user is prompted about premium. */
    const val GRACE_PERIOD_DAYS = 3
}
