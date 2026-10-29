package com.example.upk.settings.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.upk.settings.data.local.datasource.SettingsLocalDataSource
import com.example.upk.settings.data.local.datasource.SettingsLocalDataSourceImpl
import com.example.upk.settings.data.local.datastore.AppSettingsPreferencesSerializer
import com.example.upk.settings.data.repository.SettingsRepositoryImpl
import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.core.data.remote.AppLanguageProvider
import com.example.upk.settings.domain.repository.SettingsRepository
import com.example.upk.settings.data.language.SettingsAppLanguageProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDataModule {

    @Binds
    @Singleton
    abstract fun bindAppLanguageProvider(
        impl: SettingsAppLanguageProviderImpl
    ): AppLanguageProvider

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsLocalDataSource(
        impl: SettingsLocalDataSourceImpl
    ): SettingsLocalDataSource

    companion object {
        @Provides
        @Singleton
        fun provideAppSettingsPreferences(
            @ApplicationContext context: Context
        ): DataStore<AppSettingsPreferences> {
            return context.appSettingsPreferences
        }
    }
}

private val Context.appSettingsPreferences: DataStore<AppSettingsPreferences> by dataStore(
    fileName = "app_settings_preferences.pb",
    serializer = AppSettingsPreferencesSerializer
)
