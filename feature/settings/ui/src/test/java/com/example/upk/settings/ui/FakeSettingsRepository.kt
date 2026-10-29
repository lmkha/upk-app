package com.example.upk.settings.ui

import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Fake repository for ViewModel tests. Besides storing values in memory it can simulate
 * situations that are hard to reproduce with the real repository:
 *  - getThemeMode throws                              (getThemeModeError)
 *  - setThemeMode throws                              (setThemeModeError)
 *  - setThemeMode suspends until the test releases it (setThemeModeGate)
 */
open class FakeSettingsRepository : SettingsRepository {
    val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val languageTag = MutableStateFlow("")

    var getThemeModeError: Throwable? = null
    var setThemeModeError: Throwable? = null
    var setThemeModeGate: CompletableDeferred<Unit>? = null

    var setThemeModeCallCount = 0
        private set

    override fun getThemeMode(): Flow<ThemeMode> {
        getThemeModeError?.let { error ->
            return flow { throw error }
        }
        return themeMode
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        setThemeModeCallCount++
        setThemeModeGate?.await()
        setThemeModeError?.let { throw it }
        themeMode.value = mode
    }

    override fun getLanguageTag(): Flow<String> = languageTag

    override suspend fun setLanguageTag(languageTag: String) {
        this.languageTag.value = languageTag
    }
}
