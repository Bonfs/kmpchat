package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.InvalidMatrixIdException
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

private class FakeRoomRepository : RoomRepository {
    var createdRoomId = "!dm_123:matrix.org"
    var joinedRoomId: String? = null
    var leftRoomId: String? = null

    override fun observeRooms(): Flow<List<ChatRoom>> = MutableStateFlow(emptyList())
    override fun observeRoomById(roomId: String): Flow<ChatRoom?> = MutableStateFlow(null)

    override suspend fun createDirectRoom(targetUserId: String): String {
        return createdRoomId
    }

    override suspend fun joinRoom(roomId: String) {
        joinedRoomId = roomId
    }

    override suspend fun leaveRoom(roomId: String) {
        leftRoomId = roomId
    }
}

class CreateDirectRoomUseCaseTest {

    private val fakeRepo = FakeRoomRepository()
    private val useCase = CreateDirectRoomUseCase(fakeRepo)

    @Test
    fun shouldThrowExceptionWhenTargetUserIdIsEmpty() = runTest {
        assertFailsWith<EmptyFieldException> {
            useCase("")
        }
    }

    @Test
    fun shouldThrowExceptionWhenTargetUserIdIsInvalid() = runTest {
        assertFailsWith<InvalidMatrixIdException> {
            useCase("invalid_user_id")
        }
        assertFailsWith<InvalidMatrixIdException> {
            useCase("@alice")
        }
    }

    @Test
    fun shouldCreateDirectRoomWhenTargetUserIdIsValid() = runTest {
        val roomId = useCase("@bob:matrix.org")
        assertEquals("!dm_123:matrix.org", roomId)
    }
}
