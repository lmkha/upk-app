package com.example.androidspace.data.remote.datasource

import com.example.androidspace.data.remote.datasource.payment.PaymentRemoteDataSource
import com.example.androidspace.data.remote.datasource.payment.PaymentRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindPaymentRemoteDataSource(paymentRemoteDataSourceImpl: PaymentRemoteDataSourceImpl) : PaymentRemoteDataSource
}