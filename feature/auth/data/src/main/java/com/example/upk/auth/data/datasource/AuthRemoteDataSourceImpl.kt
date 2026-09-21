package com.example.upk.auth.data.datasource

import com.example.upk.auth.data.remote.api.AuthApi
import com.example.upk.auth.data.remote.dto.RefreshTokenDto
import com.example.upk.auth.data.remote.payloads.RefreshTokenRequest
import com.example.upk.core.data.datasource.RemoteDataSource
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSourceImpl @Inject constructor(
    private val authApi: AuthApi,
    json: Json
): RemoteDataSource(json), AuthRemoteDataSource {
    override suspend fun refreshToken(refreshToken: String): Result<RefreshTokenDto> {
        return callApi { authApi.refreshToken(RefreshTokenRequest(refreshToken)) }
    }
}