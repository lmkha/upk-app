package com.example.androidspace.data.repository

import com.example.androidspace.data.remote.common.ApiResponseBody
import com.example.androidspace.data.remote.datasource.PaymentRemoteDataSource
import com.example.androidspace.data.remote.dto.CreatePaymentBindingDto
import com.example.androidspace.models.CreatePaymentBindingModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource
) {
    suspend fun createBinding() : CreatePaymentBindingModel? {
        val result : Result<ApiResponseBody<CreatePaymentBindingDto>> = paymentRemoteDataSource.createBinding()
        val body = result.getOrNull()
        if (result.isSuccess && body is ApiResponseBody.Success) {
            val data = body.data
            return CreatePaymentBindingModel(
                customerId = data.customerId,
                clientSecret = data.clientSecret,
                ephemeralKeySecret = data.ephemeralKeySecret
            )
        }
        return null
    }
}