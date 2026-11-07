package com.example.upk.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upk.core.analytics.AnalyticsTracker
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
    getThemeModeUseCase: GetThemeModeUseCase,
) : ViewModel() {

    private var lastLoggedScreenName: String? = null

    val uiState: StateFlow<AppUiState> =
        getThemeModeUseCase()
            .distinctUntilChanged()
            .map { themeMode -> AppUiState(themeMode = themeMode) }
            .catch { emit(AppUiState(themeMode = ThemeMode.SYSTEM)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AppUiState(),
            )

    fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.ScreenViewed -> trackScreenView(intent.screenName)
        }
    }

    private fun trackScreenView(screenName: String) {
        if (lastLoggedScreenName == screenName) return
        lastLoggedScreenName = screenName
        analyticsTracker.logScreenView(screenName)
    }
}