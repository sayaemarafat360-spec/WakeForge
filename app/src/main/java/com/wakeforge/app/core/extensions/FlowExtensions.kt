package com.wakeforge.app.core.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────────────────────
// Flow → StateFlow (ViewModel scope)
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Converts a cold [Flow] into a hot [StateFlow] inside a ViewModel's [scope].
 *
 * The upstream flow is kept active as long as there is at least one subscriber
 * (with a 5-second keep-alive timeout so it doesn't restart on fast config changes).
 *
 * ```kotlin
 * val alarms: StateFlow<List<Alarm>> = alarmRepository.getAllAlarms()
 *     .asStateFlowInViewModel(viewModelScope, emptyList())
 * ```
 *
 * @param scope         Typically `viewModelScope`.
 * @param initialValue  The value emitted while the upstream has not yet emitted.
 */
fun <T> Flow<T>.asStateFlowInViewModel(
    scope: CoroutineScope,
    initialValue: T,
): StateFlow<T> = stateIn(
    scope = scope,
    started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
    initialValue = initialValue,
)

// ──────────────────────────────────────────────────────────────────────────────
// Flow → collectAsState (lifecycle-aware)
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Collects a [Flow] as Compose [State] in a lifecycle-aware manner.
 * Collection starts in `STARTED` and stops in `STOPPED`, preventing
 * unnecessary work when the app is in the background.
 *
 * ```kotlin
 * val alarms by alarmRepository.getAllAlarms()
 *     .collectAsStateWithLifecycle(initialValue = emptyList())
 * ```
 *
 * @param initialValue The value used before the first emission.
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(initialValue: T): androidx.compose.runtime.State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { androidx.compose.runtime.mutableStateOf(initialValue) }

    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                this@collectAsStateWithLifecycle.collect { value ->
                    state.value = value
                }
            }
        }
    }

    return state
}

// ──────────────────────────────────────────────────────────────────────────────
// SharedFlow → Event observer
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Observes a [SharedFlow] of one-shot events inside a [CoroutineScope].
 * Events are collected for the lifetime of the scope.
 *
 * ```kotlin
 * viewModel.events.observeEvents(viewModelScope) { event ->
 *     when (event) { ... }
 * }
 * ```
 *
 * @param scope   Typically `viewModelScope` or `lifecycleScope`.
 * @param onEvent Callback invoked for every emission.
 */
fun <Event> SharedFlow<Event>.observeEvents(
    scope: CoroutineScope,
    onEvent: (Event) -> Unit,
) {
    scope.launch {
        this@observeEvents.collect { event ->
            onEvent(event)
        }
    }
}

/**
 * Observes a [SharedFlow] of one-shot events in a lifecycle-aware way from
 * a composable context. Collection is tied to the `STARTED` lifecycle state.
 */
@Composable
fun <Event> SharedFlow<Event>.observeEventsWithLifecycle(
    onEvent: (Event) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            this@observeEventsWithLifecycle.collect { event ->
                onEvent(event)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// StateFlow helpers
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns the current value of a [StateFlow], or `null` if this is not
 * actually a StateFlow (defensive — always returns `.value` for real instances).
 *
 * This is mainly a readability alias: `flow.getOrNull()` vs `flow.value`.
 */
fun <T> StateFlow<T>.getOrNull(): T? = value
