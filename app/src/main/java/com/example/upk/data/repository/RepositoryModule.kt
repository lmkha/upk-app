package com.example.upk.data.repository

import com.example.upk.data.repository.payment.PaymentRepository
import com.example.upk.data.repository.payment.PaymentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPaymentRepository(paymentRepositoryImpl: PaymentRepositoryImpl) : PaymentRepository
}