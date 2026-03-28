package com.wakeforge.app.core.extensions

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

// ──────────────────────────────────────────────────────────────────────────────
// Alarm Permission (Android 12+)
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns `true` if the app has been granted `SCHEDULE_EXACT_ALARM` permission.
 * On pre-Android 12 this always returns `true` because the permission doesn't exist.
 */
fun Context.isExactAlarmPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

/**
 * Launches the system settings screen so the user can grant `SCHEDULE_EXACT_ALARM`.
 * No-op on pre-Android 12.
 */
fun Context.requestExactAlarmPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Notification Permission
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns `true` if the app can post notifications.
 * On Android 13+ this checks `POST_NOTIFICATIONS`; on older versions it checks
 * whether notifications are enabled in system settings.
 */
fun Context.isNotificationPermissionGranted(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

// ──────────────────────────────────────────────────────────────────────────────
// Battery Optimization
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns `true` if the app is on the device's battery optimization whitelist.
 * When this returns `false` the OS may kill alarms in doze mode.
 */
fun Context.isBatteryOptimizationIgnored(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
    return powerManager?.isIgnoringBatteryOptimizations(packageName) == true
}

/**
 * Opens the system settings screen where the user can disable battery optimization
 * for WakeForge.
 */
fun Context.requestBatteryOptimizationExemption() {
    startActivity(
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Do Not Disturb
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns `true` if the app has been granted DND access (required to
 * override DND so alarms still ring).
 */
fun Context.isDNDAccessGranted(): Boolean {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        notificationManager?.isNotificationPolicyAccessGranted == true
    } else {
        true // DND access API doesn't exist pre-M
    }
}

/**
 * Opens the system DND access settings so the user can grant permission.
 */
fun Context.requestDNDAccess() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        startActivity(
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Activity Recognition & Sensors
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns `true` if the app has been granted `ACTIVITY_RECOGNITION` permission
 * (required for the step-count mission on Android 10+).
 */
fun Context.hasActivityRecognitionPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

/**
 * Returns `true` if the device has a step counter sensor (TYPE_STEP_COUNTER).
 * When absent the step-count mission should be hidden from the mission picker.
 */
fun Context.isStepSensorAvailable(): Boolean {
    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    return sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}

// ──────────────────────────────────────────────────────────────────────────────
// Vibration
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Triggers device vibration with the given [pattern] of
 * `[duration_on, duration_off, duration_on, ...]` (in milliseconds).
 * Requires `android.permission.VIBRATE`.
 *
 * Example: `vibrate(longArrayOf(0, 100, 50, 100))` — instant buzz, pause, buzz.
 */
fun Context.vibrate(pattern: LongArray) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    if (vibrator == null || !vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}

/**
 * Triggers a short, single-shot vibration of [durationMs] milliseconds.
 */
fun Context.vibrate(durationMs: Long = 100L) {
    vibrate(longArrayOf(0, durationMs))
}
