package com.iptvapp.player.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(serverUrl: String, username: String, password: String) {
        prefs.edit()
            .putString(KEY_SERVER_URL, serverUrl)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun getServerUrl(): String? = prefs.getString(KEY_SERVER_URL, null)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)

    fun isLoggedIn(): Boolean =
        !getServerUrl().isNullOrBlank() && !getUsername().isNullOrBlank() && !getPassword().isNullOrBlank()

    fun saveAdminPin(pin: String) = prefs.edit().putString(KEY_ADMIN_PIN, pin).apply()
    fun getAdminPin(): String? = prefs.getString(KEY_ADMIN_PIN, DEFAULT_ADMIN_PIN)

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val FILE_NAME = "iptv_secure_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val DEFAULT_ADMIN_PIN = "1234"
    }
}
