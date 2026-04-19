package com.example.androidspace.data.remote.datasource.payment

import com.example.androidspace.data.remote.dto.PaymentProviderDto
import com.example.androidspace.data.remote.dto.payment.CreatePaymentBindingDto

interface PaymentRemoteDataSource {
    suspend fun createBinding() : Result<CreatePaymentBindingDto>
    suspend fun getAvailablePaymentProviders() : Result<List<PaymentProviderDto>>
}