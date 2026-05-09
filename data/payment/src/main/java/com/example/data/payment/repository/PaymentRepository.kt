package com.example.data.payment.repository

import com.example.data.payment.models.CreatePaymentBindingModel
import com.example.data.payment.models.PaymentProviderModel

interface PaymentRepository {
    suspend fun createBinding(): Result<CreatePaymentBindingModel>
    suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderModel>>
}