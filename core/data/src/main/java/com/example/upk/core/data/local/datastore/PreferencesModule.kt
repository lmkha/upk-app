package com.example.upk.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.upk.core.data.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.appPreferences: DataStore<AppPreferences> by dataStore(
    fileName = "app_preferences.pb",
    serializer = AppPreferencesSerializer
)

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): DataStore<AppPreferences> {
        return context.appPreferences
    }
}