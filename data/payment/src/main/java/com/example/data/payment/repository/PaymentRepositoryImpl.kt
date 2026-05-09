package com.example.data.payment.repository

import com.example.data.repository.BaseRepository
import com.example.data.payment.datasource.PaymentRemoteDataSource
import com.example.data.payment.dto.PaymentProviderDto
import com.example.data.payment.dto.toModel
import com.example.data.payment.models.CreatePaymentBindingModel
import com.example.data.payment.models.PaymentProviderModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource
): BaseRepository(), PaymentRepository {
    override suspend fun createBinding(): Result<CreatePaymentBindingModel> {
        return paymentRemoteDataSource.createBinding().map { dto -> dto.toModel() }
    }

    override suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderModel>> {
        return paymentRemoteDataSource.getAvailablePaymentProviders().map { list ->
            list.map(PaymentProviderDto::toModel)
        }
    }
}