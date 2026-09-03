package com.bonfs.tcc.kmpchat.data.repository

import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.room
import de.connect2x.trixnity.client.room.message.text
import de.connect2x.trixnity.client.room.toFlowList
import de.connect2x.trixnity.client.store.TimelineEvent
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

import com.bonfs.tcc.kmpchat.data.client.MatrixClientManager
import com.bonfs.tcc.kmpchat.domain.model.ChatMessage
import com.bonfs.tcc.kmpchat.domain.model.ClientNotInitializedException
import com.bonfs.tcc.kmpchat.domain.model.MessageDeliveryStatus
import com.bonfs.tcc.kmpchat.domain.model.MessageSendException
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository

private const val DEFAULT_TIMELINE_PAGE_SIZE = 50

class MatrixChatRepository(
    private val clientManager: MatrixClientManager
) : ChatRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTimeline(roomId: String): Flow<List<ChatMessage>> {
        val targetRoomId = RoomId(roomId)
        return clientManager.clientState.flatMapLatest { client ->
            if (client == null) {
                flowOf(emptyList())
            } else {
                observeClientTimeline(client, targetRoomId)
            }
        }
    }

    override suspend fun sendMessage(roomId: String, content: String) {
        val client = clientManager.currentClient ?: throw ClientNotInitializedException()
        try {
            client.room.sendMessage(RoomId(roomId)) {
                text(content)
            }
        } catch (exception: Exception) {
            throw MessageSendException("Falha ao enviar mensagem na sala $roomId", exception)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeClientTimeline(
        client: MatrixClient,
        roomId: RoomId
    ): Flow<List<ChatMessage>> {
        val configFlow = MutableStateFlow(DEFAULT_TIMELINE_PAGE_SIZE)
        return client.room.getLastTimelineEvents(roomId)
            .toFlowList(configFlow)
            .flatMapLatest { eventFlows ->
                if (eventFlows.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(eventFlows) { events: Array<TimelineEvent> ->
                        events.mapNotNull { mapTimelineEventToChatMessage(client, it) }
                    }
                }
            }
    }

    private fun mapTimelineEventToChatMessage(
        client: MatrixClient,
        timelineEvent: TimelineEvent
    ): ChatMessage? {
        val event = timelineEvent.event
        val content = timelineEvent.content?.getOrNull()
        val textBody = when (content) {
            is RoomMessageEventContent.TextBased.Text -> content.body
            is RoomMessageEventContent.TextBased.Notice -> content.body
            else -> return null
        }
        val isOutgoing = event.sender == client.userId
        val isEncrypted = timelineEvent.event.content is RoomMessageEventContent
        return ChatMessage(
            id = event.id.full,
            roomId = event.roomId.full,
            senderId = event.sender.full,
            senderName = event.sender.localpart,
            content = textBody,
            timestamp = event.originTimestamp,
            isOutgoing = isOutgoing,
            isEncrypted = isEncrypted,
            deliveryStatus = MessageDeliveryStatus.SENT
        )
    }
}
