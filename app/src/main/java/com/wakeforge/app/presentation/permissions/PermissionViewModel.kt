package com.wakeforge.app.presentation.permissions

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.core.extensions.isBatteryOptimizationIgnored
import com.wakeforge.app.core.extensions.isExactAlarmPermissionGranted
import com.wakeforge.app.core.extensions.isNotificationPermissionGranted
import com.wakeforge.app.core.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the permission setup screen.
 *
 * Tracks the grant status of all required permissions and provides
 * methods for checking and requesting each permission individually.
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val context: Application,
) : ViewModel() {

    /**
     * Represents a single permission item displayed in the UI.
     */
    data class PermissionItem(
        val name: String,
        val description: String,
        val isGranted: Boolean,
        val permission: String,
        val isSpecial: Boolean = false,
    )

    data class PermissionUiState(
        val permissions: List<PermissionItem> = emptyList(),
        val allGranted: Boolean = false,
    )

    private val _state = MutableStateFlow(PermissionUiState())
    val state: StateFlow<PermissionUiState> = _state.asStateFlow()

    init {
        refreshState()
    }

    /**
     * Checks all permission statuses and updates the UI state.
     */
    fun checkPermissions() {
        refreshState()
    }

    /**
     * Refreshes the permission state by querying the system for each permission.
     */
    fun refreshState() {
        val items = mutableListOf<PermissionItem>()

        // 1. Notification permission
        val notificationGranted = context.isNotificationPermissionGranted()
        items.add(
            PermissionItem(
                name = "Notifications",
                description = "Show alarm reminders and upcoming alerts",
                isGranted = notificationGranted,
                permission = android.Manifest.permission.POST_NOTIFICATIONS,
                isSpecial = false,
            ),
        )

        // 2. Exact alarm permission (special permission)
        val exactAlarmGranted = context.isExactAlarmPermissionGranted()
        items.add(
            PermissionItem(
                name = "Exact Alarms",
                description = "Schedule alarms to ring at the exact time",
                isGranted = exactAlarmGranted,
                permission = "android.permission.SCHEDULE_EXACT_ALARM",
                isSpecial = true,
            ),
        )

        // 3. Battery optimization exemption (special permission)
        val batteryGranted = context.isBatteryOptimizationIgnored()
        items.add(
            PermissionItem(
                name = "Battery Optimization",
                description = "Ensure alarms ring reliably without battery restrictions",
                isGranted = batteryGranted,
                permission = "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
                isSpecial = true,
            ),
        )

        // 4. Activity recognition (for step missions)
        val activityRecognitionGranted = context.checkSelfPermission(
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        items.add(
            PermissionItem(
                name = "Activity Recognition",
                description = "Count steps for Step Challenge missions",
                isGranted = activityRecognitionGranted,
                permission = android.Manifest.permission.ACTIVITY_RECOGNITION,
                isSpecial = false,
            ),
        )

        // Determine if all critical permissions are granted
        // Critical = notifications + exact alarms + battery optimization
        val allGranted = items.count { it.isGranted } >= 3 // At least 3 of 4

        _state.value = PermissionUiState(
            permissions = items,
            allGranted = allGranted,
        )
    }

    /**
     * Requests a specific permission.
     *
     * For special permissions, this opens the system settings.
     * For runtime permissions, the Activity should use `rememberLauncherForActivityResult`.
     *
     * @param permission The permission string to request.
     * @param activity The current Activity for launching intents.
     */
    fun requestPermission(permission: String, activity: Activity) {
        when (permission) {
            "android.permission.SCHEDULE_EXACT_ALARM" -> {
                val intent = PermissionUtils.createAlarmPermissionIntent()
                activity.startActivity(intent)
            }
            "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" -> {
                val packageName = activity.packageName
                val intent = PermissionUtils.createBatteryOptimizationIntent(packageName)
                activity.startActivity(intent)
            }
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACTIVITY_RECOGNITION -> {
                // These need to be requested via the Activity result launcher
                // The UI layer handles this; we just refresh after return
            }
        }

        // Schedule a delayed refresh to pick up permission changes
        viewModelScope.launch {
            kotlinx.coroutines.delay(500L)
            refreshState()
        }
    }
}
