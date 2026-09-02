package com.bonfs.tcc.kmpchat.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

import com.bonfs.tcc.kmpchat.ui.components.EmptyView
import com.bonfs.tcc.kmpchat.ui.screens.chat.ChatScreen
import com.bonfs.tcc.kmpchat.ui.screens.chat.ChatUiEffect
import com.bonfs.tcc.kmpchat.ui.screens.chat.ChatUiIntent
import com.bonfs.tcc.kmpchat.ui.screens.chat.ChatViewModel
import com.bonfs.tcc.kmpchat.ui.screens.roomlist.RoomListScreen
import com.bonfs.tcc.kmpchat.ui.screens.roomlist.RoomListUiEffect
import com.bonfs.tcc.kmpchat.ui.screens.roomlist.RoomListUiIntent
import com.bonfs.tcc.kmpchat.ui.screens.roomlist.RoomListViewModel
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBackground
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBorder

@Composable
fun AdaptiveMainLayout(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRoom by remember { mutableStateOf<Pair<String, String>?>(null) }
    val roomListViewModel: RoomListViewModel = koinInject()
    val chatViewModel: ChatViewModel = koinInject()

    ListenRoomListEffects(roomListViewModel, onLogout) { roomId, roomName ->
        selectedRoom = roomId to roomName
        chatViewModel.sendIntent(ChatUiIntent.LoadTimeline(roomId, roomName))
    }

    ListenChatEffects(chatViewModel) { selectedRoom = null }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MidnightBackground)) {
        if (maxWidth >= 720.dp) {
            DesktopMasterDetail(roomListViewModel, chatViewModel, selectedRoom)
        } else {
            MobileStackLayout(roomListViewModel, chatViewModel, selectedRoom)
        }
    }
}

@Composable
private fun DesktopMasterDetail(
    roomListViewModel: RoomListViewModel,
    chatViewModel: ChatViewModel,
    selectedRoom: Pair<String, String>?
) {
    val roomListState by roomListViewModel.uiState.collectAsState()
    val chatState by chatViewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.width(340.dp).fillMaxHeight()) {
            RoomListScreen(
                state = roomListState,
                onIntent = { roomListViewModel.sendIntent(it) }
            )
        }
        VerticalDivider(color = MidnightBorder, thickness = 1.dp)
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (selectedRoom != null) {
                ChatScreen(state = chatState, onIntent = { chatViewModel.sendIntent(it) })
            } else {
                EmptyView(
                    title = "Nenhuma conversa selecionada",
                    subtitle = "Selecione uma sala ao lado para visualizar o chat criptografado."
                )
            }
        }
    }
}

@Composable
private fun MobileStackLayout(
    roomListViewModel: RoomListViewModel,
    chatViewModel: ChatViewModel,
    selectedRoom: Pair<String, String>?
) {
    val roomListState by roomListViewModel.uiState.collectAsState()
    val chatState by chatViewModel.uiState.collectAsState()

    if (selectedRoom != null) {
        ChatScreen(state = chatState, onIntent = { chatViewModel.sendIntent(it) })
    } else {
        RoomListScreen(state = roomListState, onIntent = { roomListViewModel.sendIntent(it) })
    }
}

@Composable
private fun ListenRoomListEffects(
    viewModel: RoomListViewModel,
    onLogout: () -> Unit,
    onRoomSelected: (String, String) -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RoomListUiEffect.NavigateToChat -> onRoomSelected(effect.roomId, effect.roomName)
                is RoomListUiEffect.NavigateToLogin -> onLogout()
                is RoomListUiEffect.ShowSnackbar -> Unit
            }
        }
    }
}

@Composable
private fun ListenChatEffects(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatUiEffect.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }
}
