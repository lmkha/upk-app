package com.example.upk.settings.domain.usecases

import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeModeUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke() : Flow<ThemeMode> {
        return repository.getThemeMode()
    }
}
