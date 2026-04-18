package com.example.androidspace.di

import com.example.androidspace.BuildConfig
import com.example.androidspace.data.remote.interceptor.AuthInterceptor
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

    @Provides
    @Singleton
    fun provideRetrofit(
        @UnauthenticatedOkhttpClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @AuthorizedOkHttpClient
    @Provides
    @Singleton
    fun provideAuthorizedOkHttpClient(
        authInterceptor: AuthInterceptor,
//        tokenAuthenticator: TokenAuthenticator,
    ) : OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
//            .authenticator(tokenAuthenticator)
            .build()
    }

    @UnauthenticatedOkhttpClient
    @Provides
    @Singleton
    fun provideUnauthorizedOkHttpClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}
