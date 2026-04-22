package com.iptvapp.player.data.repository

import com.iptvapp.player.data.preferences.EncryptedPreferencesManager
import com.iptvapp.player.data.remote.XtreamCodesApi
import com.iptvapp.player.domain.model.Result
import com.iptvapp.player.domain.model.UserSession
import com.iptvapp.player.domain.repository.AuthRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val prefs: EncryptedPreferencesManager,
    private val okHttpClient: OkHttpClient
) : AuthRepository {

    override suspend fun login(
        serverUrl: String,
        username: String,
        password: String
    ): Result<UserSession> = try {
        val normalizedUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        val api = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamCodesApi::class.java)

        val response = api.authenticate(username, password)

        if (response.userInfo.status == "Active" || response.userInfo.status == "active") {
            prefs.saveCredentials(normalizedUrl, username, password)
            Result.Success(
                UserSession(
                    username = response.userInfo.username,
                    serverUrl = normalizedUrl,
                    status = response.userInfo.status,
                    expDate = response.userInfo.expDate,
                    maxConnections = response.userInfo.maxConnections.toIntOrNull() ?: 1,
                    isTrial = response.userInfo.isTrial == "1"
                )
            )
        } else {
            Result.Error("Account is not active. Status: ${response.userInfo.status}")
        }
    } catch (e: Exception) {
        Result.Error("Connection failed: ${e.message}", e)
    }

    override fun isLoggedIn(): Boolean = prefs.isLoggedIn()

    override fun logout() = prefs.clearAll()

    override fun getSavedSession(): UserSession? {
        if (!prefs.isLoggedIn()) return null
        return UserSession(
            username = prefs.getUsername() ?: "",
            serverUrl = prefs.getServerUrl() ?: "",
            status = "Active",
            expDate = null,
            maxConnections = 1,
            isTrial = false
        )
    }
}
