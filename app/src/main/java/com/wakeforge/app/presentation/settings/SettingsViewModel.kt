package com.wakeforge.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.data.premium.PremiumManager
import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.usecases.premium.CheckPremiumStatusUseCase
import com.wakeforge.app.domain.usecases.settings.GetSettingsUseCase
import com.wakeforge.app.domain.usecases.settings.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val premiumManager: PremiumManager
) : ViewModel() {

    data class SettingsUiState(
        val themeMode: AppSettings.ThemeMode = AppSettings.ThemeMode.SYSTEM,
        val defaultSnoozeInterval: Int = 5,
        val defaultMaxSnoozeCount: Int = 3,
        val defaultMissionType: MissionType = MissionType.MATH,
        val defaultDifficulty: MissionDifficulty = MissionDifficulty.MEDIUM,
        val strictModeDefault: Boolean = false,
        val soundVolume: Float = 0.8f,
        val vibrationIntensity: Int = 50,
        val isPremium: Boolean = false,
        val appVersion: String = "1.0.0"
    )

    sealed class SettingsEvent {
        data object NavigateToPremium : SettingsEvent()
        data object ShowAbout : SettingsEvent()
        data object ResetSettings : SettingsEvent()
        data object ClearAllData : SettingsEvent()
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                getSettingsUseCase(),
                premiumManager.isPremium()
            ) { settings, isPremium ->
                Pair(settings, isPremium)
            }.collect { (settings, isPremium) ->
                _uiState.value = SettingsUiState(
                    themeMode = settings.themeMode,
                    defaultSnoozeInterval = settings.defaultSnoozeInterval,
                    defaultMaxSnoozeCount = settings.defaultMaxSnoozeCount,
                    defaultMissionType = settings.defaultMissionType,
                    defaultDifficulty = settings.defaultDifficulty,
                    strictModeDefault = settings.strictModeDefault,
                    soundVolume = settings.soundVolume,
                    vibrationIntensity = settings.vibrationIntensity,
                    isPremium = isPremium,
                    appVersion = "1.0.0"
                )
            }
        }
    }

    fun updateThemeMode(mode: AppSettings.ThemeMode) {
        viewModelScope.launch {
            updateSettingsUseCase.updateThemeMode(mode)
        }
    }

    fun updateSnoozeDefaults(interval: Int, maxCount: Int) {
        viewModelScope.launch {
            updateSettingsUseCase.updateDefaultSnooze(interval, maxCount)
        }
    }

    fun updateMissionDefaults(type: MissionType, difficulty: MissionDifficulty) {
        viewModelScope.launch {
            updateSettingsUseCase.updateDefaultMission(type, difficulty)
        }
    }

    fun updateStrictModeDefault(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.updateStrictModeDefault(enabled)
        }
    }

    fun updateSoundVolume(volume: Float) {
        viewModelScope.launch {
            updateSettingsUseCase.updateSoundVolume(volume)
        }
    }

    fun updateVibrationIntensity(intensity: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(vibrationIntensity = intensity)
            try { updateSettingsUseCase.updateVibrationIntensity(intensity) } catch (_: Exception) {}
        }
    }

    fun navigateToPremium() {
        viewModelScope.launch {
            _events.emit(SettingsEvent.NavigateToPremium)
        }
    }

    fun showAbout() {
        viewModelScope.launch {
            _events.emit(SettingsEvent.ShowAbout)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            _events.emit(SettingsEvent.ResetSettings)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _events.emit(SettingsEvent.ClearAllData)
        }
    }
}
