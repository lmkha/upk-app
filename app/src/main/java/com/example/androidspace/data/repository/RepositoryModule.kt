package com.example.androidspace.data.repository

import com.example.androidspace.data.repository.payment.PaymentRepository
import com.example.androidspace.data.repository.payment.PaymentRepositoryImpl
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