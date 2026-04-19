package com.example.androidspace.data.remote.datasource.payment

import com.example.androidspace.data.remote.api.PaymentApi
import com.example.androidspace.data.remote.datasource.RemoteDataSource
import com.example.androidspace.data.remote.dto.PaymentProviderDto
import com.example.androidspace.data.remote.dto.payment.CreateBindingRequest
import com.example.androidspace.data.remote.dto.payment.CreatePaymentBindingDto
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRemoteDataSourceImpl @Inject constructor(
    private val paymentApi: PaymentApi,
    json: Json
) : RemoteDataSource(json), PaymentRemoteDataSource {
    override suspend fun createBinding() : Result<CreatePaymentBindingDto> {
        return callApi { paymentApi.createBinding(CreateBindingRequest()) }
    }

    override suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderDto>> {
        return callApi { paymentApi.getAvailablePaymentProviders() }
    }
}