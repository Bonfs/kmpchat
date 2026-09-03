package com.bonfs.tcc.kmpchat.ui.screens.login

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.bonfs.tcc.kmpchat.core.mvi.BaseViewModel
import com.bonfs.tcc.kmpchat.core.util.Logger
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.MatrixAuthException
import com.bonfs.tcc.kmpchat.domain.usecase.LoginUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.RestoreSessionUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val logger: Logger
) : BaseViewModel<LoginUiState, LoginUiIntent, LoginUiEffect>(LoginUiState()) {

    private val TAG = "LoginViewModel"

    init {
        checkExistingSession()
    }

    override fun sendIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.HomeserverChanged -> onHomeserverChanged(intent.homeserver)
            is LoginUiIntent.UsernameChanged -> onUsernameChanged(intent.username)
            is LoginUiIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is LoginUiIntent.SubmitLogin -> onSubmitLogin()
            is LoginUiIntent.DismissError -> onDismissError()
            is LoginUiIntent.CheckExistingSession -> checkExistingSession()
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            updateState { it.copy(isCheckingSession = true) }
            val session = restoreSessionUseCase()
            if (session != null) {
                updateState { it.copy(isCheckingSession = false) }
                emitEffect(LoginUiEffect.NavigateToRooms)
            } else {
                updateState { it.copy(isCheckingSession = false) }
            }
        }
    }

    private fun onHomeserverChanged(homeserver: String) {
        updateState { it.copy(homeserver = homeserver, errorMessage = null) }
    }

    private fun onUsernameChanged(username: String) {
        updateState { it.copy(username = username, errorMessage = null) }
    }

    private fun onPasswordChanged(password: String) {
        updateState { it.copy(password = password, errorMessage = null) }
    }

    private fun onDismissError() {
        updateState { it.copy(errorMessage = null) }
    }

    private fun onSubmitLogin() {
        if (currentState.isLoading) return

        viewModelScope.launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            val credentials = LoginCredentials(
                homeserverUrl = currentState.homeserver,
                username = currentState.username,
                password = currentState.password
            )
            try {
                loginUseCase(credentials)
                updateState { it.copy(isLoading = false) }
                emitEffect(LoginUiEffect.NavigateToRooms)
            } catch (exception: MatrixAuthException) {
                logger.logWarn(TAG, exception) { "Matrix authentication failed" }
                handleLoginError(exception.message ?: "Falha na autenticação")
            } catch (exception: Exception) {
                logger.logWarn(TAG, exception) { "Unexpected error during login" }
                handleLoginError("Erro inesperado ao conectar ao servidor")
            }
        }
    }

    private fun handleLoginError(message: String) {
        updateState { it.copy(isLoading = false, errorMessage = message) }
        emitEffect(LoginUiEffect.ShowSnackbar(message))
    }
}
