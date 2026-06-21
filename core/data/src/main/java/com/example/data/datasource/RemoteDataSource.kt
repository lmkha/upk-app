package com.example.data.datasource

import com.example.data.common.ApiException
import com.example.data.common.ApiResponseBody
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
                val successString = response.body()?.string() ?: ""
                val body = json.decodeFromString<ApiResponseBody.Success<T>>(successString)
                return Result.success(body.data)
            } else {
                val errorString = response.errorBody()?.string() ?: ""
                val body = json.decodeFromString<ApiResponseBody.Failure>(errorString)
                val exception = body.toApiException()
                return Result.failure(exception)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    protected fun ApiResponseBody.Failure.toApiException(): ApiException {
        return ApiException(
            errors = this.errors
        )
    }
}