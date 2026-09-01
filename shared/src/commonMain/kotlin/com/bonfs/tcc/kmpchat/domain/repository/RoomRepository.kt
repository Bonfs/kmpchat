package com.bonfs.tcc.kmpchat.domain.repository

import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.model.ChatRoom

interface RoomRepository {
    fun observeRooms(): Flow<List<ChatRoom>>
    fun observeRoomById(roomId: String): Flow<ChatRoom?>
    suspend fun createDirectRoom(targetUserId: String): String
    suspend fun joinRoom(roomId: String)
    suspend fun leaveRoom(roomId: String)
}
