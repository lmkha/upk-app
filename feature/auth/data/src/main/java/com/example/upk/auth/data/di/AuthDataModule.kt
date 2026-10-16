package com.example.upk.auth.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.upk.auth.data.AuthPreferences
import com.example.upk.auth.data.token.TokenProviderImpl
import com.example.upk.auth.data.local.datasource.AuthLocalDataSource
import com.example.upk.auth.data.local.datasource.AuthLocalDataSourceImpl
import com.example.upk.auth.data.local.datastore.AuthPreferencesSerializer
import com.example.upk.auth.data.remote.api.AuthApi
import com.example.upk.auth.data.remote.datasource.AuthRemoteDataSource
import com.example.upk.auth.data.remote.datasource.AuthRemoteDataSourceImpl
import com.example.upk.core.data.remote.UnauthenticatedRetrofit
import com.example.upk.core.data.remote.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

private val Context.authPreferences: DataStore<AuthPreferences> by dataStore(
    fileName = "auth_preferences.pb",
    serializer = AuthPreferencesSerializer
)

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        impl: TokenProviderImpl
    ): TokenProvider

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): AuthRemoteDataSource

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(@UnauthenticatedRetrofit retrofit: Retrofit): AuthApi {
            return retrofit.create(AuthApi::class.java)
        }

        @Provides
        @Singleton
        fun provideAuthPreferences(
            @ApplicationContext context: Context
        ): DataStore<AuthPreferences> {
            return context.authPreferences
        }
    }

    @Binds
    abstract fun bindAuthLocalDataSource(
        impl: AuthLocalDataSourceImpl
    ): AuthLocalDataSource
}