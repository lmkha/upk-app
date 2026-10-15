package com.example.upk.settings.domain.usecases

import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateThemeModeUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(newMode: ThemeMode) {
        return repository.setThemeMode(newMode)
    }
}
