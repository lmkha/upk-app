package com.example.upk.auth.data.datasource.impl

import androidx.datastore.core.DataStore
import com.example.upk.auth.data.AuthPreferences
import com.example.upk.auth.data.copy
import com.example.upk.auth.data.datasource.AuthLocalDataSource
import com.example.upk.core.data.datasource.LocalDataSource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthLocalDataSourceImpl @Inject constructor(
    private val authDataStore: DataStore<AuthPreferences>
) : LocalDataSource(), AuthLocalDataSource {

    override suspend fun getAccessToken(): String? {
        val prefs = authDataStore.data.firstOrNull()
        return prefs?.sessionsMap?.get(prefs.activeUserId)?.accessToken
    }

    override suspend fun getRefreshToken(): String? {
        val prefs = authDataStore.data.firstOrNull()
        return prefs?.sessionsMap?.get(prefs.activeUserId)?.refreshToken
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        authDataStore.updateData { currentPrefs ->
            val activeUserId = currentPrefs.activeUserId
            val currentSession = currentPrefs.sessionsMap[activeUserId] ?: return@updateData currentPrefs

            val updatedSession = currentSession.copy {
                this.accessToken = accessToken
                this.refreshToken = refreshToken
            }
            currentPrefs.copy {
                sessions.put(activeUserId, updatedSession)
            }
        }
    }

    override suspend fun clearAll() {
        authDataStore.updateData { AuthPreferences.getDefaultInstance() }
    }
}