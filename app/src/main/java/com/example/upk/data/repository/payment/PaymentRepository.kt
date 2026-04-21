package com.example.upk.data.repository.payment

import com.example.upk.domain.models.CreatePaymentBindingModel
import com.example.upk.domain.models.PaymentProviderModel

interface PaymentRepository {
    suspend fun createBinding(): Result<CreatePaymentBindingModel>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderModel>>
}