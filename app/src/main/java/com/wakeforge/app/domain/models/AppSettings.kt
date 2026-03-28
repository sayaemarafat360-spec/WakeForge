package com.wakeforge.app.domain.models

/**
 * Application-wide settings configurable by the user.
 *
 * @property themeMode The current UI theme preference.
 * @property onboardingCompleted Whether the user has completed the first-run onboarding flow.
 * @property defaultSnoozeInterval Default minutes between snoozes for new alarms.
 * @property defaultMaxSnoozeCount Default maximum snoozes allowed for new alarms.
 * @property defaultMissionType Default mission type for new alarms.
 * @property defaultDifficulty Default difficulty level for new alarms.
 * @property strictModeDefault Whether strict mode is enabled by default for new alarms.
 * @property soundVolume Alarm sound volume level (0.0–1.0).
 * @property vibrationIntensity Vibration intensity level (0–100).
 * @property notificationPermissionRequested Whether the notification permission has been requested.
 * @property batteryOptimizationRequested Whether battery optimization exemption has been requested.
 * @property is24HourFormat Whether times should be displayed in 24-hour format.
 * @property selectedThemePalette Name of the currently selected color theme palette.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val onboardingCompleted: Boolean = false,
    val defaultSnoozeInterval: Int = 5,
    val defaultMaxSnoozeCount: Int = 3,
    val defaultMissionType: MissionType = MissionType.MATH,
    val defaultDifficulty: MissionDifficulty = MissionDifficulty.MEDIUM,
    val strictModeDefault: Boolean = false,
    val soundVolume: Float = 0.8f,
    val vibrationIntensity: Int = 50,
    val notificationPermissionRequested: Boolean = false,
    val batteryOptimizationRequested: Boolean = false,
    val is24HourFormat: Boolean = false,
    val selectedThemePalette: String = "default"
) {

    /**
     * Enum representing available UI theme modes.
     */
    enum class ThemeMode {
        /** Always use dark theme. */
        DARK,

        /** Always use light theme. */
        LIGHT,

        /** Follow the system-wide theme setting. */
        SYSTEM
    }

    /**
     * Returns a new [AppSettings] with strict mode default toggled.
     */
    fun toggleStrictMode(): AppSettings = copy(strictModeDefault = !strictModeDefault)

    /**
     * Returns a new [AppSettings] with the onboarding flag set to true.
     */
    fun completeOnboarding(): AppSettings = copy(onboardingCompleted = true)

    /**
     * Validates that all settings values are within acceptable ranges.
     *
     * @return true if all settings are valid, false otherwise.
     */
    fun isValid(): Boolean {
        return soundVolume in 0.0f..1.0f &&
                vibrationIntensity in 0..100 &&
                defaultSnoozeInterval in 1..30 &&
                defaultMaxSnoozeCount in 0..10
    }
}
