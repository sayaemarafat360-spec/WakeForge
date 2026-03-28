package com.wakeforge.app.domain.repositories

import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for reading and updating application settings.
 */
interface SettingsRepository {

    /**
     * Observes the current application settings reactively.
     *
     * @return A [Flow] emitting the current [AppSettings] whenever they change.
     */
    fun getSettings(): Flow<AppSettings>

    /**
     * Updates the UI theme mode.
     *
     * @param mode The new [AppSettings.ThemeMode] to apply.
     */
    suspend fun updateThemeMode(mode: AppSettings.ThemeMode)

    /**
     * Updates the default snooze configuration for new alarms.
     *
     * @param interval Minutes between snoozes (1–30).
     * @param maxCount Maximum number of snoozes allowed (0–10).
     */
    suspend fun updateDefaultSnooze(interval: Int, maxCount: Int)

    /**
     * Updates the default mission type and difficulty for new alarms.
     *
     * @param type The default [MissionType].
     * @param difficulty The default [MissionDifficulty].
     */
    suspend fun updateDefaultMission(type: MissionType, difficulty: MissionDifficulty)

    /**
     * Updates whether strict mode is enabled by default for new alarms.
     *
     * @param enabled true to enable strict mode by default.
     */
    suspend fun updateStrictModeDefault(enabled: Boolean)

    /**
     * Updates the alarm sound volume.
     *
     * @param volume Volume level between 0.0 and 1.0.
     */
    suspend fun updateSoundVolume(volume: Float)

    /**
     * Updates the vibration intensity.
     *
     * @param intensity Vibration intensity level between 0 and 100.
     */
    suspend fun updateVibrationIntensity(intensity: Int)

    /**
     * Marks the onboarding flow as completed.
     */
    suspend fun markOnboardingCompleted()

    /**
     * Checks whether the user has completed the onboarding flow.
     *
     * @return true if onboarding is complete, false otherwise.
     */
    suspend fun isOnboardingCompleted(): Boolean

    /**
     * Updates the selected color theme palette.
     *
     * @param paletteName The name of the theme palette to apply.
     */
    suspend fun updateThemePalette(paletteName: String)

    /**
     * Updates whether times should be displayed in 24-hour format.
     *
     * @param is24Hour true for 24-hour format, false for 12-hour.
     */
    suspend fun update24HourFormat(is24Hour: Boolean)

    /**
     * Marks that the notification permission has been requested.
     */
    suspend fun markNotificationPermissionRequested()

    /**
     * Marks that the battery optimization exemption has been requested.
     */
    suspend fun markBatteryOptimizationRequested()
}
