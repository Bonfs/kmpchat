package com.bonfs.tcc.kmpchat.core.mvi

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

abstract class BaseViewModel<State : UiState, Intent : UiIntent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    private val internalUiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = internalUiState.asStateFlow()

    private val effectChannel: Channel<Effect> = Channel(capacity = Channel.BUFFERED)
    val effect: Flow<Effect> = effectChannel.receiveAsFlow()

    protected val currentState: State
        get() = internalUiState.value

    abstract fun sendIntent(intent: Intent)

    protected fun updateState(reducer: (State) -> State) {
        internalUiState.update(reducer)
    }

    protected fun emitEffect(effect: Effect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}
