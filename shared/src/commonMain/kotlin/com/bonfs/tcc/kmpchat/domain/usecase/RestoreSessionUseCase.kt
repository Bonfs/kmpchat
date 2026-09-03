package com.bonfs.tcc.kmpchat.domain.usecase

import com.bonfs.tcc.kmpchat.domain.model.UserSession
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository

class RestoreSessionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserSession? {
        return authRepository.restoreSession()
    }
}
