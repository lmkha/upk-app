package com.example.androidspace.data.remote.datasource

import com.example.androidspace.data.remote.api.PaymentApi
import com.example.androidspace.data.remote.common.ApiResponseBody
import com.example.androidspace.data.remote.dto.CreateBindingRequest
import com.example.androidspace.data.remote.dto.CreatePaymentBindingDto
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRemoteDataSource @Inject constructor(
    private val paymentApi: PaymentApi,
    json: Json
) : RemoteDataSource(json) {
    suspend fun createBinding() : Result<ApiResponseBody<CreatePaymentBindingDto>> {
        return callApi { paymentApi.createBinding(CreateBindingRequest()) }
    }
}
