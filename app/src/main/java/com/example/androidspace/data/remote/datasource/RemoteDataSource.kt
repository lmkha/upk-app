package com.example.androidspace.data.remote.datasource

import com.example.androidspace.data.remote.common.ApiResponseBody
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response

abstract class RemoteDataSource(val json: Json) {

    protected suspend inline fun <reified T> callApi(
        crossinline request: suspend () -> Response<ResponseBody>
    ): Result<ApiResponseBody<T>> {
        return try {
            val response = request()

            val responseBody: ApiResponseBody<T> = if (response.isSuccessful) {
                val successString = response.body()?.string() ?: ""
                json.decodeFromString<ApiResponseBody.Success<T>>(successString)
            } else {
                val errorString = response.errorBody()?.string() ?: ""
                json.decodeFromString<ApiResponseBody.Failure>(errorString)
            }

            Result.success(responseBody)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}