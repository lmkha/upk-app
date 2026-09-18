package com.example.data.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiException(
    val code: String? = null,
    override val message: String? = null,
    val fieldErrors: List<FieldError>
): Exception()
