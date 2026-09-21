package com.example.upk.core.data.remote

interface TokenProvider {
    fun getAccessToken(): String?
    fun refreshToken(): String?
    fun clearTokens()
}
