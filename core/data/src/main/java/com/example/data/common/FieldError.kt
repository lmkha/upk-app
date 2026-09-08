package com.example.data.common

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(
    val code: String?,
    val message: String?,
    val field: String?
)
