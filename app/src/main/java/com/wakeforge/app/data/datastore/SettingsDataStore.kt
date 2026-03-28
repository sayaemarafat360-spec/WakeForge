package com.wakeforge.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.BooleanPreferencesKey
import androidx.datastore.preferences.core.FloatPreferencesKey
import androidx.datastore.preferences.core.IntPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.StringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.wakeforgeSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "wakeforge_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val THEME_MODE = StringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = BooleanPreferencesKey("onboarding_completed")
        val DEFAULT_SNOOZE_INTERVAL = IntPreferencesKey("default_snooze_interval")
        val DEFAULT_MAX_SNOOZE_COUNT = IntPreferencesKey("default_max_snooze_count")
        val DEFAULT_MISSION_TYPE = StringPreferencesKey("default_mission_type")
        val DEFAULT_DIFFICULTY = StringPreferencesKey("default_difficulty")
        val STRICT_MODE_DEFAULT = BooleanPreferencesKey("strict_mode_default")
        val SOUND_VOLUME = FloatPreferencesKey("sound_volume")
        val VIBRATION_INTENSITY = IntPreferencesKey("vibration_intensity")
        val NOTIFICATION_PERMISSION_REQUESTED = BooleanPreferencesKey("notification_permission_requested")
        val BATTERY_OPTIMIZATION_REQUESTED = BooleanPreferencesKey("battery_optimization_requested")
        val IS_24_HOUR_FORMAT = BooleanPreferencesKey("is_24_hour_format")
        val SELECTED_THEME_PALETTE = StringPreferencesKey("selected_theme_palette")
    }

    private val dataStore: DataStore<Preferences> = context.wakeforgeSettingsDataStore

    fun getSettingsFlow(): Flow<AppSettings> {
        return dataStore.data
            .catch { handleException(it) }
            .map { prefs ->
                AppSettings(
                    themeMode = parseThemeMode(prefs[Keys.THEME_MODE]),
                    onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                    defaultSnoozeInterval = prefs[Keys.DEFAULT_SNOOZE_INTERVAL] ?: 5,
                    defaultMaxSnoozeCount = prefs[Keys.DEFAULT_MAX_SNOOZE_COUNT] ?: 3,
                    defaultMissionType = parseMissionType(prefs[Keys.DEFAULT_MISSION_TYPE]),
                    defaultDifficulty = parseMissionDifficulty(prefs[Keys.DEFAULT_DIFFICULTY]),
                    strictModeDefault = prefs[Keys.STRICT_MODE_DEFAULT] ?: false,
                    soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.8f,
                    vibrationIntensity = prefs[Keys.VIBRATION_INTENSITY] ?: 50,
                    notificationPermissionRequested = prefs[Keys.NOTIFICATION_PERMISSION_REQUESTED] ?: false,
                    batteryOptimizationRequested = prefs[Keys.BATTERY_OPTIMIZATION_REQUESTED] ?: false,
                    is24HourFormat = prefs[Keys.IS_24_HOUR_FORMAT] ?: false,
                    selectedThemePalette = prefs[Keys.SELECTED_THEME_PALETTE] ?: "default"
                )
            }
    }

    suspend fun updateThemeMode(mode: AppSettings.ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateDefaultSnoozeInterval(interval: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_SNOOZE_INTERVAL] = interval
        }
    }

    suspend fun updateDefaultMaxSnoozeCount(count: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_MAX_SNOOZE_COUNT] = count
        }
    }

    suspend fun updateDefaultMissionType(type: MissionType) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_MISSION_TYPE] = type.name
        }
    }

    suspend fun updateDefaultDifficulty(difficulty: MissionDifficulty) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_DIFFICULTY] = difficulty.name
        }
    }

    suspend fun updateStrictModeDefault(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.STRICT_MODE_DEFAULT] = enabled
        }
    }

    suspend fun updateSoundVolume(volume: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.SOUND_VOLUME] = volume
        }
    }

    suspend fun updateVibrationIntensity(intensity: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.VIBRATION_INTENSITY] = intensity
        }
    }

    suspend fun updateNotificationPermissionRequested(requested: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_PERMISSION_REQUESTED] = requested
        }
    }

    suspend fun updateBatteryOptimizationRequested(requested: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.BATTERY_OPTIMIZATION_REQUESTED] = requested
        }
    }

    suspend fun update24HourFormat(is24Hour: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_24_HOUR_FORMAT] = is24Hour
        }
    }

    suspend fun updateSelectedThemePalette(paletteName: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SELECTED_THEME_PALETTE] = paletteName
        }
    }

    private fun parseThemeMode(value: String?): AppSettings.ThemeMode {
        if (value.isNullOrBlank()) return AppSettings.ThemeMode.SYSTEM
        return try {
            AppSettings.ThemeMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AppSettings.ThemeMode.SYSTEM
        }
    }

    private fun parseMissionType(value: String?): MissionType {
        if (value.isNullOrBlank()) return MissionType.MATH
        return try {
            MissionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MissionType.MATH
        }
    }

    private fun parseMissionDifficulty(value: String?): MissionDifficulty {
        if (value.isNullOrBlank()) return MissionDifficulty.MEDIUM
        return try {
            MissionDifficulty.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MissionDifficulty.MEDIUM
        }
    }

    private fun handleException(throwable: Throwable): Preferences {
        if (throwable is IOException) {
            return emptyPreferences()
        }
        throw throwable
    }
}
