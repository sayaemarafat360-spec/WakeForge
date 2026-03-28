package com.wakeforge.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.domain.usecases.settings.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the onboarding flow.
 *
 * Manages the current page index, handles next/previous navigation,
 * and marks onboarding as completed when the user finishes or skips.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val updateSettingsUseCase: UpdateSettingsUseCase,
) : ViewModel() {

    data class OnboardingUiState(
        val currentPage: Int = 0,
        val totalPages: Int = 4,
    )

    /** Events emitted as one-shot signals to the UI layer. */
    sealed interface Event {
        data object Complete : Event
        data object Skip : Event
    }

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /**
     * Advances to the next page. If already on the last page, completes onboarding.
     */
    fun nextPage() {
        val current = _state.value.currentPage
        if (current < _state.value.totalPages - 1) {
            _state.value = _state.value.copy(currentPage = current + 1)
        } else {
            complete()
        }
    }

    /**
     * Returns to the previous page, clamped to 0.
     */
    fun previousPage() {
        val current = _state.value.currentPage
        if (current > 0) {
            _state.value = _state.value.copy(currentPage = current - 1)
        }
    }

    /**
     * Marks onboarding as completed and emits the [Event.Complete] event.
     */
    fun complete() {
        viewModelScope.launch {
            updateSettingsUseCase.markOnboardingCompleted()
            _events.emit(Event.Complete)
        }
    }

    /**
     * Marks onboarding as completed (same as complete) and emits the [Event.Skip] event.
     */
    fun skip() {
        viewModelScope.launch {
            updateSettingsUseCase.markOnboardingCompleted()
            _events.emit(Event.Skip)
        }
    }
}
