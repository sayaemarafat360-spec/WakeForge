package com.wakeforge.app.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wakeforge.app.core.constants.AlarmConstants
import com.wakeforge.app.core.constants.AppConstants

/**
 * Utility object for creating notification channels and building WakeForge notifications.
 */
object NotificationUtils {

    // ────────────────────────────────────────────────────────────────────────
    // Channel IDs (mirrored from AppConstants for self-containment)
    // ────────────────────────────────────────────────────────────────────────

    private const val CHANNEL_ALARM_ACTIVE      = AppConstants.NOTIFICATION_CHANNEL_ALARM_ACTIVE
    private const val CHANNEL_ALARM_SNOOZE      = AppConstants.NOTIFICATION_CHANNEL_ALARM_SNOOZE
    private const val CHANNEL_MISSION_REMINDER  = AppConstants.NOTIFICATION_CHANNEL_MISSION_REMINDER
    private const val CHANNEL_SCHEDULED         = AppConstants.NOTIFICATION_CHANNEL_SCHEDULED

    // ────────────────────────────────────────────────────────────────────────
    // Channel Names
    // ────────────────────────────────────────────────────────────────────────

    private const val CHANNEL_NAME_ALARM_ACTIVE     = "Active Alarm"
    private const val CHANNEL_NAME_ALARM_SNOOZE     = "Snoozed Alarm"
    private const val CHANNEL_NAME_MISSION_REMINDER = "Mission Reminders"
    private const val CHANNEL_NAME_SCHEDULED        = "Scheduled Alarms"

    // ────────────────────────────────────────────────────────────────────────
    // Channel Creation
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Creates (or updates) a notification channel.
     * No-op on pre-Android 8 where channels don't exist.
     *
     * @param context     Application context.
     * @param channelId   Unique channel ID.
     * @param channelName User-visible channel name.
     * @param importance  One of [NotificationManager.IMPORTANCE_*].
     */
    fun createAlarmChannel(
        context: Context,
        channelId: String,
        channelName: String,
        importance: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance,
            ).apply {
                description = "WakeForge: $channelName"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates all notification channels used by WakeForge.
     * Should be called once from [android.app.Application.onCreate].
     */
    fun createAllChannels(context: Context) {
        createAlarmChannel(
            context,
            CHANNEL_ALARM_ACTIVE,
            CHANNEL_NAME_ALARM_ACTIVE,
            NotificationManager.IMPORTANCE_HIGH,
        )
        createAlarmChannel(
            context,
            CHANNEL_ALARM_SNOOZE,
            CHANNEL_NAME_ALARM_SNOOZE,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        createAlarmChannel(
            context,
            CHANNEL_MISSION_REMINDER,
            CHANNEL_NAME_MISSION_REMINDER,
            NotificationManager.IMPORTANCE_LOW,
        )
        createAlarmChannel(
            context,
            CHANNEL_SCHEDULED,
            CHANNEL_NAME_SCHEDULED,
            NotificationManager.IMPORTANCE_LOW,
        )
    }

    // ────────────────────────────────────────────────────────────────────────
    // Notification Builders
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Builds a notification shown when the alarm is snoozed.
     *
     * @param context          Application context.
     * @param alarmId          Unique alarm identifier.
     * @param remainingMinutes Minutes until the alarm rings again.
     */
    fun buildSnoozeNotification(
        context: Context,
        alarmId: String,
        remainingMinutes: Int,
    ): Notification {
        val dismissIntent = Intent(AlarmConstants.ALARM_ACTION_DISMISS).apply {
            setPackage(context.packageName)
            putExtra(AlarmConstants.ALARM_ACTION_EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId.hashCode() and 0xFFFF) + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, CHANNEL_ALARM_SNOOZE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm Snoozed")
            .setContentText("Ringing again in $remainingMinutes minute${if (remainingMinutes != 1) "s" else ""}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_delete,
                "Dismiss",
                dismissPendingIntent,
            )
            .build()
    }

    /**
     * Builds a low-priority notification showing the next scheduled alarm time.
     *
     * @param context       Application context.
     * @param nextAlarmTime Formatted string like "Tomorrow, 6:30 AM".
     */
    fun buildScheduledAlarmNotification(
        context: Context,
        nextAlarmTime: String,
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

    // ────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Cancels a previously posted notification.
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    /**
     * Cancels the foreground-service notification for an active alarm.
     */
    fun cancelActiveAlarmNotification(context: Context) {
        cancelNotification(context, AlarmConstants.FOREGROUND_NOTIFICATION_ID)
    }
}
