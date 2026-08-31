package com.bonfs.tcc.kmpchat.domain.usecase

import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke() {
        authRepository.logout()
    }
}
