package com.example.upk.core.data.remote

import com.example.upk.core.common.errors.EmptyErrorBodyException
import com.example.upk.core.common.errors.EmptyResponseBodyException
import com.example.upk.core.common.errors.NonJsonFormatException
import com.example.upk.core.common.network.ApiException
import com.example.upk.core.common.network.ApiResponseBody
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Base class for Retrofit remote data sources that standardizes JSON envelope decoding,
 * HTTP error mapping, and coroutine cancellation handling.
 *
 * Choose the execution method that matches the level of response detail required:
 * - [execute]: Default choice when only the unwrapped payload `T` is needed.
 * - [executeForResponse]: When HTTP metadata (headers such as `Location` or `ETag`, status code,
 *   or envelope message) is needed alongside the decoded payload `T`.
 * - [executeRaw]: When the endpoint does not return the standard [ApiResponseBody] JSON envelope
 *   (e.g., file downloads, binary streams, or plain text).
 */
abstract class RemoteDataSource(protected val json: Json) {

    /**
     * Executes [request], decodes the [ApiResponseBody.Success] envelope, and returns the unwrapped
     * payload [T].
     *
     * Pass [Unit] as [T] for endpoints that succeed with an empty response body (e.g., HTTP 204).
     * If HTTP headers or status codes are required, use [executeForResponse] instead.
     */
    protected suspend inline fun <reified T> execute(
        crossinline request: suspend () -> Response<ResponseBody>,
    ): Result<T> = executeForResponse<T>(request).map { it.data }

    /**
     * Executes [request], decodes the [ApiResponseBody.Success] envelope, and returns the full
     * [ApiResponse] containing [ApiResponse.data], [ApiResponse.message], [ApiResponse.code],
     * and [ApiResponse.headers].
     *
     * Use this when response headers (e.g., `Location`, `ETag`, `X-Total-Count`) or the HTTP status
     * code must be inspected. Pass [Unit] as [T] when the response body is empty (e.g., HTTP 201
     * returning only a `Location` header).
     */
    protected suspend inline fun <reified T> executeForResponse(
        crossinline request: suspend () -> Response<ResponseBody>,
    ): Result<ApiResponse<T>> {
        return executeRaw(request).mapCatching { response ->
            val successString = response.body()?.string()

            if (successString.isNullOrBlank()) {
                if (Unit is T) {
                    ApiResponse(
                        data = Unit as T,
                        code = response.code(),
                        headers = response.headers(),
                    )
                } else {
                    throw EmptyResponseBodyException(httpCode = response.code())
                }
            } else {
                val body = json.decodeFromString<ApiResponseBody.Success<T>>(successString)
                ApiResponse(
                    data = body.data,
                    message = body.message,
                    code = response.code(),
                    headers = response.headers(),
                )
            }
        }
    }

    /**
     * Executes [request] and returns the raw Retrofit [Response] without decoding the body as JSON.
     *
     * Non-2xx responses are still parsed into [ApiException], [EmptyErrorBodyException], or
     * [NonJsonFormatException], and coroutine cancellation is preserved.
     */
    protected suspend inline fun executeRaw(
        crossinline request: suspend () -> Response<ResponseBody>,
    ): Result<Response<ResponseBody>> {
        return try {
            val response = request()

            if (response.isSuccessful) {
                Result.success(response)
            } else {
                Result.failure(parseError(response))
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.failure(e)
        }
    }

    /**
     * Parses a non-2xx [response] error body into an [ApiException], [EmptyErrorBodyException],
     * or [NonJsonFormatException].
     */
    protected fun parseError(response: Response<ResponseBody>): Exception {
        val errorString = response.errorBody()?.string()

        if (errorString.isNullOrBlank()) {
            return EmptyErrorBodyException(
                httpCode = response.code(),
                httpMessage = response.message(),
            )
        }

        return try {
            val errorBody = json.decodeFromString<ApiResponseBody.Failure>(errorString)
            errorBody.toApiException()
        } catch (e: SerializationException) {
            NonJsonFormatException(response.code(), errorString, e)
        }
    }

    /**
     * Maps a decoded [ApiResponseBody.Failure] envelope to an [ApiException].
     */
    protected fun ApiResponseBody.Failure.toApiException(): ApiException {
        return ApiException(
            code = this.code,
            message = this.message,
            fieldErrors = this.fieldErrors,
        )
    }
}