package com.example.upk.settings.domain

import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake of [SettingsRepository].
 * Lets use case tests run without a real DataStore.
 */
class FakeSettingsRepository : SettingsRepository {
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val languageTag = MutableStateFlow("")

    var setThemeModeCallCount = 0
        private set
    var setLanguageTagCallCount = 0
        private set

    override fun getThemeMode(): Flow<ThemeMode> = themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        setThemeModeCallCount++
        themeMode.value = mode
    }

    override fun getLanguageTag(): Flow<String> = languageTag

    override suspend fun setLanguageTag(languageTag: String) {
        setLanguageTagCallCount++
        this.languageTag.value = languageTag
    }
}
