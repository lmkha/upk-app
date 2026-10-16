package com.example.upk.auth.data.remote.datasource

import com.example.upk.auth.data.remote.dto.RefreshTokenDto

interface AuthRemoteDataSource {
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenDto>
}