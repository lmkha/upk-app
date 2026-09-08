package com.example.data.common

import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResponseBody<out T> {
    @Serializable
    data class Success<T>(
        val data: T,
        val message: String? = null
    ): ApiResponseBody<T>()

    @Serializable
    data class Failure(
        val message: String,
        val code: String? = null,
        val fieldErrors: List<FieldError> = emptyList()
    ): ApiResponseBody<Nothing>()
}
