package com.example.upk.data.repository.payment

import com.example.upk.data.remote.datasource.payment.PaymentRemoteDataSource
import com.example.upk.data.remote.dto.PaymentProviderDto
import com.example.upk.data.remote.dto.payment.toDomain
import com.example.upk.data.remote.dto.toDomain
import com.example.upk.domain.models.CreatePaymentBindingModel
import com.example.upk.domain.models.PaymentProviderModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource
): PaymentRepository {
    override suspend fun createBinding(): Result<CreatePaymentBindingModel> {
        return paymentRemoteDataSource.createBinding().map { dto -> dto.toDomain() }
    }

    override suspend fun getAvailablePaymentProviders(): Result<List<PaymentProviderModel>> {
        return paymentRemoteDataSource.getAvailablePaymentProviders().map { list ->
            list.map(PaymentProviderDto::toDomain)
        }
    }
}