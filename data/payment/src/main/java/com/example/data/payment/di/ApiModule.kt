package com.example.data.payment.di

import com.example.data.UnauthenticatedRetrofit
import com.example.data.payment.api.PaymentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun providePaymentApi(@UnauthenticatedRetrofit retrofit: Retrofit): PaymentApi {
        return retrofit.create(PaymentApi::class.java)
    }
}