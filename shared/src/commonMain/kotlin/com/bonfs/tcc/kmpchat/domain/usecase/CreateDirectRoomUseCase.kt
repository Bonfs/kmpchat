package com.bonfs.tcc.kmpchat.domain.usecase

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.InvalidMatrixIdException
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

class CreateDirectRoomUseCase(
    private val roomRepository: RoomRepository
) {

    suspend operator fun invoke(targetUserId: String): String {
        validateMatrixId(targetUserId)
        return roomRepository.createDirectRoom(targetUserId.trim())
    }

    private fun validateMatrixId(userId: String) {
        val trimmed = userId.trim()
        if (trimmed.isBlank()) {
            throw EmptyFieldException("Matrix ID")
        }
        if (!trimmed.startsWith("@") || !trimmed.contains(":") || trimmed.length < 5) {
            throw InvalidMatrixIdException()
        }
    }
}
