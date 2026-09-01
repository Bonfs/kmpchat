package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.model.ChatRoom
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository

class ObserveRoomsUseCase(
    private val roomRepository: RoomRepository
) {

    operator fun invoke(): Flow<List<ChatRoom>> {
        return roomRepository.observeRooms()
    }
}
