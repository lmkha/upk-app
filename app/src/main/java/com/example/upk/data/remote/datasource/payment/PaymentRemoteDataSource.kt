package com.example.upk.data.remote.datasource.payment

import com.example.upk.data.remote.dto.PaymentProviderDto
import com.example.upk.data.remote.dto.payment.CreatePaymentBindingDto

interface PaymentRemoteDataSource {
    suspend fun createBinding(): Result<CreatePaymentBindingDto>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderDto>>
}