package com.bonfs.tcc.kmpchat.domain.usecase

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

private class FakeLogoutRepository : AuthRepository {
    var logoutCalled: Boolean = false

    override suspend fun login(credentials: LoginCredentials): UserSession {
        return UserSession("@bob:matrix.org", "DEVICE_456", "https://matrix.org")
    }

    override suspend fun logout() {
        logoutCalled = true
    }

    override suspend fun restoreSession(): UserSession? = null
    override suspend fun getCurrentSession(): UserSession? = null
    override fun observeSession(): Flow<UserSession?> = MutableStateFlow(null)
    override fun observeSyncState(): Flow<SyncState> = MutableStateFlow(SyncState.STOPPED)
}

class LogoutUseCaseTest {

    @Test
    fun shouldTriggerLogoutOnRepository() = runTest {
        val fakeRepo = FakeLogoutRepository()
        val logoutUseCase = LogoutUseCase(fakeRepo)

        logoutUseCase()
        assertTrue(fakeRepo.logoutCalled, "LogoutUseCase must trigger repository logout and wipe")
    }
}
