package com.example.upk.payment.domain.repository

import com.example.upk.payment.domain.models.CreatePaymentBindingModel
import com.example.upk.payment.domain.models.PaymentProviderModel

interface PaymentRepository {
    suspend fun createBinding(): Result<CreatePaymentBindingModel>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderModel>>
}