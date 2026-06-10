package com.baruckis.ainews.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base for MVI ViewModels: holds a single [UiState], accepts [Intent]s through one entry
 * point and emits one-time [Effect]s on a separate stream so they never replay from state.
 *
 * @param S the screen's [UiState]
 * @param I the screen's [Intent] hierarchy
 * @param E the screen's one-time [Effect] hierarchy
 * @param initialState state published before any intent is processed
 */
abstract class MviViewModel<S : UiState, I : Intent, E : Effect>(
    initialState: S,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)

    /** Current UI state; collect to render. */
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)

    /** One-time effects; each emission is delivered to a single collector exactly once. */
    val effects: Flow<E> = _effects.receiveAsFlow()

    /** The latest published state, for reading inside intent handlers. */
    protected val currentState: S get() = _state.value

    /** Atomically replaces the state with the result of [reduce] applied to the current state. */
    protected fun setState(reduce: S.() -> S) = _state.update(reduce)

    /** Queues [effect] for one-time delivery on [effects]. */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }

    /** Single entry point: the UI funnels every user/system action through here. */
    abstract fun onIntent(intent: I)
}
