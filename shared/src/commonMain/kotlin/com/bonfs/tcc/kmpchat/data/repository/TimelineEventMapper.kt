package com.bonfs.tcc.kmpchat.data.repository

import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.store.TimelineEvent
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.model.UnknownEventMessage

object TimelineEventMapper {

    fun mapToEventMessage(
        client: MatrixClient,
        timelineEvent: TimelineEvent
    ): EventMessage {
        val isOutgoing: Boolean = timelineEvent.event.sender == client.userId
        return when (val content = timelineEvent.content?.getOrNull()) {
            is RoomMessageEventContent.TextBased.Text -> createChatMessage(timelineEvent, content.body, isOutgoing)
            is RoomMessageEventContent.TextBased.Notice -> createChatMessage(timelineEvent, content.body, isOutgoing)
            else -> createUnknownEventMessage(timelineEvent, isOutgoing)
        }
    }

    private fun createChatMessage(
        timelineEvent: TimelineEvent,
        contentBody: String,
        isOutgoing: Boolean
    ): ChatMessage {
        val event = timelineEvent.event
        val isEncrypted: Boolean = timelineEvent.event.content is RoomMessageEventContent
        return ChatMessage(
            id = event.id.full,
            roomId = event.roomId.full,
            senderId = event.sender.full,
            senderName = event.sender.localpart,
            content = contentBody,
            timestamp = event.originTimestamp,
            isOutgoing = isOutgoing,
            isEncrypted = isEncrypted,
            deliveryStatus = MessageDeliveryStatus.SENT
        )
    }

    private fun createUnknownEventMessage(
        timelineEvent: TimelineEvent,
        isOutgoing: Boolean
    ): UnknownEventMessage {
        val event = timelineEvent.event
        val isEncrypted: Boolean = timelineEvent.event.content is RoomMessageEventContent
        return UnknownEventMessage(
            id = event.id.full,
            roomId = event.roomId.full,
            senderId = event.sender.full,
            senderName = event.sender.localpart,
            rawType = event.content::class.simpleName,
            timestamp = event.originTimestamp,
            isOutgoing = isOutgoing,
            isEncrypted = isEncrypted,
            deliveryStatus = MessageDeliveryStatus.SENT
        )
    }
}
