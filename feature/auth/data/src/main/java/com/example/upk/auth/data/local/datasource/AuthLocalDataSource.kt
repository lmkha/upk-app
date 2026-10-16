package com.example.upk.auth.data.local.datasource

interface AuthLocalDataSource {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearAll()
}