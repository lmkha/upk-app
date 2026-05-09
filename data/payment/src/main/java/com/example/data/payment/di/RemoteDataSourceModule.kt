package com.example.data.payment.di

import com.example.data.payment.datasource.PaymentRemoteDataSource
import com.example.data.payment.datasource.PaymentRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindPaymentRemoteDataSource(paymentRemoteDataSourceImpl: PaymentRemoteDataSourceImpl): PaymentRemoteDataSource
}