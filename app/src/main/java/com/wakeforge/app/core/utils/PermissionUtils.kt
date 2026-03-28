package com.wakeforge.app.core.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.wakeforge.app.core.extensions.isBatteryOptimizationIgnored
import com.wakeforge.app.core.extensions.isExactAlarmPermissionGranted
import com.wakeforge.app.core.extensions.isNotificationPermissionGranted

/**
 * Utility object for managing runtime permissions required by WakeForge.
 */
object PermissionUtils {

    // ── Required Permissions ───────────────────────────────────────────────

    /**
     * List of all permissions WakeForge may need at runtime.
     * Not all of these are needed on every API level; callers should use
     * [getMissingPermissions] for a contextual list.
     */
    val REQUIRED_PERMISSIONS: List<String> = buildList {
        // Notification permission — Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Activity recognition — Android 10+ (for step-count mission)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        // Vibrate is a normal permission on API < 1, but we include it for completeness
        // (it doesn't require runtime request).
        add(Manifest.permission.VIBRATE)
    }

    // ────────────────────────────────────────────────────────────────────────
    // Query Helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Returns the subset of [REQUIRED_PERMISSIONS] that have NOT yet been granted.
     * This does NOT include special permissions like SCHEDULE_EXACT_ALARM or
     * battery optimization — use the dedicated extension functions for those.
     */
    fun getMissingPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            context.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns `true` if the user has previously denied [permission] and
     * checked "Don't ask again". When true, the app should show a rationale
     * dialog directing the user to system settings.
     */
    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Returns `true` if all [grantResults] are `true`.
     * Typically called from `onRequestPermissionsResult`.
     */
    fun allPermissionsGranted(grantResults: Map<String, Boolean>): Boolean {
        return grantResults.values.all { it }
    }

    /**
     * Comprehensive check: are all runtime AND special permissions granted?
     * Returns `true` when WakeForge is fully ready to schedule and ring alarms.
     */
    fun isFullyReady(context: Context): Boolean {
        return getMissingPermissions(context).isEmpty() &&
            context.isExactAlarmPermissionGranted() &&
            context.isBatteryOptimizationIgnored() &&
            context.isNotificationPermissionGranted()
    }

    // ────────────────────────────────────────────────────────────────────────
    // Intent Builders
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Creates an [Intent] to open the system's Alarms & Reminders settings page
     * where the user can grant SCHEDULE_EXACT_ALARM (Android 12+).
     */
    fun createAlarmPermissionIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${com.wakeforge.app.core.constants.AppConstants.PACKAGE_NAME}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an [Intent] to open the system's Battery Optimization settings
     * for the given [packageName] so the user can whitelist it.
     */
    fun createBatteryOptimizationIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an [Intent] to open the app's system settings page
     * (useful when the user needs to manually grant a permission after
     * selecting "Don't ask again").
     */
    fun createAppSettingsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an [Intent] to open notification settings for the app
     * (useful when POST_NOTIFICATIONS was permanently denied).
     */
    fun createNotificationSettingsIntent(packageName: String): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            createAppSettingsIntent(packageName)
        }
    }
}
