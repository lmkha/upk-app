package com.example.upk.auth.data

import androidx.datastore.core.DataStore
import com.example.upk.auth.data.datasource.AuthRemoteDataSource
import com.example.upk.core.data.remote.TokenProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProviderImpl @Inject constructor(
    private val authDataStore: DataStore<AuthPreferences>,
    private val authRemoteDataSource: AuthRemoteDataSource,
) : TokenProvider {

    override fun getAccessToken(): String? {
        return runBlocking {
            authDataStore.data.firstOrNull()?.let { prefs ->
                prefs.sessionsMap[prefs.activeUserId]?.accessToken
            }
        }
    }

    override fun refreshToken(): String? {
        return runBlocking {
            val prefs = authDataStore.data.firstOrNull() ?: return@runBlocking null
            val activeUserId = prefs.activeUserId
            val currentSession = prefs.sessionsMap[activeUserId] ?: return@runBlocking null
            val currentRefreshToken = currentSession.refreshToken

            // If the refresh token is missing or empty, abort the API call
            if (currentRefreshToken.isEmpty()) {
                return@runBlocking null
            }

            val result = authRemoteDataSource.refreshToken(currentRefreshToken)

            result.fold(
                onSuccess = { dto ->
                    authDataStore.updateData { currentPrefs ->
                        val updatedSession = currentSession.copy {
                            accessToken = dto.accessToken
                            refreshToken = dto.refreshToken
                        }

                        currentPrefs.copy {
                            sessions.put(activeUserId, updatedSession)
                        }
                    }
                    return@runBlocking dto.accessToken
                },
                onFailure = {
                    // Refresh failed (e.g., the refresh token is also expired or revoked)
                    // Returning null triggers clearTokens() in the calling Authenticator
                    return@runBlocking null
                }
            )
        }
    }

    override fun clearTokens() {
        runBlocking { authDataStore.updateData { AuthPreferences.getDefaultInstance() } }
    }
}