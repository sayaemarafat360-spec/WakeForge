package com.wakeforge.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.wakeforge.app.core.constants.AlarmConstants
import com.wakeforge.app.core.constants.AppConstants
import com.wakeforge.app.data.alarm.AlarmScheduler
import com.wakeforge.app.data.database.dao.AlarmDao
import com.wakeforge.app.data.database.entity.AlarmEntity
import com.wakeforge.app.data.sound.SoundManager
import com.wakeforge.app.domain.models.Alarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground [Service] responsible for managing an active alarm session.
 *
 * Lifecycle:
 * 1. **Started** by [com.wakeforge.app.data.alarm.AlarmReceiver] when an alarm fires.
 * 2. **Foreground** notification is posted immediately with snooze / dismiss actions.
 * 3. **WakeLock** is acquired so the device stays awake while the alarm rings.
 * 4. **Sound** is played via [SoundManager].
 * 5. The service remains active until explicitly stopped via [stopAlarm] or [snoozeAlarm].
 *
 * @see AlarmNotificationHelper for notification construction details.
 */
@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        private const val TAG = "AlarmService"

        /** Intent extra key carrying the alarm ID. */
        const val EXTRA_ALARM_ID = "extra_alarm_id"

        /** Intent action broadcast by the notification "Snooze" button. */
        const val ACTION_SNOOZE = "com.wakeforge.app.service.ACTION_SNOOZE"

        /** Intent action broadcast by the notification "Dismiss" button. */
        const val ACTION_DISMISS = "com.wakeforge.app.service.ACTION_DISMISS"

        /** Intent action to stop alarm sound from the UI (e.g. mission screen). */
        const val ACTION_STOP_SOUND = "com.wakeforge.app.service.ACTION_STOP_SOUND"
    }

    // ── Injected Dependencies ────────────────────────────────────────────────

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var alarmDao: AlarmDao

    @Inject
    lateinit var notificationHelper: AlarmNotificationHelper

    // ── Internal State ───────────────────────────────────────────────────────

    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarmId: String? = null
    private var currentAlarm: Alarm? = null
    private var snoozeCount: Int = 0
    private var serviceScope: CoroutineScope? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var snoozeRunnable: Runnable? = null

    // ── Service Lifecycle ────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // Create notification channels (idempotent on API 26+)
        notificationHelper.createNotificationChannels()

        Log.d(TAG, "AlarmService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Log.w(TAG, "Null intent received, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent.action
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)

        Log.d(TAG, "onStartCommand: action=$action, alarmId=$alarmId")

        when (action) {
            AlarmScheduler.ALARM_ACTION_RING -> {
                if (alarmId.isNullOrBlank()) {
                    Log.e(TAG, "Alarm ID is null or blank for RING action")
                    stopSelf()
                    return START_NOT_STICKY
                }
                handleStartAlarm(alarmId)
            }

            ACTION_SNOOZE -> {
                val snoozeAlarmId = alarmId ?: currentAlarmId
                if (snoozeAlarmId != null) {
                    handleSnoozeAlarm(snoozeAlarmId)
                } else {
                    Log.w(TAG, "SNOOZE action received but no alarm ID available")
                }
            }

            ACTION_DISMISS -> {
                val dismissAlarmId = alarmId ?: currentAlarmId
                if (dismissAlarmId != null) {
                    handleDismissAlarm(dismissAlarmId)
                } else {
                    Log.w(TAG, "DISMISS action received but no alarm ID available")
                }
            }

            ACTION_STOP_SOUND -> {
                Log.d(TAG, "Stop sound action received")
                soundManager.stopAlarm()
            }

            else -> {
                Log.w(TAG, "Unknown action: $action")
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        Log.d(TAG, "AlarmService destroyed")
    }

    // ── Alarm Handlers ───────────────────────────────────────────────────────

    /**
     * Starts the alarm: posts foreground notification, acquires wake lock,
     * loads alarm data, and begins playing the alarm sound.
     */
    private fun handleStartAlarm(alarmId: String) {
        if (currentAlarmId == alarmId) {
            Log.d(TAG, "Alarm $alarmId is already ringing, ignoring duplicate start")
            return
        }

        currentAlarmId = alarmId

        serviceScope?.launch {
            try {
                // Load alarm details from the database
                val entity = alarmDao.getAlarmByIdOnce(alarmId)
                if (entity == null) {
                    Log.e(TAG, "Alarm $alarmId not found in database")
                    stopAlarm()
                    return@launch
                }

                currentAlarm = entity.toDomain()
                val alarm = currentAlarm!!

                // Update snooze count from intent extras if available
                snoozeCount = 0

                // Post foreground notification
                val timeText = formatTimeText(alarm.hour, alarm.minute)
                val notification = notificationHelper.createAlarmNotification(
                    alarmId = alarmId,
                    label = alarm.label.ifBlank { "Alarm" },
                    timeText = timeText
                )

                startForeground(
                    AlarmConstants.FOREGROUND_NOTIFICATION_ID,
                    notification
                )
                Log.d(TAG, "Foreground notification posted for alarm $alarmId")

                // Acquire wake lock to keep the CPU awake
                acquireWakeLock(alarmId)

                // Play alarm sound
                val volume = if (alarm.vibrationEnabled) 1.0f else 0.8f
                soundManager.playAlarm(
                    soundUri = alarm.soundUri,
                    volume = volume,
                    looping = true
                )
                Log.d(TAG, "Alarm sound started for alarm $alarmId")

            } catch (e: Exception) {
                Log.e(TAG, "Error starting alarm $alarmId", e)
                stopAlarm()
            }
        }
    }

    /**
     * Snoozes the current alarm: stops the sound, posts a snooze notification,
     * and schedules the next ring via [AlarmScheduler].
     */
    private fun handleSnoozeAlarm(alarmId: String) {
        val alarm = currentAlarm ?: return

        snoozeCount++

        // Enforce max snooze count
        val maxSnoozes = alarm.maxSnoozeCount
        if (maxSnoozes > 0 && snoozeCount > maxSnoozes) {
            Log.d(TAG, "Max snooze count ($maxSnoozes) reached for alarm $alarmId, forcing dismiss")
            handleDismissAlarm(alarmId)
            return
        }

        // Stop the alarm sound
        soundManager.stopAlarm()

        val snoozeInterval = alarm.snoozeIntervalMinutes

        // Update notification to snoozed state
        val snoozeNotification = notificationHelper.createSnoozeNotification(
            alarmId = alarmId,
            remainingMinutes = snoozeInterval
        )
        notificationHelper.updateForegroundNotification(snoozeNotification)

        // Schedule the next alarm ring
        alarmScheduler.scheduleSnoozeAlarm(alarmId, snoozeInterval)
        Log.d(TAG, "Alarm $alarmId snoozed for $snoozeInterval minutes (snooze #$snoozeCount)")

        // Release the wake lock while snoozing to save battery
        releaseWakeLock()

        // Stop the foreground service; the next ring will restart it
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    /**
     * Dismisses the alarm completely: stops sound, releases wake lock,
     * cancels any pending snooze, and stops the service.
     */
    private fun handleDismissAlarm(alarmId: String) {
        Log.d(TAG, "Dismissing alarm $alarmId")
        stopAlarm()
    }

    // ── Public API (callable from other components via LocalBroadcast / intent) ──

    /**
     * Fully stops the alarm service: silences sound, releases wake lock,
     * cancels pending snooze handlers, and stops the foreground service.
     */
    fun stopAlarm() {
        soundManager.stopAlarm()
        releaseWakeLock()

        snoozeRunnable?.let { mainHandler.removeCallbacks(it) }
        snoozeRunnable = null

        notificationHelper.dismissNotification(AlarmConstants.FOREGROUND_NOTIFICATION_ID)

        currentAlarmId = null
        currentAlarm = null
        snoozeCount = 0

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "Alarm stopped and service terminated")
    }

    /**
     * Temporarily stops the alarm sound and schedules it to resume after the
     * alarm's configured snooze interval.
     */
    fun snoozeAlarm() {
        val alarmId = currentAlarmId ?: return
        handleSnoozeAlarm(alarmId)
    }

    /**
     * Returns the ID of the currently active alarm, or null if no alarm is
     * currently being managed by this service.
     */
    fun getActiveAlarmId(): String? = currentAlarmId

    /**
     * Returns the current snooze count for the active alarm.
     */
    fun getSnoozeCount(): Int = snoozeCount

    /**
     * Returns the [Alarm] object for the currently ringing alarm, or null.
     */
    fun getCurrentAlarm(): Alarm? = currentAlarm

    // ── Internal Helpers ─────────────────────────────────────────────────────

    /**
     * Acquires a [PowerManager.PARTIAL_WAKE_LOCK] to ensure the CPU stays
     * awake while the alarm is active.
     */
    private fun acquireWakeLock(tag: String) {
        releaseWakeLock() // Release any existing lock first

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WakeForge:AlarmWakeLock:$tag"
        ).apply {
            acquire(10 * 60 * 1000L /* 10 minutes max, refreshed by service */)
            Log.d(TAG, "WakeLock acquired for alarm $tag")
        }
    }

    /**
     * Releases the wake lock if it is currently held.
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        } finally {
            wakeLock = null
        }
    }

    /**
     * Cleans up all resources: coroutine scope, sound, wake lock.
     */
    private fun cleanup() {
        soundManager.stopAlarm()
        releaseWakeLock()

        snoozeRunnable?.let { mainHandler.removeCallbacks(it) }
        snoozeRunnable = null

        serviceScope?.cancel()
        serviceScope = null

        currentAlarmId = null
        currentAlarm = null
    }

    /**
     * Formats an hour/minute pair into a human-readable time string.
     */
    private fun formatTimeText(hour: Int, minute: Int): String {
        val isPm = hour >= 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (isPm) "PM" else "AM"
        val minuteStr = minute.toString().padStart(2, '0')
        return "$displayHour:$minuteStr $amPm"
    }
}
