package com.bonfs.tcc.kmpchat.ui.screens.roomlist

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository
import com.bonfs.tcc.kmpchat.domain.usecase.CreateDirectRoomUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.HandleInviteUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.LogoutUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveRoomsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class RoomListViewModelTest {

    private class FakeRoomRepo : RoomRepository {
        val roomsFlow = MutableStateFlow<List<ChatRoom>>(emptyList())
        var directRoomCreatedTarget: String? = null
        var acceptedRoomId: String? = null
        var rejectedRoomId: String? = null

        override fun observeRooms(): Flow<List<ChatRoom>> = roomsFlow
        override fun observeRoomById(roomId: String): Flow<ChatRoom?> = flowOf(null)
        override suspend fun createDirectRoom(targetUserId: String): String {
            directRoomCreatedTarget = targetUserId
            return "!created_dm:matrix.org"
        }
        override suspend fun joinRoom(roomId: String) { acceptedRoomId = roomId }
        override suspend fun leaveRoom(roomId: String) { rejectedRoomId = roomId }
    }

    private class FakeAuthRepo : AuthRepository {
        var logoutCalled = false
        override fun observeSession(): Flow<UserSession?> = flowOf(null)
        override suspend fun getCurrentSession(): UserSession? = null
        override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.STOPPED)
        override suspend fun login(credentials: LoginCredentials): UserSession = throw UnsupportedOperationException()
        override suspend fun logout() { logoutCalled = true }
    }

    @Test
    fun shouldPartitionRoomsAndInvitesCorrectly() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val roomRepo = FakeRoomRepo()
        val authRepo = FakeAuthRepo()

        val joinedRoom = ChatRoom(id = "!r1", name = "Sala 1", lastMessage = null, lastEventTimestamp = 0L, unreadCount = 0, isDirect = false, isEncrypted = true, membership = RoomMembership.JOIN)
        val invitedRoom = ChatRoom(id = "!r2", name = "Convite 1", lastMessage = null, lastEventTimestamp = 0L, unreadCount = 0, isDirect = true, isEncrypted = true, membership = RoomMembership.INVITE)
        roomRepo.roomsFlow.value = listOf(joinedRoom, invitedRoom)

        val viewModel = RoomListViewModel(
            ObserveRoomsUseCase(roomRepo),
            CreateDirectRoomUseCase(roomRepo),
            HandleInviteUseCase(roomRepo),
            LogoutUseCase(authRepo)
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.rooms.size)
        assertEquals("!r1", viewModel.uiState.value.rooms.first().id)
        assertEquals(1, viewModel.uiState.value.invites.size)
        assertEquals("!r2", viewModel.uiState.value.invites.first().id)

        Dispatchers.resetMain()
    }

    @Test
    fun shouldCreateDirectRoomAndDismissDialog() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val roomRepo = FakeRoomRepo()
        val authRepo = FakeAuthRepo()

        val viewModel = RoomListViewModel(
            ObserveRoomsUseCase(roomRepo),
            CreateDirectRoomUseCase(roomRepo),
            HandleInviteUseCase(roomRepo),
            LogoutUseCase(authRepo)
        )

        viewModel.sendIntent(RoomListUiIntent.OpenNewChatDialog)
        assertTrue(viewModel.uiState.value.showNewChatDialog)

        viewModel.sendIntent(RoomListUiIntent.NewChatUserIdChanged("@bob:matrix.org"))
        viewModel.sendIntent(RoomListUiIntent.CreateDirectRoom)

        advanceUntilIdle()

        assertEquals("@bob:matrix.org", roomRepo.directRoomCreatedTarget)
        assertFalse(viewModel.uiState.value.showNewChatDialog)
        assertFalse(viewModel.uiState.value.isCreatingRoom)

        Dispatchers.resetMain()
    }
}
