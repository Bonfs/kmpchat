package com.bonfs.tcc.kmpchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.domain.model.UnknownEventMessage
import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme
import com.bonfs.tcc.kmpchat.ui.theme.MidnightE2EE
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSecondary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextSecondary
import com.bonfs.tcc.kmpchat.ui.theme.WarningOrange

@Composable
fun RoomListItem(
    room: ChatRoom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatAvatar(name = room.name, size = 48.dp)
        Spacer(modifier = Modifier.width(12.dp))
        RoomInfoSection(room = room, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        RoomMetadataSection(room = room)
    }
}

@Composable
private fun RoomInfoSection(room: ChatRoom, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        RoomTitleRow(room)
        Spacer(modifier = Modifier.height(3.dp))
        RoomSubtitleText(room)
    }
}

@Composable
private fun RoomTitleRow(room: ChatRoom) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = room.name,
            style = MaterialTheme.typography.titleMedium,
            color = MidnightTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (room.isEncrypted) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "🔒", color = MidnightE2EE, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RoomSubtitleText(room: ChatRoom) {
    val (text, color) = resolveSubtitle(room)
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RoomMetadataSection(room: ChatRoom) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        if (room.lastEventTimestamp > 0L) {
            Text(
                text = formatRelativeTime(room.lastEventTimestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MidnightTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (room.unreadCount > 0) {
            UnreadCounterBadge(room.unreadCount)
        }
    }
}

@Composable
private fun UnreadCounterBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(MidnightSecondary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.Black,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun resolveSubtitle(room: ChatRoom): Pair<String, Color> {
    return when {
        room.membership == RoomMembership.INVITE -> "Convite recebido" to WarningOrange
        room.lastEvent != null -> resolveEventSubtitle(room.lastEvent)
        else -> "Nenhuma mensagem ainda" to MidnightTextSecondary.copy(alpha = 0.6f)
    }
}

private fun resolveEventSubtitle(event: EventMessage): Pair<String, Color> {
    return when (event) {
        is ChatMessage -> event.content to MidnightTextSecondary
        is UnknownEventMessage -> "Evento não suportado" to MidnightTextSecondary
    }
}

private fun formatRelativeTime(epochMs: Long): String {
    if (epochMs <= 0L) return ""
    val totalSeconds = epochMs / 1000L
    val totalMinutes = totalSeconds / 60L
    val hours = (totalMinutes / 60L) % 24L
    val minutes = totalMinutes % 60L
    val h = hours.toString().padStart(2, '0')
    val m = minutes.toString().padStart(2, '0')
    return "$h:$m"
}

@Preview
@Composable
private fun RoomListItemPreview() {
    KmpChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RoomListItem(
                room = ChatRoom(
                    id = "!general:matrix.org",
                    name = "Equipe TCC Matrix",
                    lastEvent = ChatMessage(
                        id = "$1",
                        roomId = "!general:matrix.org",
                        senderId = "@alice:matrix.org",
                        senderName = "Alice",
                        content = "Criptografia ponta a ponta configurada!",
                        timestamp = 1725148800000L,
                        isOutgoing = false,
                        isEncrypted = true,
                        deliveryStatus = MessageDeliveryStatus.SENT
                    ),
                    lastEventTimestamp = 1725148800000L,
                    unreadCount = 3,
                    isDirect = false,
                    isEncrypted = true,
                    membership = RoomMembership.JOIN
                ),
                onClick = {}
            )
            RoomListItem(
                room = ChatRoom(
                    id = "!alice:matrix.org",
                    name = "Alice Silva",
                    lastEvent = null,
                    lastEventTimestamp = 1725148860000L,
                    unreadCount = 0,
                    isDirect = true,
                    isEncrypted = true,
                    membership = RoomMembership.INVITE
                ),
                onClick = {}
            )
        }
    }
}
