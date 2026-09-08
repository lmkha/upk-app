package com.example.data.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiException(
    val errors: List<FieldError>
): Exception()
