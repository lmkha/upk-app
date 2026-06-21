package com.example.data.payment.datasource

import com.example.data.datasource.RemoteDataSource
import com.example.data.payment.api.PaymentApi
import com.example.data.payment.dto.PaymentProviderDto
import com.example.data.payment.dto.CreateBindingRequest
import com.example.data.payment.dto.CreatePaymentBindingDto
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRemoteDataSourceImpl @Inject constructor(
    private val paymentApi: PaymentApi,
    json: Json
): RemoteDataSource(json), PaymentRemoteDataSource {
    override suspend fun createBinding(): Result<CreatePaymentBindingDto> {
        return callApi { paymentApi.createBinding(CreateBindingRequest()) }
    }

    override suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderDto>> {
        return callApi { paymentApi.getAvailablePaymentProviders() }
    }
}