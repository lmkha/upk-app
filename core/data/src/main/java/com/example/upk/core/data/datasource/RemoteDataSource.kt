package com.example.upk.core.data.datasource

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
        crossinline request: suspend () -> Response<ResponseBody>
    ): Result<T> {
        return try {
            val response = request()

            if (response.isSuccessful) {
                val successString = response.body()?.string()

                if (successString.isNullOrBlank()) {
                    return if (Unit is T) {
                        Result.success(Unit as T)
                    } else {
                        Result.failure(EmptyResponseBodyException(httpCode = response.code()))
                    }
                }

                val body = json.decodeFromString<ApiResponseBody.Success<T>>(successString)
                Result.success(body.data)

            } else {
                val errorString = response.errorBody()?.string()

                if (errorString.isNullOrBlank()) {
                    return Result.failure(
                        EmptyErrorBodyException(
                            httpCode = response.code(),
                            httpMessage = response.message()
                        )
                    )
                }

                val exception = try {
                    val errorBody = json.decodeFromString<ApiResponseBody.Failure>(errorString)
                    errorBody.toApiException()
                } catch (e: SerializationException) {
                    NonJsonFormatException(response.code(), errorString, e)
                }

                Result.failure(exception)
            }

        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.failure(e)
        }
    }

    protected fun ApiResponseBody.Failure.toApiException(): APIException {
        return APIException(
            code = this.code,
            message = this.message,
            fieldErrors = this.fieldErrors
        )
    }
}