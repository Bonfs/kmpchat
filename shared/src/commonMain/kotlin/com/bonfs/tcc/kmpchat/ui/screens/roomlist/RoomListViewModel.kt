package com.bonfs.tcc.kmpchat.ui.screens.roomlist

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.bonfs.tcc.kmpchat.core.mvi.BaseViewModel
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.MatrixRoomException
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.domain.usecase.CreateDirectRoomUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.HandleInviteUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.LogoutUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveRoomsUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveSyncStateUseCase
import de.connect2x.trixnity.clientserverapi.client.SyncState

class RoomListViewModel(
    private val observeRoomsUseCase: ObserveRoomsUseCase,
    private val createDirectRoomUseCase: CreateDirectRoomUseCase,
    private val handleInviteUseCase: HandleInviteUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeSyncStateUseCase: ObserveSyncStateUseCase
) : BaseViewModel<RoomListUiState, RoomListUiIntent, RoomListUiEffect>(RoomListUiState(isLoading = true)) {

    init {
        observeRoomsStream()
        observeSyncStateStream()
    }

    override fun sendIntent(intent: RoomListUiIntent) {
        when (intent) {
            is RoomListUiIntent.LoadRooms -> observeRoomsStream()
            is RoomListUiIntent.SelectRoom -> onSelectRoom(intent.roomId, intent.roomName)
            is RoomListUiIntent.OpenNewChatDialog -> updateState { it.copy(showNewChatDialog = true) }
            is RoomListUiIntent.CloseNewChatDialog -> updateState { it.copy(showNewChatDialog = false, newChatUserId = "") }
            is RoomListUiIntent.NewChatUserIdChanged -> updateState { it.copy(newChatUserId = intent.userId) }
            is RoomListUiIntent.CreateDirectRoom -> onCreateDirectRoom()
            is RoomListUiIntent.AcceptInvite -> onAcceptInvite(intent.roomId)
            is RoomListUiIntent.RejectInvite -> onRejectInvite(intent.roomId)
            is RoomListUiIntent.Logout -> onLogout()
        }
    }

    private fun observeRoomsStream() {
        viewModelScope.launch {
            observeRoomsUseCase()
                .catch { handleStreamError(it) }
                .collectLatest { roomList ->
                    val (invites, active) = partitionRooms(roomList)
                    updateState {
                        it.copy(rooms = active, invites = invites, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    private fun observeSyncStateStream() {
        viewModelScope.launch {
            observeSyncStateUseCase()
                .collectLatest { syncState ->
                    updateState {
                        it.copy(isInitialSyncing = syncState == SyncState.INITIAL_SYNC)
                    }
                }
        }
    }

    private fun partitionRooms(roomList: List<ChatRoom>): Pair<List<ChatRoom>, List<ChatRoom>> {
        val invites = roomList.filter { it.membership == RoomMembership.INVITE }
        val active = roomList.filter { it.membership == RoomMembership.JOIN }
        return invites to active
    }

    private fun onSelectRoom(roomId: String, roomName: String) {
        emitEffect(RoomListUiEffect.NavigateToChat(roomId, roomName))
    }

    private fun onCreateDirectRoom() {
        val targetUserId = currentState.newChatUserId.trim()
        if (targetUserId.isBlank() || currentState.isCreatingRoom) return

        viewModelScope.launch {
            updateState { it.copy(isCreatingRoom = true) }
            try {
                val roomId = createDirectRoomUseCase(targetUserId)
                updateState { it.copy(isCreatingRoom = false, showNewChatDialog = false, newChatUserId = "") }
                emitEffect(RoomListUiEffect.NavigateToChat(roomId, targetUserId))
            } catch (exception: MatrixRoomException) {
                updateState { it.copy(isCreatingRoom = false) }
                emitEffect(RoomListUiEffect.ShowSnackbar(exception.message ?: "Erro ao criar sala"))
            } catch (exception: Exception) {
                updateState { it.copy(isCreatingRoom = false) }
                emitEffect(RoomListUiEffect.ShowSnackbar("Erro inesperado ao criar sala"))
            }
        }
    }

    private fun onAcceptInvite(roomId: String) {
        viewModelScope.launch {
            try {
                handleInviteUseCase.accept(roomId)
            } catch (exception: Exception) {
                emitEffect(RoomListUiEffect.ShowSnackbar("Erro ao aceitar convite"))
            }
        }
    }

    private fun onRejectInvite(roomId: String) {
        viewModelScope.launch {
            try {
                handleInviteUseCase.reject(roomId)
            } catch (exception: Exception) {
                emitEffect(RoomListUiEffect.ShowSnackbar("Erro ao recusar convite"))
            }
        }
    }

    private fun onLogout() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                logoutUseCase()
            } finally {
                emitEffect(RoomListUiEffect.NavigateToLogin)
            }
        }
    }

    private fun handleStreamError(throwable: Throwable) {
        updateState { it.copy(isLoading = false, errorMessage = "Erro ao sincronizar salas") }
    }
}
