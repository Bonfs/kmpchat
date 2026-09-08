package com.bonfs.tcc.kmpchat.domain.usecase

import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.EventMessage
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository

class ObserveTimelineUseCase(
    private val chatRepository: ChatRepository
) {

    operator fun invoke(roomId: String): Flow<List<EventMessage>> {
        if (roomId.isBlank()) {
            throw EmptyFieldException("Room ID")
        }
        return chatRepository.observeTimeline(roomId.trim())
    }
}
