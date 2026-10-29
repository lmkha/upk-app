package com.example.upk.settings.data.local.datasource

import com.example.upk.settings.data.AppSettingsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake of [SettingsLocalDataSource], used by repository and language provider tests.
 * Initial values match the proto defaults: theme = THEME_UNSPECIFIED, language = "".
 */
class FakeSettingsLocalDataSource : SettingsLocalDataSource {
    val themeMode = MutableStateFlow(AppSettingsPreferences.ThemeProto.THEME_UNSPECIFIED)
    val languageTag = MutableStateFlow("")

    var lastSetThemeMode: AppSettingsPreferences.ThemeProto? = null
        private set
    var lastSetLanguageTag: String? = null
        private set

    override fun observeThemeMode(): Flow<AppSettingsPreferences.ThemeProto> = themeMode

    override suspend fun setThemeMode(themeMode: AppSettingsPreferences.ThemeProto) {
        lastSetThemeMode = themeMode
        this.themeMode.value = themeMode
    }

    override fun observeLanguageTag(): Flow<String> = languageTag

    override suspend fun setLanguageTag(languageTag: String) {
        lastSetLanguageTag = languageTag
        this.languageTag.value = languageTag
    }
}
