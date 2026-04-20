package com.example.androidspace.data.repository.payment

import com.example.androidspace.domain.models.CreatePaymentBindingModel
import com.example.androidspace.domain.models.PaymentProviderModel

interface PaymentRepository {
    suspend fun createBinding(): Result<CreatePaymentBindingModel>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderModel>>
}