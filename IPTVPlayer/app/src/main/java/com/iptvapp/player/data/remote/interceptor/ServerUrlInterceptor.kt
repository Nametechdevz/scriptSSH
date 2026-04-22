package com.iptvapp.player.data.remote.interceptor

import com.iptvapp.player.data.preferences.EncryptedPreferencesManager
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ServerUrlInterceptor @Inject constructor(
    private val prefs: EncryptedPreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val serverUrl = prefs.getServerUrl()
        val request = chain.request()

        if (serverUrl.isNullOrBlank()) return chain.proceed(request)

        return try {
            val newUrl = request.url.newBuilder()
                .scheme(serverUrl.toHttpUrl().scheme)
                .host(serverUrl.toHttpUrl().host)
                .port(serverUrl.toHttpUrl().port)
                .build()

            chain.proceed(request.newBuilder().url(newUrl).build())
        } catch (e: Exception) {
            chain.proceed(request)
        }
    }
}
