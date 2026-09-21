package com.example.upk.auth.data.datasource

import com.example.upk.auth.data.remote.dto.RefreshTokenDto

interface AuthRemoteDataSource {
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenDto>
}