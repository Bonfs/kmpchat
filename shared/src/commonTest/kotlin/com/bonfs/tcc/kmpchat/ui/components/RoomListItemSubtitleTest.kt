package com.bonfs.tcc.kmpchat.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.domain.model.UnknownEventMessage

class RoomListItemSubtitleTest {

    @Test
    fun shouldReturnTextMessageContentWhenLastEventIsChatMessage() {
        val message = ChatMessage(
            id = "$1",
            roomId = "!room1:matrix.org",
            senderId = "@alice:matrix.org",
            senderName = "Alice",
            content = "Olá, tudo bem?",
            timestamp = 1000L,
            isOutgoing = false,
            isEncrypted = true,
            deliveryStatus = MessageDeliveryStatus.SENT
        )
        val room = ChatRoom(
            id = "!room1:matrix.org",
            name = "Alice",
            lastEvent = message,
            lastEventTimestamp = 1000L,
            unreadCount = 0,
            isDirect = true,
            isEncrypted = true,
            membership = RoomMembership.JOIN
        )

        assertEquals("Olá, tudo bem?", room.lastMessage)
    }

    @Test
    fun shouldReturnUnsupportedMessageWhenLastEventIsUnknownEventMessage() {
        val unknownEvent = UnknownEventMessage(
            id = "$2",
            roomId = "!room1:matrix.org",
            senderId = "@alice:matrix.org",
            senderName = "Alice",
            rawType = "m.reaction",
            timestamp = 2000L,
            isOutgoing = false,
            isEncrypted = true,
            deliveryStatus = MessageDeliveryStatus.SENT
        )
        val room = ChatRoom(
            id = "!room1:matrix.org",
            name = "Alice",
            lastEvent = unknownEvent,
            lastEventTimestamp = 2000L,
            unreadCount = 0,
            isDirect = true,
            isEncrypted = true,
            membership = RoomMembership.JOIN
        )

        assertEquals("Evento não suportado", room.lastMessage)
    }

    @Test
    fun shouldReturnNullWhenRoomHasNoLastEvent() {
        val room = ChatRoom(
            id = "!room1:matrix.org",
            name = "Alice",
            lastEvent = null,
            lastEventTimestamp = 0L,
            unreadCount = 0,
            isDirect = true,
            isEncrypted = true,
            membership = RoomMembership.JOIN
        )

        assertNull(room.lastMessage)
    }
}
