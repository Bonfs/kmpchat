package com.bonfs.tcc.kmpchat.domain.usecase

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

class HandleInviteUseCase(
    private val roomRepository: RoomRepository
) {

    suspend fun accept(roomId: String) {
        validateRoomId(roomId)
        roomRepository.joinRoom(roomId.trim())
    }

    suspend fun reject(roomId: String) {
        validateRoomId(roomId)
        roomRepository.leaveRoom(roomId.trim())
    }

    private fun validateRoomId(roomId: String) {
        if (roomId.isBlank()) {
            throw EmptyFieldException("Room ID")
        }
    }
}
