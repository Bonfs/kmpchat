package com.bonfs.tcc.kmpchat.data.repository

import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.flattenValues
import de.connect2x.trixnity.client.room
import de.connect2x.trixnity.client.store.Room as TrixnityRoom
import de.connect2x.trixnity.client.store.TimelineEvent
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.Membership
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

import com.bonfs.tcc.kmpchat.data.client.MatrixClientManager
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.ClientNotInitializedException
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.model.InvalidMatrixIdException
import com.bonfs.tcc.kmpchat.domain.model.RoomCreationException
import com.bonfs.tcc.kmpchat.domain.model.RoomMembership
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

class MatrixRoomRepository(
    private val clientManager: MatrixClientManager
) : RoomRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRooms(): Flow<List<ChatRoom>> {
        return clientManager.clientState.flatMapLatest { client ->
            if (client == null) {
                flowOf(emptyList())
            } else {
                client.room.getAll().flattenValues().flatMapLatest { rooms ->
                    if (rooms.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val roomFlows = rooms.map { observeRoomWithLastEvent(client, it) }
                        combine(roomFlows) { chatRooms ->
                            chatRooms.toList().sortedByDescending { it.lastEventTimestamp }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRoomById(roomId: String): Flow<ChatRoom?> {
        val targetRoomId = RoomId(roomId)
        return clientManager.clientState.flatMapLatest { client ->
            if (client == null) {
                flowOf(null)
            } else {
                client.room.getById(targetRoomId).flatMapLatest { room ->
                    if (room == null) {
                        flowOf(null)
                    } else {
                        observeRoomWithLastEvent(client, room).map { it as ChatRoom? }
                    }
                }
            }
        }
    }

    override suspend fun createDirectRoom(targetUserId: String): String {
        val client = getActiveClient()
        val parsedUserId = parseUserId(targetUserId)
        return try {
            val response = client.api.room.createRoom(
                isDirect = true,
                invite = setOf(parsedUserId)
            ).getOrThrow()
            response.full
        } catch (exception: Exception) {
            throw RoomCreationException("Falha ao criar sala direta com $targetUserId", exception)
        }
    }

    override suspend fun joinRoom(roomId: String) {
        val client = getActiveClient()
        try {
            client.api.room.joinRoom(RoomId(roomId)).getOrThrow()
        } catch (exception: Exception) {
            throw RoomCreationException("Falha ao aceitar convite para a sala $roomId", exception)
        }
    }

    override suspend fun leaveRoom(roomId: String) {
        val client = getActiveClient()
        try {
            client.api.room.leaveRoom(RoomId(roomId)).getOrThrow()
        } catch (exception: Exception) {
            throw RoomCreationException("Falha ao sair/recusar sala $roomId", exception)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRoomWithLastEvent(
        client: MatrixClient,
        room: TrixnityRoom
    ): Flow<ChatRoom> {
        val lastEventId = room.lastRelevantEventId
        val timelineFlow: Flow<TimelineEvent?> = if (lastEventId != null) {
            client.room.getTimelineEvent(room.roomId, lastEventId)
        } else {
            flowOf(null)
        }
        return timelineFlow.map { timelineEvent ->
            mapTrixnityRoomToChatRoom(client, room, timelineEvent)
        }
    }

    private fun getActiveClient(): MatrixClient {
        return clientManager.currentClient ?: throw ClientNotInitializedException()
    }

    private fun parseUserId(rawUserId: String): UserId {
        return try {
            UserId(rawUserId)
        } catch (exception: Exception) {
            throw InvalidMatrixIdException()
        }
    }

    private fun mapTrixnityRoomToChatRoom(
        client: MatrixClient,
        room: TrixnityRoom,
        timelineEvent: TimelineEvent?
    ): ChatRoom {
        val membership: RoomMembership = when (room.membership) {
            Membership.JOIN -> RoomMembership.JOIN
            Membership.INVITE -> RoomMembership.INVITE
            Membership.LEAVE -> RoomMembership.LEAVE
            Membership.BAN -> RoomMembership.BAN
            else -> RoomMembership.LEAVE
        }
        val roomName: String = room.name?.explicitName ?: room.roomId.full
        val lastEvent: EventMessage? = timelineEvent?.let {
            TimelineEventMapper.mapToEventMessage(client, it)
        }
        val lastTimestamp: Long = lastEvent?.timestamp
            ?: room.lastRelevantEventTimestamp?.toEpochMilliseconds()
            ?: 0L

        return ChatRoom(
            id = room.roomId.full,
            name = roomName,
            lastEvent = lastEvent,
            lastEventTimestamp = lastTimestamp,
            unreadCount = 0,
            isDirect = room.isDirect,
            isEncrypted = room.encrypted,
            membership = membership
        )
    }
}
