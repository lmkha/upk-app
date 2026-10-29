
package com.example.upk.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upk.core.analytics.AnalyticsTracker
import com.example.upk.core.analytics.AppAnalyticsEvent
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.usecases.GetThemeModeUseCase
import com.example.upk.settings.domain.usecases.UpdateThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
    val isUpdatingTheme: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SettingsIntent {
    data class ThemeModeSelected(
        val themeMode: ThemeMode,
    ) : SettingsIntent

    data object ErrorMessageDismissed : SettingsIntent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeThemeMode()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> {
                changeThemeMode(intent.themeMode)
            }
            SettingsIntent.ErrorMessageDismissed -> {
                _uiState.update {
                    it.copy(errorMessage = null)
                }
            }
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            getThemeModeUseCase()
                .distinctUntilChanged()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                                ?: "Unable to load appearance settings.",
                        )
                    }
                }
                .collect { themeMode ->
                    _uiState.update {
                        it.copy(
                            themeMode = themeMode,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun changeThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (
                currentState.isLoading ||
                currentState.isUpdatingTheme ||
                currentState.themeMode == themeMode
            ) {
                return@launch
            }

            _uiState.update {
                it.copy(
                    isUpdatingTheme = true,
                    errorMessage = null,
                )
            }

            try {
                updateThemeModeUseCase(themeMode)
                analyticsTracker.logEvent(AppAnalyticsEvent.ThemeChanged(theme = themeMode.name))
                // The observed Flow updates themeMode after persistence changes.
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message
                            ?: "Unable to update appearance settings.",
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isUpdatingTheme = false)
                }
            }
        }
    }
}
