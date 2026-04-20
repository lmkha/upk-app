package com.example.androidspace.data.remote.api

import com.example.androidspace.data.remote.dto.payment.CreateBindingRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentApi {
    companion object { const val BASE_PATH = "dev/payment/api/Payment" }

    @POST("$BASE_PATH/create/binding")
    suspend fun createBinding(
        @Body request: CreateBindingRequest
    ): Response<ResponseBody>

    @GET("$BASE_PATH/providers/available")
    suspend fun getAvailablePaymentProviders(): Response<ResponseBody>
}