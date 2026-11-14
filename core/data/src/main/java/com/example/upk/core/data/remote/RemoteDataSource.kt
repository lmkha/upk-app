package com.example.upk.core.data.remote

import com.example.upk.core.common.errors.EmptyErrorBodyException
import com.example.upk.core.common.errors.EmptyResponseBodyException
import com.example.upk.core.common.errors.NonJsonFormatException
import com.example.upk.core.common.network.APIException
import com.example.upk.core.common.network.ApiResponseBody
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response

abstract class RemoteDataSource(protected val json: Json) {

    protected suspend inline fun <reified T> callApi(
        crossinline request: suspend () -> Response<ResponseBody>,
    ): Result<T> = callApiResponse<T>(request).map { it.data }

    protected suspend inline fun <reified T> callApiResponse(
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

    protected suspend inline fun executeRaw(
        crossinline request: suspend () -> Response<ResponseBody>,
    ): Result<Response<ResponseBody>> {
        return try {
            val response = request()

            if (response.isSuccessful) {
                Result.success(response)
            } else {
                Result.failure(parseErrorResponse(response))
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.failure(e)
        }
    }

    protected fun parseErrorResponse(response: Response<ResponseBody>): Exception {
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

    protected fun ApiResponseBody.Failure.toApiException(): APIException {
        return APIException(
            code = this.code,
            message = this.message,
            fieldErrors = this.fieldErrors,
        )
    }
}