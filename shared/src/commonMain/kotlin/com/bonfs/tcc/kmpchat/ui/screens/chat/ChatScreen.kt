package com.bonfs.tcc.kmpchat.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.ui.components.ChatAvatar
import com.bonfs.tcc.kmpchat.ui.components.EmptyView
import com.bonfs.tcc.kmpchat.ui.components.ErrorView
import com.bonfs.tcc.kmpchat.ui.components.LoadingView
import com.bonfs.tcc.kmpchat.ui.components.MessageBubble
import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBackground
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBorder
import com.bonfs.tcc.kmpchat.ui.theme.MidnightE2EE
import com.bonfs.tcc.kmpchat.ui.theme.MidnightPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSurface
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onIntent: (ChatUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = { ChatTopBar(roomName = state.roomName, onBack = { onIntent(ChatUiIntent.NavigateBack) }) },
        bottomBar = { ChatBottomBar(state = state, onIntent = onIntent) },
        containerColor = MidnightBackground,
        modifier = modifier
    ) { innerPadding ->
        ChatBody(
            state = state,
            listState = listState,
            onIntent = onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(roomName: String, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            TextButton(onClick = onBack) {
                Text(text = "‹ Voltar", color = MidnightPrimary, fontSize = 16.sp)
            }
        },
        title = { ChatTopBarTitle(roomName = roomName) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightSurface)
    )
}

@Composable
private fun ChatTopBarTitle(roomName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ChatAvatar(name = roomName, size = 36.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = roomName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MidnightTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "🔒 Criptografia E2EE",
                style = MaterialTheme.typography.labelSmall,
                color = MidnightE2EE
            )
        }
    }
}

@Composable
private fun ChatBody(
    state: ChatUiState,
    listState: LazyListState,
    onIntent: (ChatUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> LoadingView(message = "Carregando histórico...")
            state.errorMessage != null -> ErrorView(message = state.errorMessage)
            state.messages.isEmpty() -> EmptyView(
                title = "Início da conversa criptografada",
                subtitle = "As mensagens nesta sala são protegidas com Vodozemac E2EE."
            )
            else -> ChatTimeline(state = state, listState = listState, onIntent = onIntent)
        }
    }
}

@Composable
private fun ChatTimeline(
    state: ChatUiState,
    listState: LazyListState,
    onIntent: (ChatUiIntent) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(state.messages, key = { it.id }) { message ->
            MessageBubble(message = message)
        }
    }
}

@Composable
private fun ChatBottomBar(
    state: ChatUiState,
    onIntent: (ChatUiIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatInputField(
            value = state.inputText,
            onValueChange = { onIntent(ChatUiIntent.InputTextChanged(it)) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        ChatSendButton(
            isSending = state.isSending,
            isEnabled = state.inputText.isNotBlank() && !state.isSending,
            onSend = { onIntent(ChatUiIntent.SendMessage) }
        )
    }
}

@Composable
private fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Mensagem segura...", color = MidnightTextSecondary) },
        maxLines = 4,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MidnightTextPrimary,
            unfocusedTextColor = MidnightTextPrimary,
            focusedBorderColor = MidnightPrimary,
            unfocusedBorderColor = MidnightBorder
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    )
}

@Composable
private fun ChatSendButton(
    isSending: Boolean,
    isEnabled: Boolean,
    onSend: () -> Unit
) {
    IconButton(
        onClick = onSend,
        enabled = isEnabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isEnabled) MidnightPrimary else MidnightBorder,
            contentColor = Color.White
        ),
        modifier = Modifier.size(44.dp)
    ) {
        if (isSending) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(text = "➤", fontSize = 16.sp)
        }
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    KmpChatTheme {
        ChatScreen(
            state = ChatUiState(
                roomId = "!room:matrix.org",
                roomName = "Alice Silva",
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        roomId = "!room:matrix.org",
                        senderId = "@alice:matrix.org",
                        senderName = "Alice",
                        content = "Olá! Canal E2EE estabelecido com sucesso.",
                        timestamp = 1725148800000L,
                        isOutgoing = false,
                        isEncrypted = true,
                        deliveryStatus = MessageDeliveryStatus.RECEIVED
                    ),
                    ChatMessage(
                        id = "2",
                        roomId = "!room:matrix.org",
                        senderId = "@me:matrix.org",
                        senderName = "Eu",
                        content = "Perfeito! Todas as mensagens protegidas.",
                        timestamp = 1725148860000L,
                        isOutgoing = true,
                        isEncrypted = true,
                        deliveryStatus = MessageDeliveryStatus.SENT
                    )
                ),
                inputText = "",
                isLoading = false,
                isSending = false
            ),
            onIntent = {}
        )
    }
}
