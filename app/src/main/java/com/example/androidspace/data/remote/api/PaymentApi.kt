package com.example.androidspace.data.remote.api

import com.example.androidspace.data.remote.dto.CreateBindingRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {
    @POST("Payment/create/binding")
    suspend fun createBinding(
        @Body request: CreateBindingRequest
    ): Response<ResponseBody>
}