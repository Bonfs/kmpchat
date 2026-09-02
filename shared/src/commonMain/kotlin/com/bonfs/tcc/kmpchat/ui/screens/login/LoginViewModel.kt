package com.bonfs.tcc.kmpchat.ui.screens.login

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

import com.bonfs.tcc.kmpchat.core.mvi.BaseViewModel
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.MatrixAuthException
import com.bonfs.tcc.kmpchat.domain.usecase.LoginUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginUiState, LoginUiIntent, LoginUiEffect>(LoginUiState()) {

    private val logger = Logger.withTag("LoginViewModel")

    override fun sendIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.HomeserverChanged -> onHomeserverChanged(intent.homeserver)
            is LoginUiIntent.UsernameChanged -> onUsernameChanged(intent.username)
            is LoginUiIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is LoginUiIntent.SubmitLogin -> onSubmitLogin()
            is LoginUiIntent.DismissError -> onDismissError()
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
                logger.w(exception) { "Matrix authentication failed" }
                handleLoginError(exception.message ?: "Falha na autenticação")
            } catch (exception: Exception) {
                logger.e(exception) { "Unexpected error during login" }
                handleLoginError("Erro inesperado ao conectar ao servidor")
            }
        }
    }

    private fun handleLoginError(message: String) {
        updateState { it.copy(isLoading = false, errorMessage = message) }
        emitEffect(LoginUiEffect.ShowSnackbar(message))
    }
}
