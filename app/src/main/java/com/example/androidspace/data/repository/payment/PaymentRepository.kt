package com.example.androidspace.data.repository.payment

import com.example.androidspace.data.remote.dto.CreatePaymentBindingDto

interface PaymentRepository {
    suspend fun createBinding(): Result<CreatePaymentBindingDto>
}