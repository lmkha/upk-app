package com.example.upk.core.common.network

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(
    val field: String?,
    val message: String?
)