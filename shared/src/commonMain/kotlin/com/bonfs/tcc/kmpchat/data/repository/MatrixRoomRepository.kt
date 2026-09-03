package com.bonfs.tcc.kmpchat.data.repository

import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.flattenValues
import de.connect2x.trixnity.client.room
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.Membership
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

import com.bonfs.tcc.kmpchat.data.client.MatrixClientManager
import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.model.ClientNotInitializedException
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
            client?.room?.getAll()?.flattenValues()?.map { rooms ->
                rooms.map { mapTrixnityRoomToChatRoom(it) }
                    .sortedByDescending { it.lastEventTimestamp }
            }
                ?: flowOf(emptyList())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRoomById(roomId: String): Flow<ChatRoom?> {
        val targetRoomId = RoomId(roomId)
        return clientManager.clientState.flatMapLatest { client ->
            client?.room?.getById(targetRoomId)?.map { room ->
                room?.let { mapTrixnityRoomToChatRoom(it) }
            }
                ?: flowOf(null)
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

    private fun mapTrixnityRoomToChatRoom(room: de.connect2x.trixnity.client.store.Room): ChatRoom {
        val membership = when (room.membership) {
            Membership.JOIN -> RoomMembership.JOIN
            Membership.INVITE -> RoomMembership.INVITE
            Membership.LEAVE -> RoomMembership.LEAVE
            Membership.BAN -> RoomMembership.BAN
            else -> RoomMembership.LEAVE
        }
        val roomName = room.name?.explicitName ?: room.roomId.full
        return ChatRoom(
            id = room.roomId.full,
            name = roomName,
            lastMessage = null,
            lastEventTimestamp = room.lastRelevantEventTimestamp?.toEpochMilliseconds() ?: 0L,
            unreadCount = 0,
            isDirect = room.isDirect,
            isEncrypted = room.encrypted,
            membership = membership
        )
    }
}
