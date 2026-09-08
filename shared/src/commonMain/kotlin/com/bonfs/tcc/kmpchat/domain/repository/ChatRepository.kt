package com.bonfs.tcc.kmpchat.domain.repository

import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.model.EventMessage

interface ChatRepository {
    fun observeTimeline(roomId: String): Flow<List<EventMessage>>
    suspend fun sendMessage(roomId: String, content: String)
}
