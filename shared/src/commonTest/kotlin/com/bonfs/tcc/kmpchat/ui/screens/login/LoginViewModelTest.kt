package com.bonfs.tcc.kmpchat.ui.screens.login

import de.connect2x.trixnity.clientserverapi.client.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import com.bonfs.tcc.kmpchat.core.util.Logger
import com.bonfs.tcc.kmpchat.domain.model.InvalidCredentialsException
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository
import com.bonfs.tcc.kmpchat.domain.usecase.LoginUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeLogger : Logger {
        override fun logInfo(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun logWarn(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun logError(tag: String?, throwable: Throwable?, message: () -> String) {}
    }

    private class FakeAuthRepo(
        private val shouldFail: Boolean = false
    ) : AuthRepository {
        var loginCalled = false

        override fun observeSession(): Flow<UserSession?> = flowOf(null)
        override suspend fun getCurrentSession(): UserSession? = null
        override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.STOPPED)

        override suspend fun login(credentials: LoginCredentials): UserSession {
            loginCalled = true
            if (shouldFail) {
                throw InvalidCredentialsException("Usuário ou senha incorretos")
            }
            return UserSession(
                userId = "@test:matrix.org",
                deviceId = "DEV1",
                homeserverUrl = credentials.homeserverUrl
            )
        }

        override suspend fun logout() {}
    }

    @Test
    fun shouldUpdateFieldsWhenIntentsReceived() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepo()), FakeLogger())

        viewModel.sendIntent(LoginUiIntent.HomeserverChanged("https://custom.matrix.org"))
        viewModel.sendIntent(LoginUiIntent.UsernameChanged("alice"))
        viewModel.sendIntent(LoginUiIntent.PasswordChanged("secret"))

        assertEquals("https://custom.matrix.org", viewModel.uiState.value.homeserver)
        assertEquals("alice", viewModel.uiState.value.username)
        assertEquals("secret", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)

        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoginSuccessfullyAndEmitNavigateEffect() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val fakeRepo = FakeAuthRepo(shouldFail = false)
        val viewModel = LoginViewModel(LoginUseCase(fakeRepo), FakeLogger())

        viewModel.sendIntent(LoginUiIntent.UsernameChanged("alice"))
        viewModel.sendIntent(LoginUiIntent.PasswordChanged("secret"))
        viewModel.sendIntent(LoginUiIntent.SubmitLogin)

        advanceUntilIdle()

        assertTrue(fakeRepo.loginCalled)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)

        Dispatchers.resetMain()
    }

    @Test
    fun shouldSetErrorMessageWhenLoginFails() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val fakeRepo = FakeAuthRepo(shouldFail = true)
        val viewModel = LoginViewModel(LoginUseCase(fakeRepo), FakeLogger())

        viewModel.sendIntent(LoginUiIntent.UsernameChanged("alice"))
        viewModel.sendIntent(LoginUiIntent.PasswordChanged("wrong"))
        viewModel.sendIntent(LoginUiIntent.SubmitLogin)

        advanceUntilIdle()

        assertTrue(fakeRepo.loginCalled)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Usuário ou senha incorretos", viewModel.uiState.value.errorMessage)

        Dispatchers.resetMain()
    }
}
