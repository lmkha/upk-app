package com.example.upk.setttings.data.language

import com.example.upk.core.common.ApplicationScope
import com.example.upk.core.data.remote.AppLanguageProvider
import com.example.upk.setttings.data.local.datasource.SettingsLocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsAppLanguageProviderImpl @Inject constructor(
    localDataSource: SettingsLocalDataSource,
    private val languageSnapshot: LanguageSnapshot,
    @ApplicationScope scope: CoroutineScope,
) : AppLanguageProvider {

    init {
        scope.launch {
            localDataSource.observeLanguageTag()
                .map { storedTag ->
                    storedTag.ifBlank {
                        Locale.getDefault().toLanguageTag()
                    }
                }
                .distinctUntilChanged()
                .collect(languageSnapshot::updateLanguageTag)
        }
    }

    override fun getLanguageTag(): String {
        return languageSnapshot.getLanguageTag()
    }
}