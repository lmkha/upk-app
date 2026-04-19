package com.example.androidspace.data.repository.payment

import com.example.androidspace.data.remote.datasource.payment.PaymentRemoteDataSource
import com.example.androidspace.data.remote.dto.CreatePaymentBindingDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource
) : PaymentRepository {
    override suspend fun createBinding(): Result<CreatePaymentBindingDto> {
        return paymentRemoteDataSource.createBinding()
    }
}