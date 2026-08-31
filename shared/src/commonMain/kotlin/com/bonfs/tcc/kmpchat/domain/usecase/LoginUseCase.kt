package com.bonfs.tcc.kmpchat.domain.usecase

import com.bonfs.tcc.kmpchat.domain.model.EmptyFieldException
import com.bonfs.tcc.kmpchat.domain.model.LoginCredentials
import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(credentials: LoginCredentials): UserSession {
        validateCredentials(credentials)
        return authRepository.login(credentials)
    }

    private fun validateCredentials(credentials: LoginCredentials) {
        if (credentials.homeserverUrl.isBlank()) {
            throw EmptyFieldException("Homeserver")
        }
        if (credentials.username.isBlank()) {
            throw EmptyFieldException("Usuário")
        }
        if (credentials.password.isBlank()) {
            throw EmptyFieldException("Senha")
        }
    }
}
