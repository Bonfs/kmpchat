package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

private class FakeObserveRoomRepository : RoomRepository {
    val roomsFlow = MutableStateFlow(
        listOf(
            ChatRoom(
                id = "!room1:matrix.org",
                name = "General",
                lastMessage = "Hello",
                lastEventTimestamp = 1000L,
                unreadCount = 2,
                isDirect = false,
                isEncrypted = true,
                membership = RoomMembership.JOIN
            )
        )
    )

    override fun observeRooms(): Flow<List<ChatRoom>> = roomsFlow
    override fun observeRoomById(roomId: String): Flow<ChatRoom?> = MutableStateFlow(null)
    override suspend fun createDirectRoom(targetUserId: String): String = ""
    override suspend fun joinRoom(roomId: String) {}
    override suspend fun leaveRoom(roomId: String) {}
}

class ObserveRoomsUseCaseTest {

    @Test
    fun shouldObserveListOfRooms() = runTest {
        val fakeRepo = FakeObserveRoomRepository()
        val useCase = ObserveRoomsUseCase(fakeRepo)

        val rooms = useCase().first()
        assertEquals(1, rooms.size)
        assertEquals("!room1:matrix.org", rooms.first().id)
        assertEquals("General", rooms.first().name)
        assertEquals(2, rooms.first().unreadCount)
    }
}
