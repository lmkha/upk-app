package com.example.androidspace.data.remote.datasource

import com.example.androidspace.data.remote.common.ApiResponseBody
import com.example.androidspace.data.remote.common.toApiException
import com.example.androidspace.data.remote.dto.ApiResponseData
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response

abstract class RemoteDataSource(val json: Json) {

    protected suspend inline fun <reified T : ApiResponseData> callApi(
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
}