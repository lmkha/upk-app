package com.example.upk.core.data.remote

import okhttp3.Headers

/**
 * Represents a successful HTTP response after decoding the [com.example.upk.core.common.network.ApiResponseBody.Success]
 * JSON envelope.
 *
 * @param T The type of the decoded payload [data].
 * @property data The unwrapped payload from the response envelope (or [Unit] when the body is empty).
 * @property message Optional human-readable message included in the response envelope.
 * @property code The HTTP status code (e.g., `200`, `201`, `204`).
 * @property headers The HTTP response headers (e.g., `Location`, `ETag`, `X-Total-Count`).
 */
data class ApiResponse<out T>(
    val data: T,
    val message: String? = null,
    val code: Int,
    val headers: Headers = Headers.EMPTY,
)
