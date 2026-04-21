package com.example.upk.data.remote.common

import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResponseBody<out T> {
    @Serializable
    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : ApiResponseBody<T>()

    @Serializable
    data class Failure(
        val errors: List<ApiError>
    ) : ApiResponseBody<Nothing>()
}
