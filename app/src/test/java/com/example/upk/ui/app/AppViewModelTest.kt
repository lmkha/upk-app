package com.example.upk.ui.app

import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.usecases.GetThemeModeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeSettingsRepository
    private lateinit var analyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        analyticsTracker = FakeAnalyticsTracker()
    }

    private fun createViewModel(repo: FakeSettingsRepository = repository) = AppViewModel(
        analyticsTracker = analyticsTracker,
        getThemeModeUseCase = GetThemeModeUseCase(repo),
    )

    // ---------- Initial state & theme observation ----------

    @Test
    fun `default AppUiState has null themeMode`() {
        val state = AppUiState()

        assertNull(state.themeMode)
    }

    @Test
    fun `themeMode stays null until repository emits`() = runTest {
        val neverEmitRepository = object : FakeSettingsRepository() {
            override fun getThemeMode() = flow<ThemeMode> {
                CompletableDeferred<Unit>().await()
            }
        }

        val viewModel = createViewModel(neverEmitRepository)

        assertNull(viewModel.uiState.value.themeMode)
    }

    @Test
    fun `after init uiState reflects stored theme from repository`() = runTest {
        repository.themeMode.value = ThemeMode.DARK

        val viewModel = createViewModel()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `uiState follows theme changes from repository`() = runTest {
        val viewModel = createViewModel()

        repository.themeMode.value = ThemeMode.LIGHT
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)

        repository.themeMode.value = ThemeMode.DARK
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `getThemeMode error falls back to SYSTEM theme`() = runTest {
        repository.getThemeModeError = IllegalStateException("DataStore is corrupted")

        val viewModel = createViewModel()

        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }

    // ---------- ScreenViewed ----------

    @Test
    fun `ScreenViewed logs screen view to analytics tracker`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(AppIntent.ScreenViewed("home"))

        assertEquals(1, analyticsTracker.logScreenViewCallCount)
        assertEquals("home", analyticsTracker.lastLoggedScreenName)
    }

    @Test
    fun `consecutive ScreenViewed intents for the same screen log only once`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(AppIntent.ScreenViewed("home"))
        viewModel.onIntent(AppIntent.ScreenViewed("home"))

        assertEquals(1, analyticsTracker.logScreenViewCallCount)
        assertEquals(listOf("home"), analyticsTracker.loggedScreenNames)
    }

    @Test
    fun `ScreenViewed logs again after navigating to a different screen and back`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(AppIntent.ScreenViewed("home"))
        viewModel.onIntent(AppIntent.ScreenViewed("setting"))
        viewModel.onIntent(AppIntent.ScreenViewed("home"))

        assertEquals(3, analyticsTracker.logScreenViewCallCount)
        assertEquals(listOf("home", "setting", "home"), analyticsTracker.loggedScreenNames)
    }
}
