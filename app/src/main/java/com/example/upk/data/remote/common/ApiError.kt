package com.example.upk.data.remote.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String?,
    val message: String?,
    val field: String?
)
