package com.example.upk.core.data.remote

import okhttp3.Headers

data class ApiResponse<out T>(
    val data: T,
    val message: String? = null,
    val code: Int,
    val headers: Headers = Headers.EMPTY,
)
