package com.bonfs.tcc.kmpchat.ui.screens.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveTimelineUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.SendMessageUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private class FakeChatRepo : ChatRepository {
        val timelineFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
        var lastSentMessage: Pair<String, String>? = null

        override fun observeTimeline(roomId: String): Flow<List<ChatMessage>> = timelineFlow

        override suspend fun sendMessage(roomId: String, content: String) {
            lastSentMessage = roomId to content
        }
    }

    @Test
    fun shouldObserveTimelineAndSortChronologically() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val chatRepo = FakeChatRepo()
        val msg1 = ChatMessage(id = "m1", roomId = "!r1", senderId = "@a:matrix.org", senderName = "A", content = "Oi", timestamp = 200L, isOutgoing = false, isEncrypted = true, deliveryStatus = MessageDeliveryStatus.RECEIVED)
        val msg2 = ChatMessage(id = "m2", roomId = "!r1", senderId = "@b:matrix.org", senderName = "B", content = "Tudo bem?", timestamp = 100L, isOutgoing = true, isEncrypted = true, deliveryStatus = MessageDeliveryStatus.SENT)
        chatRepo.timelineFlow.value = listOf(msg1, msg2)

        val viewModel = ChatViewModel(
            ObserveTimelineUseCase(chatRepo),
            SendMessageUseCase(chatRepo)
        )

        viewModel.sendIntent(ChatUiIntent.LoadTimeline("!r1", "Sala de Teste"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.messages.size)
        // Should be sorted by timestamp asc
        assertEquals("m2", viewModel.uiState.value.messages[0].id)
        assertEquals("m1", viewModel.uiState.value.messages[1].id)

        Dispatchers.resetMain()
    }

    @Test
    fun shouldSendMessageAndClearInput() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val chatRepo = FakeChatRepo()
        val viewModel = ChatViewModel(
            ObserveTimelineUseCase(chatRepo),
            SendMessageUseCase(chatRepo)
        )

        viewModel.sendIntent(ChatUiIntent.LoadTimeline("!r1", "Sala 1"))
        viewModel.sendIntent(ChatUiIntent.InputTextChanged("Mensagem de teste"))
        viewModel.sendIntent(ChatUiIntent.SendMessage)

        advanceUntilIdle()

        assertEquals("" to "", viewModel.uiState.value.inputText to "")
        assertEquals("!r1" to "Mensagem de teste", chatRepo.lastSentMessage)
        assertFalse(viewModel.uiState.value.isSending)

        Dispatchers.resetMain()
    }
}
