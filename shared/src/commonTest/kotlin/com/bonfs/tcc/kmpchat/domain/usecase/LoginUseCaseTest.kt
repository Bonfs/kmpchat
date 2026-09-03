package com.bonfs.tcc.kmpchat.domain.usecase

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.InvalidCredentialsException
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

private class FakeAuthRepository : AuthRepository {
    var loginResult: Result<UserSession> = Result.success(
        UserSession("@alice:matrix.org", "DEVICE_123", "https://matrix.org")
    )
    var wasLogoutCalled: Boolean = false
    private val sessionFlow = MutableStateFlow<UserSession?>(null)

    override suspend fun login(credentials: LoginCredentials): UserSession {
        return loginResult.getOrThrow().also {
            sessionFlow.value = it
        }
    }

    override suspend fun logout() {
        wasLogoutCalled = true
        sessionFlow.value = null
    }

    override suspend fun restoreSession(): UserSession? = sessionFlow.value
    override suspend fun getCurrentSession(): UserSession? = sessionFlow.value
    override fun observeSession(): Flow<UserSession?> = sessionFlow
    override fun observeSyncState(): Flow<SyncState> = MutableStateFlow(SyncState.RUNNING)
}

class LoginUseCaseTest {

    private val fakeRepository = FakeAuthRepository()
    private val loginUseCase = LoginUseCase(fakeRepository)

    @Test
    fun shouldThrowExceptionWhenHomeserverIsEmpty() = runTest {
        val credentials = LoginCredentials("", "alice", "secret123")
        assertFailsWith<EmptyFieldException> {
            loginUseCase(credentials)
        }
    }

    @Test
    fun shouldThrowExceptionWhenUsernameIsEmpty() = runTest {
        val credentials = LoginCredentials("https://matrix.org", "", "secret123")
        assertFailsWith<EmptyFieldException> {
            loginUseCase(credentials)
        }
    }

    @Test
    fun shouldThrowExceptionWhenPasswordIsEmpty() = runTest {
        val credentials = LoginCredentials("https://matrix.org", "alice", "")
        assertFailsWith<EmptyFieldException> {
            loginUseCase(credentials)
        }
    }

    @Test
    fun shouldReturnSessionWhenCredentialsAreValid() = runTest {
        val credentials = LoginCredentials("https://matrix.org", "alice", "secret123")
        val session = loginUseCase(credentials)
        assertEquals("@alice:matrix.org", session.userId)
        assertEquals("DEVICE_123", session.deviceId)
        assertEquals("https://matrix.org", session.homeserverUrl)
    }

    @Test
    fun shouldPropagateRepositoryAuthenticationErrors() = runTest {
        fakeRepository.loginResult = Result.failure(InvalidCredentialsException())
        val credentials = LoginCredentials("https://matrix.org", "alice", "wrong_password")
        assertFailsWith<InvalidCredentialsException> {
            loginUseCase(credentials)
        }
    }
}
