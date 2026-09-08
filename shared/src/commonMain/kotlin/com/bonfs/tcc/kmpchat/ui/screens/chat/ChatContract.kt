package com.bonfs.tcc.kmpchat.ui.screens.chat

import com.bonfs.tcc.kmpchat.core.mvi.UiEffect
import com.bonfs.tcc.kmpchat.core.mvi.UiIntent
import com.bonfs.tcc.kmpchat.core.mvi.UiState
import com.bonfs.tcc.kmpchat.domain.model.EventMessage

data class ChatUiState(
    val roomId: String = "",
    val roomName: String = "",
    val messages: List<EventMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isEncrypted: Boolean = true,
    val errorMessage: String? = null
) : UiState

sealed interface ChatUiIntent : UiIntent {
    data class LoadTimeline(val roomId: String, val roomName: String) : ChatUiIntent
    data class InputTextChanged(val text: String) : ChatUiIntent
    data object SendMessage : ChatUiIntent
    data class RetryMessage(val content: String) : ChatUiIntent
    data object NavigateBack : ChatUiIntent
}

sealed interface ChatUiEffect : UiEffect {
    data object NavigateBack : ChatUiEffect
    data object ScrollToBottom : ChatUiEffect
    data class ShowSnackbar(val message: String) : ChatUiEffect
}
