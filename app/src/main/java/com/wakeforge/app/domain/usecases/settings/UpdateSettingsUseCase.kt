package com.wakeforge.app.domain.usecases.settings

import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.repositories.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating individual application settings.
 *
 * Provides a unified entry point for updating any setting field with
 * built-in validation for each setting type.
 *
 * @property repository The [SettingsRepository] used for persisting setting changes.
 */
class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {

    /**
     * Updates the UI theme mode.
     *
     * @param mode The new [AppSettings.ThemeMode] to apply.
     */
    suspend fun updateThemeMode(mode: AppSettings.ThemeMode) {
        repository.updateThemeMode(mode)
    }

    /**
     * Updates the default snooze configuration for new alarms.
     *
     * @param interval Minutes between snoozes (1–30).
     * @param maxCount Maximum number of snoozes allowed (0–10).
     * @throws IllegalArgumentException if values are out of range.
     */
    suspend fun updateDefaultSnooze(interval: Int, maxCount: Int) {
        require(interval in 1..30) {
            "Snooze interval must be between 1 and 30 minutes, but was $interval."
        }
        require(maxCount in 0..10) {
            "Max snooze count must be between 0 and 10, but was $maxCount."
        }
        repository.updateDefaultSnooze(interval, maxCount)
    }

    /**
     * Updates the default mission type and difficulty for new alarms.
     *
     * @param type The default [MissionType].
     * @param difficulty The default [MissionDifficulty].
     */
    suspend fun updateDefaultMission(type: MissionType, difficulty: MissionDifficulty) {
        repository.updateDefaultMission(type, difficulty)
    }

    /**
     * Updates whether strict mode is enabled by default for new alarms.
     *
     * @param enabled true to enable strict mode by default.
     */
    suspend fun updateStrictModeDefault(enabled: Boolean) {
        repository.updateStrictModeDefault(enabled)
    }

    /**
     * Updates the alarm sound volume.
     *
     * @param volume Volume level between 0.0 and 1.0.
     * @throws IllegalArgumentException if volume is out of range.
     */
    suspend fun updateSoundVolume(volume: Float) {
        require(volume in 0.0f..1.0f) {
            "Sound volume must be between 0.0 and 1.0, but was $volume."
        }
        repository.updateSoundVolume(volume)
    }

    /**
     * Updates the vibration intensity.
     *
     * @param intensity Vibration intensity level between 0 and 100.
     * @throws IllegalArgumentException if intensity is out of range.
     */
    suspend fun updateVibrationIntensity(intensity: Int) {
        require(intensity in 0..100) {
            "Vibration intensity must be between 0 and 100, but was $intensity."
        }
        repository.updateVibrationIntensity(intensity)
    }

    /**
     * Marks the onboarding flow as completed.
     */
    suspend fun markOnboardingCompleted() {
        repository.markOnboardingCompleted()
    }

    /**
     * Updates the selected color theme palette.
     *
     * @param paletteName The name of the theme palette to apply. Must not be blank.
     * @throws IllegalArgumentException if the palette name is blank.
     */
    suspend fun updateThemePalette(paletteName: String) {
        require(paletteName.isNotBlank()) {
            "Palette name must not be blank."
        }
        repository.updateThemePalette(paletteName)
    }

    /**
     * Updates whether times should be displayed in 24-hour format.
     *
     * @param is24Hour true for 24-hour format, false for 12-hour.
     */
    suspend fun update24HourFormat(is24Hour: Boolean) {
        repository.update24HourFormat(is24Hour)
    }

    /**
     * Marks that the notification permission has been requested from the user.
     */
    suspend fun markNotificationPermissionRequested() {
        repository.markNotificationPermissionRequested()
    }

    /**
     * Marks that the battery optimization exemption has been requested from the user.
     */
    suspend fun markBatteryOptimizationRequested() {
        repository.markBatteryOptimizationRequested()
    }
}
