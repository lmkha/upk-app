package com.example.data.payment.datasource

import com.example.data.payment.dto.PaymentProviderDto
import com.example.data.payment.dto.CreatePaymentBindingDto

interface PaymentRemoteDataSource {
    suspend fun createBinding(): Result<CreatePaymentBindingDto>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderDto>>
}