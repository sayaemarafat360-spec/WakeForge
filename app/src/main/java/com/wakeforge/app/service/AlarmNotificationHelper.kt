package com.wakeforge.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wakeforge.app.core.constants.AlarmConstants
import com.wakeforge.app.core.constants.AppConstants
import com.wakeforge.app.core.utils.NotificationUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralised helper for building WakeForge alarm-related notifications.
 *
 * All notification construction is consolidated here so the [AlarmService]
 * and any other component that needs to post alarm notifications can
 * reference a single, consistent source of truth.
 */
@Singleton
class AlarmNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AlarmNotificationHelper"

        // Channel IDs (match those created in NotificationUtils)
        private const val CHANNEL_ALARM_ACTIVE = AppConstants.NOTIFICATION_CHANNEL_ALARM_ACTIVE
        private const val CHANNEL_ALARM_SNOOZE = AppConstants.NOTIFICATION_CHANNEL_ALARM_SNOOZE
        private const val CHANNEL_SCHEDULED = AppConstants.NOTIFICATION_CHANNEL_SCHEDULED

        // Request-code offsets for PendingIntent uniqueness
        private const val RC_DISMISS = 1000
        private const val RC_SNOOZE = 1001
        private const val RC_DISMISS_SNOOZE = 1002
        private const val RC_FULL_SCREEN = 2000
        private const val RC_CONTENT = 2001
    }

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // ──────────────────────────────────────────────────────────────────────────
    // Channel Management
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates all notification channels required by the alarm system.
     * Safe to call multiple times; channels are idempotent on API 26+.
     */
    fun createNotificationChannels() {
        NotificationUtils.createAllChannels(context)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Notification Builders
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Builds the foreground-service notification shown while an alarm is ringing.
     *
     * This notification is ongoing (cannot be swiped away) and includes
     * "Snooze" and "Dismiss" action buttons.
     *
     * @param alarmId   Unique identifier of the ringing alarm.
     * @param label     User-defined alarm label (e.g. "Morning Workout").
     * @param timeText  Formatted time string (e.g. "6:30 AM").
     * @return A fully constructed [Notification] ready to pass to `startForeground()`.
     */
    fun createAlarmNotification(
        alarmId: String,
        label: String,
        timeText: String
    ): Notification {
        val requestCodeBase = alarmId.hashCode() and 0xFFFF

        // ── Dismiss action ─────────────────────────────────────────────────
        val dismissIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DISMISS
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            context,
            requestCodeBase + RC_DISMISS,
            dismissIntent,
            pendingIntentFlags()
        )

        // ── Snooze action ──────────────────────────────────────────────────
        val snoozeIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getService(
            context,
            requestCodeBase + RC_SNOOZE,
            snoozeIntent,
            pendingIntentFlags()
        )

        // ── Content intent (tap notification → open app) ───────────────────
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            requestCodeBase + RC_CONTENT,
            contentIntent,
            pendingIntentFlags()
        )

        return NotificationCompat.Builder(context, CHANNEL_ALARM_ACTIVE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm: $label")
            .setContentText(timeText)
            .setSubText("Tap to manage")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(buildFullScreenIntent(alarmId), true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Snooze",
                snoozePendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Dismiss",
                dismissPendingIntent
            )
            .build()
    }

    /**
     * Builds a notification shown when an alarm has been snoozed.
     *
     * @param alarmId          Unique identifier of the snoozed alarm.
     * @param remainingMinutes Minutes until the alarm rings again.
     * @return A [Notification] indicating the snooze state.
     */
    fun createSnoozeNotification(
        alarmId: String,
        remainingMinutes: Int
    ): Notification {
        val requestCodeBase = alarmId.hashCode() and 0xFFFF

        // ── Dismiss action (permanently dismiss the snoozed alarm) ────────
        val dismissIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DISMISS
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            context,
            requestCodeBase + RC_DISMISS_SNOOZE,
            dismissIntent,
            pendingIntentFlags()
        )

        val minuteText = if (remainingMinutes == 1) "minute" else "minutes"

        return NotificationCompat.Builder(context, CHANNEL_ALARM_SNOOZE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm Snoozed")
            .setContentText("Ringing again in $remainingMinutes $minuteText")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_delete,
                "Dismiss",
                dismissPendingIntent
            )
            .build()
    }

    /**
     * Builds a low-priority notification informing the user about the
     * next scheduled alarm.
     *
     * @param nextAlarmTime Human-readable time string (e.g. "Tomorrow, 6:30 AM").
     * @return A [Notification] for the scheduled alarm channel.
     */
    fun createScheduledNotification(
        nextAlarmTime: String
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_SCHEDULED)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Next Alarm")
            .setContentText(nextAlarmTime)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Full-Screen Intent
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Builds a [PendingIntent] that launches the alarm ringing activity
     * as a full-screen intent (high-priority heads-up on Android 10+).
     *
     * @param alarmId The ID of the ringing alarm to pass to the activity.
     * @return A [PendingIntent] suitable for [NotificationCompat.setFullScreenIntent].
     */
    fun buildFullScreenIntent(alarmId: String): PendingIntent {
        // The AlarmRingingActivity is referenced via the package's component.
        // If you have a dedicated Activity class, reference it here.
        // For now, we use the launch intent as a fallback.
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        } ?: Intent().apply {
            setPackage(context.packageName)
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return PendingIntent.getActivity(
            context,
            (alarmId.hashCode() and 0xFFFF) + RC_FULL_SCREEN,
            intent,
            pendingIntentFlags()
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Notification Management
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Dismisses a previously posted notification by its ID.
     *
     * @param notificationId The ID passed to [NotificationManager.notify].
     */
    fun dismissNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Updates the foreground-service notification in-place.
     *
     * Used when transitioning from "ringing" to "snoozed" state without
     * stopping the foreground service.
     *
     * @param notification The new [Notification] to display.
     */
    fun updateForegroundNotification(notification: Notification) {
        notificationManager.notify(
            AlarmConstants.FOREGROUND_NOTIFICATION_ID,
            notification
        )
    }

    /**
     * Cancels all WakeForge alarm-related notifications.
     */
    fun dismissAllAlarmNotifications() {
        notificationManager.cancel(AlarmConstants.FOREGROUND_NOTIFICATION_ID)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns the correct [PendingIntent] flags for the current API level.
     * Always includes [PendingIntent.FLAG_IMMUTABLE] on API 23+.
     */
    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_IMMUTABLE
                else
                    0)
    }
}
