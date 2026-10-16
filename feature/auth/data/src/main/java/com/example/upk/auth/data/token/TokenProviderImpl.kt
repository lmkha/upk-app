package com.example.upk.auth.data.token

import androidx.datastore.core.DataStore
import com.example.upk.auth.data.AuthPreferences
import com.example.upk.auth.data.local.datasource.AuthLocalDataSource
import com.example.upk.auth.data.remote.datasource.AuthRemoteDataSource
import com.example.upk.core.data.remote.TokenProvider
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProviderImpl @Inject constructor(
    private val authDataStore: DataStore<AuthPreferences>,
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) : TokenProvider {

    override fun getAccessToken(): String? {
        return runBlocking { authLocalDataSource.getAccessToken() }
    }

    override fun refreshToken(): String? {
        return runBlocking {
            val currentRefreshToken = authLocalDataSource.getRefreshToken()
            // If the refresh token is missing or empty, abort the API call
            if (currentRefreshToken.isNullOrEmpty()) return@runBlocking null

            val result = authRemoteDataSource.refreshToken(currentRefreshToken)
            result.fold(
                onSuccess = { dto ->
                    authLocalDataSource.saveTokens(dto.accessToken, dto.refreshToken)
                    return@runBlocking dto.accessToken
                },
                // Refresh failed (e.g., the refresh token is also expired or revoked)
                // Returning null triggers clearTokens() in the calling Authenticator
                onFailure = { return@runBlocking null }
            )
        }
    }

    override fun clearTokens() {
        runBlocking { authDataStore.updateData { AuthPreferences.getDefaultInstance() } }
    }
}