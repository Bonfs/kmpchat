package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

private class FakeInviteRoomRepository : RoomRepository {
    var acceptedRoomId: String? = null
    var rejectedRoomId: String? = null

    override fun observeRooms(): Flow<List<ChatRoom>> = MutableStateFlow(emptyList())
    override fun observeRoomById(roomId: String): Flow<ChatRoom?> = MutableStateFlow(null)
    override suspend fun createDirectRoom(targetUserId: String): String = ""

    override suspend fun joinRoom(roomId: String) {
        acceptedRoomId = roomId
    }

    override suspend fun leaveRoom(roomId: String) {
        rejectedRoomId = roomId
    }
}

class HandleInviteUseCaseTest {

    private val fakeRepo = FakeInviteRoomRepository()
    private val useCase = HandleInviteUseCase(fakeRepo)

    @Test
    fun shouldThrowExceptionWhenRoomIdIsBlankOnAccept() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase.accept("")
        }
    }

    @Test
    fun shouldThrowExceptionWhenRoomIdIsBlankOnReject() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase.reject("")
        }
    }

    @Test
    fun shouldAcceptInviteSuccessfully() = runTest {
        useCase.accept("!room1:matrix.org")
        assertEquals("!room1:matrix.org", fakeRepo.acceptedRoomId)
    }

    @Test
    fun shouldRejectInviteSuccessfully() = runTest {
        useCase.reject("!room1:matrix.org")
        assertEquals("!room1:matrix.org", fakeRepo.rejectedRoomId)
    }
}
