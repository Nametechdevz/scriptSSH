package com.iptvapp.player.domain.repository

import com.iptvapp.player.domain.model.Result
import com.iptvapp.player.domain.model.UserSession

interface AuthRepository {
    suspend fun login(serverUrl: String, username: String, password: String): Result<UserSession>
    fun isLoggedIn(): Boolean
    fun logout()
    fun getSavedSession(): UserSession?
}
