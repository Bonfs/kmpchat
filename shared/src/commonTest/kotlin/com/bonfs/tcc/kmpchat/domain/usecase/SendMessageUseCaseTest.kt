package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository

private class FakeChatRepository : ChatRepository {
    var sentRoomId: String? = null
    var sentContent: String? = null

    override fun observeTimeline(roomId: String): Flow<List<EventMessage>> = MutableStateFlow(emptyList())

    override suspend fun sendMessage(roomId: String, content: String) {
        sentRoomId = roomId
        sentContent = content
    }
}

class SendMessageUseCaseTest {

    private val fakeChatRepo = FakeChatRepository()
    private val useCase = SendMessageUseCase(fakeChatRepo)

    @Test
    fun shouldThrowExceptionWhenRoomIdIsBlank() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase("", "Hello Matrix")
        }
    }

    @Test
    fun shouldThrowExceptionWhenContentIsBlank() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase("!room:matrix.org", "   ")
        }
    }

    @Test
    fun shouldSendMessageSuccessfully() = runTest {
        useCase("!room:matrix.org", "Hello Matrix E2EE")
        assertEquals("!room:matrix.org", fakeChatRepo.sentRoomId)
        assertEquals("Hello Matrix E2EE", fakeChatRepo.sentContent)
    }
}
