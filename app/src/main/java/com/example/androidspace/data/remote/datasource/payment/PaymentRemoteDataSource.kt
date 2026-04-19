package com.example.androidspace.data.remote.datasource.payment

import com.example.androidspace.data.remote.dto.CreatePaymentBindingDto

interface PaymentRemoteDataSource {
    suspend fun createBinding() : Result<CreatePaymentBindingDto>
}