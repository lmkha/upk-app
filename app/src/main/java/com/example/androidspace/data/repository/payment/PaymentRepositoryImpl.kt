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
        val result = paymentRemoteDataSource.createBinding()
        return result.fold(
            onSuccess = { dto->
                val model = dto.toDomain()
                Result.success(model)
            },
            onFailure = { throwable ->
                Result.failure(throwable)
            }
        )
    }
}