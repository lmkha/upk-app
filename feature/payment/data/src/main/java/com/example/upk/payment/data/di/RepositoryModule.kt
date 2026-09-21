package com.example.upk.payment.data.di

import com.example.upk.payment.data.repository.PaymentRepositoryImpl
import com.example.upk.payment.domain.repository.PaymentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPaymentRepository(paymentRepositoryImpl: PaymentRepositoryImpl): PaymentRepository
}