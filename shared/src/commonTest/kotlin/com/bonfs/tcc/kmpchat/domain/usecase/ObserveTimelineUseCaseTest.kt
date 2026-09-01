package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository

private class FakeObserveChatRepository : ChatRepository {
    val timelineFlow = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "event_123",
                roomId = "!room1:matrix.org",
                senderId = "@alice:matrix.org",
                senderName = "alice",
                content = "Hello from Matrix!",
                timestamp = 2000L,
                isOutgoing = false,
                isEncrypted = true,
                deliveryStatus = MessageDeliveryStatus.SENT
            )
        )
    )

    override fun observeTimeline(roomId: String): Flow<List<ChatMessage>> = timelineFlow
    override suspend fun sendMessage(roomId: String, content: String) {}
}

class ObserveTimelineUseCaseTest {

    private val fakeChatRepo = FakeObserveChatRepository()
    private val useCase = ObserveTimelineUseCase(fakeChatRepo)

    @Test
    fun shouldThrowExceptionWhenRoomIdIsBlank() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase("")
        }
    }

    @Test
    fun shouldObserveMessagesFromTimeline() = runTest {
        val messages = useCase("!room1:matrix.org").first()
        assertEquals(1, messages.size)
        assertEquals("Hello from Matrix!", messages.first().content)
        assertEquals("@alice:matrix.org", messages.first().senderId)
    }
}
