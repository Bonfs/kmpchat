package com.bonfs.tcc.kmpchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.ui.theme.ErrorRed
import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBubbleIncoming
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBubbleOutgoing
import com.bonfs.tcc.kmpchat.ui.theme.MidnightE2EE
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSecondary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextSecondary

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val alignment = if (message.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = alignment
    ) {
        MessageBubbleContent(message)
    }
}

@Composable
private fun MessageBubbleContent(message: ChatMessage) {
    val bubbleColor = if (message.isOutgoing) MidnightBubbleOutgoing else MidnightBubbleIncoming
    val shape = resolveBubbleShape(message.isOutgoing)

    Column(
        modifier = Modifier
            .widthIn(min = 60.dp, max = 290.dp)
            .clip(shape)
            .background(bubbleColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (!message.isOutgoing && message.senderName != null) {
            SenderHeader(message.senderName)
        }
        MessageBodyText(message.content)
        MessageFooter(message)
    }
}

@Composable
private fun SenderHeader(senderName: String) {
    Text(
        text = senderName,
        style = MaterialTheme.typography.labelSmall,
        color = MidnightSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun MessageBodyText(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MidnightTextPrimary
    )
}

@Composable
private fun MessageFooter(message: ChatMessage) {
    val formattedTime = formatTimestamp(message.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (message.isEncrypted) {
            EncryptedBadge()
            Spacer(modifier = Modifier.width(4.dp))
        }
        TimestampLabel(formattedTime)
        if (message.isOutgoing) {
            Spacer(modifier = Modifier.width(4.dp))
            DeliveryStatusIndicator(message.deliveryStatus)
        }
    }
}

@Composable
private fun EncryptedBadge() {
    Text(
        text = "🔒",
        color = MidnightE2EE,
        fontSize = 9.sp
    )
}

@Composable
private fun TimestampLabel(timeString: String) {
    Text(
        text = timeString,
        style = MaterialTheme.typography.labelSmall,
        color = MidnightTextSecondary,
        fontSize = 10.sp
    )
}

@Composable
private fun DeliveryStatusIndicator(status: MessageDeliveryStatus) {
    val (symbol, color) = when (status) {
        MessageDeliveryStatus.SENDING -> "🕒" to MidnightTextSecondary
        MessageDeliveryStatus.SENT -> "✓" to MidnightE2EE
        MessageDeliveryStatus.RECEIVED -> "✓✓" to MidnightE2EE
        MessageDeliveryStatus.FAILED -> "!" to ErrorRed
    }
    Text(
        text = symbol,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )
}

private fun resolveBubbleShape(isOutgoing: Boolean): RoundedCornerShape {
    return if (isOutgoing) {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp)
    }
}

private fun formatTimestamp(epochMs: Long): String {
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
private fun MessageBubblePreview() {
    KmpChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 16.dp)
        ) {
            MessageBubble(
                message = ChatMessage(
                    id = "1",
                    roomId = "!room:matrix.org",
                    senderId = "@alice:matrix.org",
                    senderName = "Alice",
                    content = "Olá! Esta mensagem é protegida com criptografia ponta a ponta (E2EE) via Vodozemac.",
                    timestamp = 1725148800000L,
                    isOutgoing = false,
                    isEncrypted = true,
                    deliveryStatus = MessageDeliveryStatus.RECEIVED
                )
            )
            MessageBubble(
                message = ChatMessage(
                    id = "2",
                    roomId = "!room:matrix.org",
                    senderId = "@bob:matrix.org",
                    senderName = "Bob",
                    content = "Excelente! Criptografia validada com sucesso.",
                    timestamp = 1725148860000L,
                    isOutgoing = true,
                    isEncrypted = true,
                    deliveryStatus = MessageDeliveryStatus.SENT
                )
            )
            MessageBubble(
                message = ChatMessage(
                    id = "3",
                    roomId = "!room:matrix.org",
                    senderId = "@bob:matrix.org",
                    senderName = "Bob",
                    content = "Enviando outro pacote de dados...",
                    timestamp = 1725148920000L,
                    isOutgoing = true,
                    isEncrypted = true,
                    deliveryStatus = MessageDeliveryStatus.SENDING
                )
            )
        }
    }
}
