package com.example.androidspace.data.repository.payment

import com.example.androidspace.models.CreatePaymentBindingModel

interface PaymentRepository {
    suspend fun createBinding(): Result<CreatePaymentBindingModel>
}