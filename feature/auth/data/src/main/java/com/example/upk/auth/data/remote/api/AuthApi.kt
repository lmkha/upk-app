package com.example.upk.auth.data.remote.api

import com.example.upk.auth.data.remote.payloads.RefreshTokenRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    companion object { const val BASE_PATH = "dev/auth/api/Auth" }

    @POST("$BASE_PATH/refresh-token")
    suspend fun refreshToken(
        @Body requestBody: RefreshTokenRequest
    ): Response<ResponseBody>
}