package com.example.upk.core.data.remote.interceptor

import com.example.upk.core.data.remote.TokenProvider
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops if the server consistently returns a 401 error
        if (generateSequence(response) { it.priorResponse }.count() > 2) {
            return null
        }

        // Lock the block to prevent race conditions and ensure only one thread refreshes the token at a time
        synchronized(this) {
            // Check if the token has already been refreshed by another thread
            val currentToken = tokenProvider.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            val newToken = if (currentToken != null && currentToken != requestToken) {
                // The token was already refreshed by a concurrent API call, just use it
                currentToken
            } else {
                // Perform the actual Refresh Token API call (runs synchronously)
                tokenProvider.refreshToken()
            }

            // If the refresh fails (e.g., the refresh token is also expired)
            if (newToken == null) {
                tokenProvider.clearTokens()
                // Typically, emit an event here (EventBus/Flow) to force the UI back to the Login screen
                return null
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }
}