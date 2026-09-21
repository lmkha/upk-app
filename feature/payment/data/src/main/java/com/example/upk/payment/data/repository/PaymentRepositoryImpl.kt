package com.example.upk.payment.data.repository

import com.example.upk.core.data.repository.BaseRepository
import com.example.upk.payment.data.datasource.PaymentRemoteDataSource
import com.example.upk.payment.data.dto.PaymentProviderDto
import com.example.upk.payment.data.dto.toModel
import com.example.upk.payment.domain.models.CreatePaymentBindingModel
import com.example.upk.payment.domain.models.PaymentProviderModel
import com.example.upk.payment.domain.repository.PaymentRepository
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