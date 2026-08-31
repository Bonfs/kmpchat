package com.bonfs.tcc.kmpchat.domain.repository

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): UserSession
    suspend fun logout()
    suspend fun getCurrentSession(): UserSession?
    fun observeSession(): Flow<UserSession?>
    fun observeSyncState(): Flow<SyncState>
}
