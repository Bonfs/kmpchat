package com.bonfs.tcc.kmpchat.domain.usecase

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {

    suspend operator fun invoke(roomId: String, content: String) {
        validateInput(roomId, content)
        chatRepository.sendMessage(roomId.trim(), content.trim())
    }

    private fun validateInput(roomId: String, content: String) {
        if (roomId.isBlank()) {
            throw EmptyFieldException("Room ID")
        }
        if (content.isBlank()) {
            throw EmptyFieldException("Mensagem")
        }
    }
}
