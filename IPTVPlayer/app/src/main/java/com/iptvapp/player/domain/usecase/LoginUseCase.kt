package com.iptvapp.player.domain.usecase

import com.iptvapp.player.domain.model.Result
import com.iptvapp.player.domain.model.UserSession
import com.iptvapp.player.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String
    ): Result<UserSession> {
        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            return Result.Error("All fields are required")
        }
        return authRepository.login(serverUrl.trim(), username.trim(), password.trim())
    }
}
