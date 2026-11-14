package com.example.upk.core.common.network

data class ApiException(
    val code: String? = null,
    override val message: String? = null,
    val fieldErrors: List<FieldError> = emptyList(),
) : Exception(message)

