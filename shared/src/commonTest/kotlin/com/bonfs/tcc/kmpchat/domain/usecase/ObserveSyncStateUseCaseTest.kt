package com.bonfs.tcc.kmpchat.domain.usecase

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

private class FakeObserveSyncAuthRepository : AuthRepository {
    val syncStateFlow = MutableStateFlow(SyncState.INITIAL_SYNC)

    override fun observeSyncState(): Flow<SyncState> = syncStateFlow
    override fun observeSession(): Flow<UserSession?> = MutableStateFlow(null)
    override suspend fun login(credentials: LoginCredentials): UserSession = throw UnsupportedOperationException()
    override suspend fun logout() {}
    override suspend fun restoreSession(): UserSession? = null
    override suspend fun getCurrentSession(): UserSession? = null
}

class ObserveSyncStateUseCaseTest {

    @Test
    fun shouldEmitCurrentSyncState() = runTest {
        val fakeRepo = FakeObserveSyncAuthRepository()
        val useCase = ObserveSyncStateUseCase(fakeRepo)

        val state = useCase().first()
        assertEquals(SyncState.INITIAL_SYNC, state)

        fakeRepo.syncStateFlow.value = SyncState.RUNNING
        val updatedState = useCase().first()
        assertEquals(SyncState.RUNNING, updatedState)
    }
}
