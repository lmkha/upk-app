package com.example.settings.data

import com.example.upk.core.data.remote.AppLanguageProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsAppLanguageProviderImpl @Inject constructor(
    private val languageSnapshot: LanguageSnapshot
) : AppLanguageProvider {

    override fun getLanguageTag(): String = languageSnapshot.getLanguageTag()
}