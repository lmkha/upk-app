package com.example.upk.setttings.data.repository

import com.example.upk.setttings.data.local.datasource.SettingsLocalDataSource
import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val localDataSource: SettingsLocalDataSource,
) : SettingsRepository {

    override fun getThemeMode(): Flow<ThemeMode> {
        return localDataSource.observeThemeMode()
            .map(::mapToDomainTheme)
            .distinctUntilChanged()
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        localDataSource.setThemeMode(mapToProtoTheme(mode))
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

    override fun getLanguageTag(): Flow<String> {
        return localDataSource.observeLanguageTag()
            .distinctUntilChanged()
    }

    override suspend fun setLanguageTag(languageTag: String) {
        val normalizedTag = languageTag
            .trim()
            .let { tag ->
                if (tag.isBlank()) {
                    ""
                } else {
                    Locale.forLanguageTag(tag).toLanguageTag()
                }
            }

        localDataSource.setLanguageTag(normalizedTag)
    }
}