package com.example.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.core.data.remote.AppLanguageProvider
import com.example.upk.settings.domain.repository.SettingsRepository
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
