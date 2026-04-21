package com.example.upk.data.remote

import com.example.upk.BuildConfig
import com.example.upk.data.remote.interceptor.AuthInterceptor
import com.example.upk.di.AuthenticatedOkHttpClient
import com.example.upk.di.AuthenticatedRetrofit
import com.example.upk.di.UnauthenticatedOkHttpClient
import com.example.upk.di.UnauthenticatedRetrofit
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

    @UnauthenticatedRetrofit
    @Provides
    @Singleton
    fun provideUnauthenticatedRetrofit(
        @UnauthenticatedOkHttpClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @AuthenticatedRetrofit
    @Provides
    @Singleton
    fun provideAuthenticatedRetrofit(
        @AuthenticatedOkHttpClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @AuthenticatedOkHttpClient
    @Provides
    @Singleton
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
//        tokenAuthenticator: TokenAuthenticator,
    ) : OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
//            .authenticator(tokenAuthenticator)
            .build()
    }

    @UnauthenticatedOkHttpClient
    @Provides
    @Singleton
    fun provideUnauthenticatedOkHttpClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}