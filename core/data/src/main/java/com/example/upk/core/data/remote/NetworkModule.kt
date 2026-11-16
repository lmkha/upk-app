package com.example.upk.core.data.remote

import com.example.upk.core.data.remote.interceptor.AcceptLanguageInterceptor
import com.example.upk.core.data.remote.interceptor.AuthInterceptor
import com.example.upk.core.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @AuthenticatedOkHttpClient
    @Provides
    @Singleton
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        acceptLanguageInterceptor: AcceptLanguageInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(acceptLanguageInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @UnauthenticatedOkHttpClient
    @Provides
    @Singleton
    fun provideUnauthenticatedOkHttpClient(
        acceptLanguageInterceptor: AcceptLanguageInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(acceptLanguageInterceptor)
            .build()
    }

    @UnauthenticatedRetrofit
    @Provides
    @Singleton
    fun provideUnauthenticatedRetrofit(
        @UnauthenticatedOkHttpClient okHttpClient: OkHttpClient,
        @ApiBaseUrl baseUrl: String,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @AuthenticatedRetrofit
    @Provides
    @Singleton
    fun provideAuthenticatedRetrofit(
        @AuthenticatedOkHttpClient okHttpClient: OkHttpClient,
        @ApiBaseUrl baseUrl: String,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}