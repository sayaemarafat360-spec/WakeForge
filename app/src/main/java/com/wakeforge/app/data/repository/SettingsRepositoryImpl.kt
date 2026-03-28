package com.wakeforge.app.data.repository

import com.wakeforge.app.data.datastore.SettingsDataStore
import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return settingsDataStore.getSettingsFlow()
    }

    override suspend fun updateThemeMode(mode: AppSettings.ThemeMode) {
        settingsDataStore.updateThemeMode(mode)
    }

    override suspend fun updateDefaultSnooze(interval: Int, maxCount: Int) {
        settingsDataStore.updateDefaultSnoozeInterval(interval)
        settingsDataStore.updateDefaultMaxSnoozeCount(maxCount)
    }

    override suspend fun updateDefaultMission(type: MissionType, difficulty: MissionDifficulty) {
        settingsDataStore.updateDefaultMissionType(type)
        settingsDataStore.updateDefaultDifficulty(difficulty)
    }

    override suspend fun updateStrictModeDefault(enabled: Boolean) {
        settingsDataStore.updateStrictModeDefault(enabled)
    }

    override suspend fun updateSoundVolume(volume: Float) {
        settingsDataStore.updateSoundVolume(volume)
    }

    override suspend fun updateVibrationIntensity(intensity: Int) {
        settingsDataStore.updateVibrationIntensity(intensity)
    }

    override suspend fun markOnboardingCompleted() {
        settingsDataStore.updateOnboardingCompleted(true)
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return settingsDataStore.getSettingsFlow().first().onboardingCompleted
    }

    override suspend fun updateThemePalette(paletteName: String) {
        settingsDataStore.updateSelectedThemePalette(paletteName)
    }

    override suspend fun update24HourFormat(is24Hour: Boolean) {
        settingsDataStore.update24HourFormat(is24Hour)
    }

    override suspend fun markNotificationPermissionRequested() {
        settingsDataStore.updateNotificationPermissionRequested(true)
    }

    override suspend fun markBatteryOptimizationRequested() {
        settingsDataStore.updateBatteryOptimizationRequested(true)
    }
}
