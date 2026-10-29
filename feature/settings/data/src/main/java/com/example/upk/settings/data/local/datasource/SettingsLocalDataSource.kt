package com.example.upk.settings.data.local.datasource

import com.example.upk.settings.data.AppSettingsPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    fun observeThemeMode(): Flow<AppSettingsPreferences.ThemeProto>
    suspend fun setThemeMode(themeMode: AppSettingsPreferences.ThemeProto)
    fun observeLanguageTag(): Flow<String>
    suspend fun setLanguageTag(languageTag: String)
}