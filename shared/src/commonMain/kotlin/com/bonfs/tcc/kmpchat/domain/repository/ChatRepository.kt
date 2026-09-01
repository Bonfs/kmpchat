package com.bonfs.tcc.kmpchat.domain.repository

import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.model.ChatMessage

interface ChatRepository {
    fun observeTimeline(roomId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(roomId: String, content: String)
}
