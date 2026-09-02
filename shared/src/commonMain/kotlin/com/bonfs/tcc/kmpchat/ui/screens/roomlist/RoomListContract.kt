package com.bonfs.tcc.kmpchat.ui.screens.roomlist

import com.bonfs.tcc.kmpchat.core.mvi.UiEffect
import com.bonfs.tcc.kmpchat.core.mvi.UiIntent
import com.bonfs.tcc.kmpchat.core.mvi.UiState
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom

data class RoomListUiState(
    val rooms: List<ChatRoom> = emptyList(),
    val invites: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingRoom: Boolean = false,
    val errorMessage: String? = null,
    val showNewChatDialog: Boolean = false,
    val newChatUserId: String = ""
) : UiState

sealed interface RoomListUiIntent : UiIntent {
    data object LoadRooms : RoomListUiIntent
    data class SelectRoom(val roomId: String, val roomName: String) : RoomListUiIntent
    data object OpenNewChatDialog : RoomListUiIntent
    data object CloseNewChatDialog : RoomListUiIntent
    data class NewChatUserIdChanged(val userId: String) : RoomListUiIntent
    data object CreateDirectRoom : RoomListUiIntent
    data class AcceptInvite(val roomId: String) : RoomListUiIntent
    data class RejectInvite(val roomId: String) : RoomListUiIntent
    data object Logout : RoomListUiIntent
}

sealed interface RoomListUiEffect : UiEffect {
    data class NavigateToChat(val roomId: String, val roomName: String) : RoomListUiEffect
    data object NavigateToLogin : RoomListUiEffect
    data class ShowSnackbar(val message: String) : RoomListUiEffect
}
