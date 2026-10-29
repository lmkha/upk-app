package com.example.upk.settings.data.local.datasource

import androidx.datastore.core.DataStore
import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.settings.data.copy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsLocalDataSourceImpl @Inject constructor(
    private val settingsDataStore: DataStore<AppSettingsPreferences>,
) : SettingsLocalDataSource {

    override fun observeThemeMode(): Flow<AppSettingsPreferences.ThemeProto> {
        return settingsDataStore.data.map { preferences ->
            preferences.themeMode
        }
    }

    override suspend fun setThemeMode(
        themeMode: AppSettingsPreferences.ThemeProto,
    ) {
        settingsDataStore.updateData { preferences ->
            preferences.toBuilder()
                .setThemeMode(themeMode)
                .build()
        }
    }

    override fun observeLanguageTag(): Flow<String> {
        return settingsDataStore.data.map { preferences ->
            preferences.language
        }
    }

    override suspend fun setLanguageTag(languageTag: String) {
        settingsDataStore.updateData { currentPrefs ->
            currentPrefs.copy {
                language = languageTag
            }
        }
    }
}
