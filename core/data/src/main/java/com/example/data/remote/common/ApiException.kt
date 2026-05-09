package com.example.data.remote.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiException(
    val errors: List<ApiError>
): Exception()
