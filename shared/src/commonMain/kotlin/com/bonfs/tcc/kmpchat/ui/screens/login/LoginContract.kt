package com.bonfs.tcc.kmpchat.ui.screens.login

import com.bonfs.tcc.kmpchat.core.mvi.UiEffect
import com.bonfs.tcc.kmpchat.core.mvi.UiIntent
import com.bonfs.tcc.kmpchat.core.mvi.UiState

data class LoginUiState(
    val homeserver: String = "https://matrix.org",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface LoginUiIntent : UiIntent {
    data class HomeserverChanged(val homeserver: String) : LoginUiIntent
    data class UsernameChanged(val username: String) : LoginUiIntent
    data class PasswordChanged(val password: String) : LoginUiIntent
    data object SubmitLogin : LoginUiIntent
    data object DismissError : LoginUiIntent
}

sealed interface LoginUiEffect : UiEffect {
    data object NavigateToRooms : LoginUiEffect
    data class ShowSnackbar(val message: String) : LoginUiEffect
}
