package com.example.upk.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.usecases.GetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AppUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isThemeResolved: Boolean = false,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    getThemeModeUseCase: GetThemeModeUseCase,
) : ViewModel() {
    val uiState: StateFlow<AppUiState> =
        getThemeModeUseCase()
            .distinctUntilChanged()
            .map { themeMode ->
                AppUiState(
                    themeMode = themeMode,
                    isThemeResolved = true,
                )
            }
            .catch {
                emit(
                    AppUiState(
                        themeMode = ThemeMode.SYSTEM,
                        isThemeResolved = true,
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AppUiState(),
            )
}