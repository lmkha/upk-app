package com.example.androidspace.data.repository.payment

import com.example.androidspace.data.remote.datasource.payment.PaymentRemoteDataSource
import com.example.androidspace.data.remote.dto.toDomain
import com.example.androidspace.models.CreatePaymentBindingModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource
) : PaymentRepository {
    override suspend fun createBinding(): Result<CreatePaymentBindingModel> {
        return paymentRemoteDataSource.createBinding().map { dto -> dto.toDomain() }
    }
}