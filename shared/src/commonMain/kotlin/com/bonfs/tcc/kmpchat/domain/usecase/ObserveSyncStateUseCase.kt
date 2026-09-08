package com.bonfs.tcc.kmpchat.domain.usecase

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlinx.coroutines.flow.Flow

import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

class ObserveSyncStateUseCase(
    private val authRepository: AuthRepository
) {

    operator fun invoke(): Flow<SyncState> {
        return authRepository.observeSyncState()
    }
}
