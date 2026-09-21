package com.example.upk.core.common.network

import kotlinx.serialization.Serializable

@Serializable
data class APIException(
    val code: String? = null,
    override val message: String? = null,
    val fieldErrors: List<FieldError>
): Exception()
