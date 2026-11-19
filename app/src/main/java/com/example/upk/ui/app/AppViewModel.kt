package com.example.upk.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upk.core.analytics.AnalyticsTracker
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.usecases.GetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AppIntent {
    data class ScreenViewed(
        val screenName: String,
    ) : AppIntent
}

data class AppUiState(
    val themeMode: ThemeMode? = null,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val getThemeModeUseCase: GetThemeModeUseCase,
) : ViewModel() {

    private var lastLoggedScreenName: String? = null

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeThemeMode()
    }

    fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.ScreenViewed -> trackScreenView(intent.screenName)
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            getThemeModeUseCase()
                .distinctUntilChanged()
                .catch {
                    _uiState.update {
                        it.copy(themeMode = ThemeMode.SYSTEM)
                    }
                }
                .collect { themeMode ->
                    _uiState.update {
                        it.copy(themeMode = themeMode)
                    }
                }
        }
    }

    private fun trackScreenView(screenName: String) {
        if (lastLoggedScreenName == screenName) return
        lastLoggedScreenName = screenName
        analyticsTracker.logScreenView(screenName)
    }
}