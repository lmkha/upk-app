package com.example.upk.payment.data.datasource

import com.example.upk.payment.data.dto.PaymentProviderDto
import com.example.upk.payment.data.dto.CreatePaymentBindingDto

interface PaymentRemoteDataSource {
    suspend fun createBinding(): Result<CreatePaymentBindingDto>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderDto>>
}