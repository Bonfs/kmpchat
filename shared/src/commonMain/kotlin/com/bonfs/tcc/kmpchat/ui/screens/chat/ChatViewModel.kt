package com.bonfs.tcc.kmpchat.ui.screens.chat

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.bonfs.tcc.kmpchat.core.mvi.BaseViewModel
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveTimelineUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.SendMessageUseCase

class ChatViewModel(
    private val observeTimelineUseCase: ObserveTimelineUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : BaseViewModel<ChatUiState, ChatUiIntent, ChatUiEffect>(ChatUiState()) {

    override fun sendIntent(intent: ChatUiIntent) {
        when (intent) {
            is ChatUiIntent.LoadTimeline -> onLoadTimeline(intent.roomId, intent.roomName)
            is ChatUiIntent.InputTextChanged -> onInputTextChanged(intent.text)
            is ChatUiIntent.SendMessage -> onSendMessage()
            is ChatUiIntent.RetryMessage -> onRetryMessage(intent.content)
            is ChatUiIntent.NavigateBack -> onNavigateBack()
        }
    }

    private fun onLoadTimeline(roomId: String, roomName: String) {
        updateState { it.copy(roomId = roomId, roomName = roomName, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            observeTimelineUseCase(roomId)
                .catch { handleTimelineError(it) }
                .collectLatest { messageList: List<EventMessage> ->
                    val sorted: List<EventMessage> = messageList.sortedBy { it.timestamp }
                    updateState { it.copy(messages = sorted, isLoading = false) }
                    emitEffect(ChatUiEffect.ScrollToBottom)
                }
        }
    }

    private fun onInputTextChanged(text: String) {
        updateState { it.copy(inputText = text) }
    }

    private fun onSendMessage() {
        val content = currentState.inputText.trim()
        val roomId = currentState.roomId
        if (content.isBlank() || roomId.isBlank() || currentState.isSending) return

        updateState { it.copy(inputText = "", isSending = true) }
        viewModelScope.launch {
            try {
                sendMessageUseCase(roomId, content)
                updateState { it.copy(isSending = false) }
                emitEffect(ChatUiEffect.ScrollToBottom)
            } catch (exception: Exception) {
                updateState { it.copy(isSending = false) }
                emitEffect(ChatUiEffect.ShowSnackbar("Erro ao enviar mensagem"))
            }
        }
    }

    private fun onRetryMessage(content: String) {
        updateState { it.copy(inputText = content) }
        onSendMessage()
    }

    private fun onNavigateBack() {
        emitEffect(ChatUiEffect.NavigateBack)
    }

    private fun handleTimelineError(throwable: Throwable) {
        updateState { it.copy(isLoading = false, errorMessage = "Erro ao carregar mensagens da sala") }
    }
}
