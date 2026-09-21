package com.example.upk.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenDto(
    val accessToken: String,
    val refreshToken: String
)