package com.example.settings.data

import androidx.datastore.core.DataStore
import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val authDataStore: DataStore<AppSettingsPreferences>
) : SettingsRepository {

    override fun getThemeMode(): Flow<ThemeMode> {
        return authDataStore.data.map { preferences ->
            mapToDomainTheme(preferences.themeMode)
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        authDataStore.updateData { preferences ->
            preferences.toBuilder()
                .setThemeMode(mapToProtoTheme(mode))
                .build()
        }
    }

    private fun mapToDomainTheme(protoTheme: AppSettingsPreferences.ThemeProto): ThemeMode {
        return when (protoTheme) {
            AppSettingsPreferences.ThemeProto.DARK -> ThemeMode.DARK
            AppSettingsPreferences.ThemeProto.LIGHT -> ThemeMode.LIGHT
            AppSettingsPreferences.ThemeProto.SYSTEM,
            AppSettingsPreferences.ThemeProto.THEME_UNSPECIFIED,
            AppSettingsPreferences.ThemeProto.UNRECOGNIZED -> ThemeMode.SYSTEM
        }
    }

    private fun mapToProtoTheme(domainTheme: ThemeMode): AppSettingsPreferences.ThemeProto {
        return when (domainTheme) {
            ThemeMode.DARK -> AppSettingsPreferences.ThemeProto.DARK
            ThemeMode.LIGHT -> AppSettingsPreferences.ThemeProto.LIGHT
            ThemeMode.SYSTEM -> AppSettingsPreferences.ThemeProto.SYSTEM
        }
    }
}