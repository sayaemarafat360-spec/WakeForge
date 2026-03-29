package com.wakeforge.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.usecases.settings.GetSettingsUseCase
import com.wakeforge.app.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the splash screen.
 *
 * Checks whether onboarding has been completed and routes the user
 * to the appropriate destination after a brief delay.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
) : ViewModel() {

    /**
     * Sealed interface representing the possible UI states of the splash screen.
     */
    sealed interface SplashUiState {
        /** The app is loading / checking settings. */
        data object Loading : SplashUiState

        /** Navigation should proceed to [destination]. */
        data class Navigate(val destination: String) : SplashUiState
    }

    private val _state = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val state: StateFlow<SplashUiState> = _state

    init {
        viewModelScope.launch {
            // Wait 2 seconds for the splash animation to play
            delay(2000L)

            val settings = getSettingsUseCase().first()

            val destination = if (settings.onboardingCompleted) {
                Routes.HOME
            } else {
                Routes.ONBOARDING
            }

            _state.value = SplashUiState.Navigate(destination)
        }
    }
}
