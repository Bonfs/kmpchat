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
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.model.UnknownEventMessage
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository

private class FakeObserveChatRepository : ChatRepository {
    val timelineFlow: MutableStateFlow<List<EventMessage>> = MutableStateFlow(
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
            ),
            UnknownEventMessage(
                id = "event_unknown_456",
                roomId = "!room1:matrix.org",
                senderId = "@alice:matrix.org",
                senderName = "alice",
                rawType = "RoomMemberEventContent",
                timestamp = 1000L,
                isOutgoing = false,
                isEncrypted = false,
                deliveryStatus = MessageDeliveryStatus.SENT
            )
        )
    )

    override fun observeTimeline(roomId: String): Flow<List<EventMessage>> = timelineFlow
    override suspend fun sendMessage(roomId: String, content: String) {}
}

class ObserveTimelineUseCaseTest {

    private val fakeChatRepo: FakeObserveChatRepository = FakeObserveChatRepository()
    private val useCase: ObserveTimelineUseCase = ObserveTimelineUseCase(fakeChatRepo)

    @Test
    fun shouldThrowExceptionWhenRoomIdIsBlank() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase("")
        }
    }

    @Test
    fun shouldObserveMessagesFromTimeline() = runTest {
        val events: List<EventMessage> = useCase("!room1:matrix.org").first()
        assertEquals(2, events.size)
        val chatMessage = events.filterIsInstance<ChatMessage>().first()
        assertEquals("Hello from Matrix!", chatMessage.content)
        assertEquals("@alice:matrix.org", chatMessage.senderId)
        val unknownMessage = events.filterIsInstance<UnknownEventMessage>().first()
        assertEquals("RoomMemberEventContent", unknownMessage.rawType)
    }
}
