package com.example.upk.payment.data.datasource

import com.example.upk.core.data.remote.RemoteDataSource
import com.example.upk.payment.data.api.PaymentApi
import com.example.upk.payment.data.dto.PaymentProviderDto
import com.example.upk.payment.data.dto.CreateBindingRequest
import com.example.upk.payment.data.dto.CreatePaymentBindingDto
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