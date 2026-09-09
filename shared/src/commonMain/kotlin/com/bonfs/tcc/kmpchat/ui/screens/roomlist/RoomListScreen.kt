package com.bonfs.tcc.kmpchat.ui.screens.roomlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.ui.components.EmptyView
import com.bonfs.tcc.kmpchat.ui.components.ErrorView
import com.bonfs.tcc.kmpchat.ui.components.LoadingView
import com.bonfs.tcc.kmpchat.ui.components.RoomListItem
import com.bonfs.tcc.kmpchat.ui.theme.ErrorRed
import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBackground
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBorder
import com.bonfs.tcc.kmpchat.ui.theme.MidnightPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSecondary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSurface
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    state: RoomListUiState,
    onIntent: (RoomListUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { RoomListTopBar(onLogout = { onIntent(RoomListUiIntent.Logout) }) },
        floatingActionButton = {
            NewChatFab(onClick = { onIntent(RoomListUiIntent.OpenNewChatDialog) })
        },
        containerColor = MidnightBackground,
        modifier = modifier
    ) { innerPadding ->
        RoomListBody(state = state, onIntent = onIntent, padding = innerPadding)
    }

    if (state.showNewChatDialog) {
        NewChatDialog(state = state, onIntent = onIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListTopBar(onLogout: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Conversas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MidnightTextPrimary
            )
        },
        actions = {
            TextButton(onClick = onLogout) {
                Text(text = "Sair", color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightSurface)
    )
}

@Composable
private fun NewChatFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MidnightPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = "＋", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RoomListBody(
    state: RoomListUiState,
    onIntent: (RoomListUiIntent) -> Unit,
    padding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        when {
            state.isLoading || state.isInitialSyncing -> LoadingView(message = "Sincronizando conversas...")
            state.errorMessage != null -> ErrorView(
                message = state.errorMessage,
                onRetry = { onIntent(RoomListUiIntent.LoadRooms) }
            )
            state.rooms.isEmpty() && state.invites.isEmpty() -> EmptyView(
                title = "Nenhuma conversa ativa",
                subtitle = "Toque no botão + abaixo para iniciar um chat seguro por Matrix ID."
            )
            else -> RoomListContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun RoomListContent(
    state: RoomListUiState,
    onIntent: (RoomListUiIntent) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (state.invites.isNotEmpty()) {
            item { InvitesSectionHeader(count = state.invites.size) }
            items(state.invites, key = { "invite_${it.id}" }) { room ->
                InviteListItem(
                    room = room,
                    onAccept = { onIntent(RoomListUiIntent.AcceptInvite(room.id)) },
                    onReject = { onIntent(RoomListUiIntent.RejectInvite(room.id)) }
                )
                HorizontalDivider(color = MidnightBorder, thickness = 0.5.dp)
            }
        }
        items(state.rooms, key = { it.id }) { room ->
            RoomListItem(
                room = room,
                onClick = { onIntent(RoomListUiIntent.SelectRoom(room.id, room.name)) }
            )
            HorizontalDivider(color = MidnightBorder, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun InvitesSectionHeader(count: Int) {
    Text(
        text = "CONVITES RECEBIDOS ($count)",
        style = MaterialTheme.typography.labelSmall,
        color = MidnightSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun InviteListItem(
    room: ChatRoom,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = room.name,
                style = MaterialTheme.typography.titleMedium,
                color = MidnightTextPrimary
            )
            Text(
                text = "Convidou você para uma conversa",
                style = MaterialTheme.typography.bodyMedium,
                color = MidnightTextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(containerColor = MidnightPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Aceitar", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedButton(
            onClick = onReject,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Recusar", color = ErrorRed, fontSize = 12.sp)
        }
    }
}

@Composable
private fun NewChatDialog(
    state: RoomListUiState,
    onIntent: (RoomListUiIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(RoomListUiIntent.CloseNewChatDialog) },
        title = {
            Text(
                text = "Nova Conversa Direta",
                style = MaterialTheme.typography.titleLarge,
                color = MidnightTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Informe o Matrix ID do contato (ex: @alice:matrix.org):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MidnightTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.newChatUserId,
                    onValueChange = { onIntent(RoomListUiIntent.NewChatUserIdChanged(it)) },
                    placeholder = { Text("@usuario:servidor") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MidnightTextPrimary,
                        unfocusedTextColor = MidnightTextPrimary,
                        focusedBorderColor = MidnightPrimary,
                        unfocusedBorderColor = MidnightBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onIntent(RoomListUiIntent.CreateDirectRoom) },
                enabled = !state.isCreatingRoom && state.newChatUserId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MidnightPrimary)
            ) {
                if (state.isCreatingRoom) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text("Iniciar Chat")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(RoomListUiIntent.CloseNewChatDialog) }) {
                Text("Cancelar", color = MidnightTextSecondary)
            }
        },
        containerColor = MidnightSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview
@Composable
private fun RoomListScreenPreview() {
    KmpChatTheme {
        RoomListScreen(
            state = RoomListUiState(
                rooms = listOf(
                    ChatRoom(
                        id = "!dm1:matrix.org",
                        name = "Alice Silva",
                        lastEvent = ChatMessage(
                            id = "$1",
                            roomId = "!dm1:matrix.org",
                            senderId = "@alice:matrix.org",
                            senderName = "Alice",
                            content = "Criptografia E2EE ativa!",
                            timestamp = 1725148800000L,
                            isOutgoing = false,
                            isEncrypted = true,
                            deliveryStatus = MessageDeliveryStatus.SENT
                        ),
                        lastEventTimestamp = 1725148800000L,
                        unreadCount = 2,
                        isDirect = true,
                        isEncrypted = true,
                        membership = RoomMembership.JOIN
                    )
                ),
                invites = emptyList(),
                isLoading = false,
                errorMessage = null
            ),
            onIntent = {}
        )
    }
}
